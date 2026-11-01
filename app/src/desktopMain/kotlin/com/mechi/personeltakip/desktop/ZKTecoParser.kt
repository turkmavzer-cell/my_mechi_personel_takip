package com.mechi.personeltakip.desktop

import com.mechi.personeltakip.domain.calculator.CalendarUtils
import com.mechi.personeltakip.domain.models.*
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.util.regex.Pattern
import kotlin.math.max
import kotlin.math.roundToInt

object ZKTecoParser {

    fun parseExcel(filePath: String, shiftSettings: GlobalShiftSettings): Pair<List<EmployeeAttendance>, ParsingSummary> {
        val file = File(filePath)
        val fis = FileInputStream(file)
        val workbook: Workbook = if (filePath.endsWith(".xlsx", ignoreCase = true)) {
            XSSFWorkbook(fis)
        } else {
            HSSFWorkbook(fis)
        }

        val sheet = workbook.getSheet("Att.log report") ?: workbook.getSheetAt(0)
        val matrixRows = mutableListOf<List<String>>()
        val dataFormatter = DataFormatter()

        for (row in sheet) {
            val rowList = mutableListOf<String>()
            for (c in 0 until row.lastCellNum) {
                val cell = row.getCell(c.toInt())
                rowList.add(dataFormatter.formatCellValue(cell).trim())
            }
            matrixRows.add(rowList)
        }
        workbook.close()
        fis.close()

        if (matrixRows.isEmpty()) throw Exception("Excel dosyası boş veya okunamadı.")

        // 1. Gün başlıklarını bul (1, 2, 3... 30/31)
        val dayColMap = mutableMapOf<Int, Int>()
        var dateRange = "Tespit Edilemedi"
        var detectedYearMonth = shiftSettings.yearMonth

        for (r in 0 until minOf(matrixRows.size, 25)) {
            val row = matrixRows[r]
            val fullRowText = row.joinToString(" ")

            // Tarih Aralığını Yakala (Att. Time 2026-07-01 ~ ...)
            if (fullRowText.contains("Att. Time", ignoreCase = true)) {
                val m = Pattern.compile("Att\\.\\s*Time\\s*([0-9]{4})-([0-9]{2})-([0-9]{2})", Pattern.CASE_INSENSITIVE).matcher(fullRowText)
                if (m.find()) {
                    val year = m.group(1)
                    val month = m.group(2)
                    detectedYearMonth = "$year-$month"
                    dateRange = detectedYearMonth
                }
            }

            var count = 0
            val tempMap = mutableMapOf<Int, Int>()
            for (c in row.indices) {
                val valStr = row[c]
                val num = valStr.toIntOrNull()
                if (num != null && num in 1..31) {
                    tempMap[num] = c
                    count++
                }
            }
            if (count >= 15) {
                dayColMap.putAll(tempMap)
            }
        }

        val daysInMonth = if (dayColMap.isNotEmpty()) dayColMap.keys.maxOrNull() ?: 30 else 30

        // 2. Personel bloklarını bul
        val empHeaderIndices = mutableListOf<Int>()
        for (r in matrixRows.indices) {
            val rowText = matrixRows[r].joinToString(" ")
            if (Pattern.compile("ID\\s*[:=]", Pattern.CASE_INSENSITIVE).matcher(rowText).find()) {
                empHeaderIndices.add(r)
            }
        }

        val employees = mutableListOf<EmployeeAttendance>()

        for (i in empHeaderIndices.indices) {
            val start = empHeaderIndices[i]
            val end = if (i + 1 < empHeaderIndices.size) empHeaderIndices[i + 1] else matrixRows.size
            val empBlock = matrixRows.subList(start, end)

            val headerText = empBlock[0].joinToString(" ")
            val id = findValue(headerText, "ID") ?: (i + 1).toString()
            val name = findValue(headerText, "Name") ?: "Personel $id"
            val dept = findValue(headerText, "Dept") ?: "Genel"

            val rawPunchesPerDay = mutableMapOf<Int, MutableList<String>>()
            for (day in 1..daysInMonth) {
                val col = dayColMap[day] ?: (day - 1)
                val dayPunches = mutableListOf<String>()
                for (row in empBlock) {
                    if (col < row.size) {
                        dayPunches.addAll(extractTimes(row[col]))
                    }
                }
                rawPunchesPerDay[day] = dayPunches.distinct().sortedBy { timeToMinutes(it) }.toMutableList()
            }

            // --- AKILLI ALGORİTMA: GECE VARDİYASI VE UNUTULAN GİRİŞLER ---
            val resolvedDays = mutableMapOf<Int, ResolvedDayState>()
            for (day in 1..daysInMonth) {
                resolvedDays[day] = ResolvedDayState(punches = rawPunchesPerDay[day] ?: mutableListOf())
            }

            // Gece 12'den sonraki (00:00 - 07:15) çıkışları bir önceki güne bağla
            for (day in 1 until daysInMonth) {
                val current = resolvedDays[day]!!
                val next = resolvedDays[day + 1]!!

                if (next.punches.isNotEmpty()) {
                    val firstNextMin = timeToMinutes(next.punches[0])
                    if (firstNextMin <= 7.25 * 60) { // 07:15'e kadar
                        if (current.punches.isNotEmpty()) {
                            // Mevcut günde akşam çıkışı yoksa, sonraki günün sabahını buna bağla
                            val lastCurrentMin = timeToMinutes(current.punches.last())
                            if (current.punches.size < 2 || (lastCurrentMin < 15 * 60)) {
                                val overnightExit = next.punches.removeAt(0)
                                current.checkIn = current.punches.first()
                                current.checkOut = overnightExit
                                current.isAutoCorrected = true
                                current.note = "Gece Çıkışı Bağlandı"
                            }
                        } else {
                            // Bugün hiç basmamış ama yarın sabah erkenden basmış -> Bugünün girişini unutmuş
                            val overnightExit = next.punches.removeAt(0)
                            current.checkIn = shiftSettings.workStartTime
                            current.checkOut = overnightExit
                            current.isAutoCorrected = true
                            current.note = "Giriş Unutulmuş (Akıllı Tamamlama)"
                        }
                    }
                }
            }

            val dailyLogs = mutableMapOf<Int, DailyPunch>()
            for (day in 1..daysInMonth) {
                val state = resolvedDays[day]!!
                var checkIn = state.checkIn
                var checkOut = state.checkOut
                var isAuto = state.isAutoCorrected
                var note = state.note
                var workedHours = 0.0

                if (checkIn != null && checkOut != null) {
                    workedHours = calculateDuration(checkIn, checkOut)
                } else if (state.punches.size >= 2) {
                    checkIn = state.punches.first()
                    checkOut = state.punches.last()
                    workedHours = calculateDuration(checkIn, checkOut)
                } else if (state.punches.size == 1) {
                    // TEK PARMAK İZİ AKILLI TAMAMLAMA
                    checkIn = state.punches.first()
                    val min = timeToMinutes(checkIn)
                    if (min >= 15 * 60) { // Saat 15:00'den sonra basılmışsa çıkıştır
                        checkOut = checkIn
                        val estIn = max(0, min - shiftSettings.standardDailyHours * 60)
                        checkIn = minutesToTime(estIn)
                    } else { // Sabah basılmışsa giriştir
                        val estOut = min + shiftSettings.standardDailyHours * 60
                        checkOut = minutesToTime(estOut)
                    }
                    workedHours = shiftSettings.standardDailyHours.toDouble()
                    isAuto = true
                    note = "Tek Parmak İzi (Otomatik)"
                }

                val isWeekend = CalendarUtils.isWeekend(detectedYearMonth, day, shiftSettings.weekendDays)

                dailyLogs[day] = DailyPunch(
                    day = day,
                    dateStr = "$detectedYearMonth-${day.toString().padStart(2, '0')}",
                    checkIn = checkIn,
                    checkOut = checkOut,
                    allPunches = state.punches,
                    workedHours = (workedHours * 100.0).roundToInt() / 100.0,
                    overtimeHours = if (isWeekend) workedHours else max(0.0, workedHours - shiftSettings.standardDailyHours),
                    latenessHours = if (!isWeekend && workedHours < shiftSettings.standardDailyHours && workedHours > 0) shiftSettings.standardDailyHours - workedHours else 0.0,
                    isWeekend = isWeekend,
                    status = if (workedHours > 0) "present" else if (isWeekend) "weekend" else "absent",
                    isAutoCorrected = isAuto,
                    note = note
                )
            }

            employees.add(
                EmployeeAttendance(
                    id = id,
                    name = name,
                    department = dept,
                    dailyLogs = dailyLogs,
                    daysPresent = dailyLogs.values.count { it.status == "present" },
                    daysAbsent = dailyLogs.values.count { it.status == "absent" },
                    daysWeekendPresent = dailyLogs.values.count { it.status == "present" && it.isWeekend },
                    totalWorkedHours = dailyLogs.values.sumOf { it.workedHours },
                    totalOvertimeHoursWeekday = dailyLogs.values.filter { !it.isWeekend }.sumOf { it.overtimeHours },
                    totalOvertimeHoursWeekend = dailyLogs.values.filter { it.isWeekend }.sumOf { it.overtimeHours },
                    totalLatenessHours = dailyLogs.values.sumOf { it.latenessHours }
                )
            )
        }

        return Pair(employees, ParsingSummary(
            fileName = file.name,
            dateRange = dateRange,
            totalEmployeesFound = employees.size,
            totalDaysDetected = dayColMap.size,
            rawSheetNames = listOf(sheet.sheetName),
            activeSheet = sheet.sheetName,
            detectedYearMonth = detectedYearMonth
        ))
    }

    private data class ResolvedDayState(
        var checkIn: String? = null,
        var checkOut: String? = null,
        val punches: MutableList<String>,
        var isAutoCorrected: Boolean = false,
        var note: String? = null
    )

    private fun calculateDuration(start: String, end: String): Double {
        var startMin = timeToMinutes(start)
        var endMin = timeToMinutes(end)
        if (endMin < startMin) endMin += 24 * 60 // Gece vardiyası
        return (endMin - startMin) / 60.0
    }

    private fun findValue(text: String, key: String): String? {
        val pattern = Pattern.compile("$key\\s*[:=]\\s*([^\\|\\s]+)", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)
        return if (matcher.find()) matcher.group(1).trim() else null
    }

    private fun extractTimes(text: String): List<String> {
        val pattern = Pattern.compile("\\d{1,2}:\\d{2}")
        val matcher = pattern.matcher(text)
        val list = mutableListOf<String>()
        while (matcher.find()) {
            list.add(matcher.group())
        }
        return list
    }

    private fun timeToMinutes(timeStr: String): Int {
        val parts = timeStr.split(":")
        if (parts.size < 2) return 0
        return (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].take(2).toIntOrNull() ?: 0)
    }

    private fun minutesToTime(totalMinutes: Int): String {
        val h = (totalMinutes / 60) % 24
        val m = totalMinutes % 60
        return "%02d:%02d".format(h, m)
    }
}
