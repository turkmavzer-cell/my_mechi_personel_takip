import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.mechi.personeltakip.desktop.*
import com.mechi.personeltakip.domain.calculator.CalendarUtils
import com.mechi.personeltakip.domain.models.*
import com.mechi.personeltakip.presentation.App
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

fun main() = application {
    val windowState = rememberWindowState(width = 1440.dp, height = 900.dp)

    // Önceki durumu yükle
    val savedState = remember { PersistenceManager.loadState() ?: AppPersistentState() }

    var employees by remember { mutableStateOf(savedState.employees) }
    var summary by remember { mutableStateOf(savedState.summary) }
    var shiftSettings by remember { mutableStateOf(savedState.shiftSettings) }
    val payrollSettingsMap = remember {
        val map = mutableStateMapOf<String, EmployeePayrollSettings>()
        map.putAll(savedState.payrollSettingsMap)
        map
    }

    // Durum değiştiğinde kaydet
    LaunchedEffect(employees, summary, shiftSettings, payrollSettingsMap.toMap()) {
        PersistenceManager.saveState(
            AppPersistentState(
                employees = employees,
                summary = summary,
                shiftSettings = shiftSettings,
                payrollSettingsMap = payrollSettingsMap.toMap()
            )
        )
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Mechi ZKTeco - Kurumsal Bordro & Puantaj Otomasyonu",
        state = windowState
    ) {
        App(
            employees = employees,
            summary = summary,
            shiftSettings = shiftSettings,
            payrollSettingsMap = payrollSettingsMap.toMap(),
            onShiftSettingsChange = { shiftSettings = it },
            onUpdatePayrollSettings = { id, settings -> payrollSettingsMap[id] = settings },
            onUpdateDayPunch = { id, day, isPresent, hours ->
                employees = employees.map { emp ->
                    if (emp.id == id) {
                        val currentLog = emp.dailyLogs[day]
                        val newStatus = if (isPresent) "present" else "absent"
                        val updatedLog = (currentLog ?: DailyPunch(day, "", null, null, emptyList(), 0.0, 0.0, 0.0, false, "absent")).copy(
                            status = newStatus,
                            workedHours = hours,
                            overtimeHours = if (isPresent) max(0.0, hours - shiftSettings.standardDailyHours) else 0.0,
                            note = "Manuel Düzenleme"
                        )
                        val newLogs = emp.dailyLogs.toMutableMap()
                        newLogs[day] = updatedLog
                        emp.copy(dailyLogs = newLogs.toMap())
                    } else emp
                }
            },
            onUpdateDayTimes = { id, day, newCheckIn, newCheckOut ->
                employees = employees.map { emp ->
                    if (emp.id == id) {
                        val currentLog = emp.dailyLogs[day] ?: DailyPunch(day, "", null, null, emptyList(), 0.0, 0.0, 0.0, false, "absent")
                        val isWeekend = CalendarUtils.isWeekend(shiftSettings.yearMonth, day, shiftSettings.weekendDays)

                        var workedHours = 0.0
                        if (newCheckIn != null && newCheckOut != null) {
                            val inParts = newCheckIn.split(":")
                            val outParts = newCheckOut.split(":")
                            val inMin = (inParts.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (inParts.getOrNull(1)?.take(2)?.toIntOrNull() ?: 0)
                            var outMin = (outParts.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (outParts.getOrNull(1)?.take(2)?.toIntOrNull() ?: 0)
                            if (outMin < inMin) outMin += 24 * 60
                            workedHours = (outMin - inMin) / 60.0
                        }

                        val isPresent = workedHours > 0 || (newCheckIn != null || newCheckOut != null)
                        val updatedLog = currentLog.copy(
                            checkIn = newCheckIn,
                            checkOut = newCheckOut,
                            workedHours = (workedHours * 100.0).roundToInt() / 100.0,
                            overtimeHours = if (isWeekend) workedHours else max(0.0, workedHours - shiftSettings.standardDailyHours),
                            status = if (isPresent) "present" else if (isWeekend) "weekend" else "absent",
                            note = "Saat Manuel Değiştirildi"
                        )
                        val newLogs = emp.dailyLogs.toMutableMap()
                        newLogs[day] = updatedLog
                        emp.copy(dailyLogs = newLogs.toMap())
                    } else emp
                }
            },
            onUpdatePreviousMonthDebt = { id, debt ->
                val current = payrollSettingsMap[id] ?: EmployeePayrollSettings(id, 6500.0)
                payrollSettingsMap[id] = current.copy(previousMonthDebt = debt)
            },
            onAddDay = { id, day ->
                if (day > shiftSettings.totalDaysInMonth) {
                    shiftSettings = shiftSettings.copy(totalDaysInMonth = day)
                }
                employees = employees.map { emp ->
                    if (emp.id == id) {
                        val isWeekend = CalendarUtils.isWeekend(shiftSettings.yearMonth, day, shiftSettings.weekendDays)
                        val newLog = DailyPunch(
                            day = day,
                            dateStr = "${shiftSettings.yearMonth}-${day.toString().padStart(2, '0')}",
                            checkIn = shiftSettings.workStartTime,
                            checkOut = shiftSettings.workEndTime,
                            allPunches = listOf(shiftSettings.workStartTime, shiftSettings.workEndTime),
                            workedHours = shiftSettings.standardDailyHours.toDouble(),
                            overtimeHours = 0.0,
                            latenessHours = 0.0,
                            isWeekend = isWeekend,
                            status = "present",
                            isAutoCorrected = true,
                            note = "Manuel Eklenen Gün"
                        )
                        val newLogs = emp.dailyLogs.toMutableMap()
                        newLogs[day] = newLog
                        emp.copy(dailyLogs = newLogs.toMap())
                    } else emp
                }
            },
            onDeleteDay = { id, day ->
                employees = employees.map { emp ->
                    if (emp.id == id) {
                        val newLogs = emp.dailyLogs.toMutableMap()
                        newLogs.remove(day)
                        emp.copy(dailyLogs = newLogs.toMap())
                    } else emp
                }
            },
            onToggleOvertime = { id, day ->
                employees = employees.map { emp ->
                    if (emp.id == id) {
                        val currentLog = emp.dailyLogs[day]
                        currentLog?.let {
                            val updatedLog = it.copy(isOvertimeDisabled = !it.isOvertimeDisabled)
                            val newLogs = emp.dailyLogs.toMutableMap()
                            newLogs[day] = updatedLog
                            emp.copy(dailyLogs = newLogs.toMap())
                        } ?: emp
                    } else emp
                }
            },
            onToggleLateness = { id, day ->
                employees = employees.map { emp ->
                    if (emp.id == id) {
                        val currentLog = emp.dailyLogs[day]
                        currentLog?.let {
                            val updatedLog = it.copy(isLatenessDisabled = !it.isLatenessDisabled)
                            val newLogs = emp.dailyLogs.toMutableMap()
                            newLogs[day] = updatedLog
                            emp.copy(dailyLogs = newLogs.toMap())
                        } ?: emp
                    } else emp
                }
            },
            onToggleEarlyIn = { id, day ->
                employees = employees.map { emp ->
                    if (emp.id == id) {
                        val currentLog = emp.dailyLogs[day]
                        currentLog?.let {
                            val updatedLog = it.copy(isEarlyInDisabled = !it.isEarlyInDisabled)
                            val newLogs = emp.dailyLogs.toMutableMap()
                            newLogs[day] = updatedLog
                            emp.copy(dailyLogs = newLogs.toMap())
                        } ?: emp
                    } else emp
                }
            },
            onToggleEarlyOut = { id, day ->
                employees = employees.map { emp ->
                    if (emp.id == id) {
                        val currentLog = emp.dailyLogs[day]
                        currentLog?.let {
                            val updatedLog = it.copy(isEarlyOutDisabled = !it.isEarlyOutDisabled)
                            val newLogs = emp.dailyLogs.toMutableMap()
                            newLogs[day] = updatedLog
                            emp.copy(dailyLogs = newLogs.toMap())
                        } ?: emp
                    } else emp
                }
            },
            onUpdateAbsenceMultiplier = { id, day, mult ->
                val current = payrollSettingsMap[id] ?: EmployeePayrollSettings(id, 6500.0)
                val newDailyMults = current.dailyAbsenceMultipliers.toMutableMap()
                newDailyMults[day] = mult
                payrollSettingsMap[id] = current.copy(dailyAbsenceMultipliers = newDailyMults.toMap())
            },
            onPrint = { results ->
                PrintService.printPayroll(results)
            },
            onExportExcel = { results ->
                val fileDialog = FileDialog(null as Frame?, "Bordro Excelini Kaydet", FileDialog.SAVE)
                fileDialog.file = "Mechi_Bordro_${shiftSettings.yearMonth}.xlsx"
                fileDialog.isVisible = true
                val file = fileDialog.file
                val directory = fileDialog.directory
                if (file != null && directory != null) {
                    val fullPath = File(directory, file)
                    ExcelExportService.exportPayrollToExcel(results, fullPath)
                }
            },
            onReset = {
                employees = emptyList()
                summary = null
            },
            onOpenFilePicker = {
                val fileDialog = FileDialog(null as Frame?, "ZKTeco Excel Raporunu Seçin", FileDialog.LOAD)
                fileDialog.isVisible = true
                val file = fileDialog.file
                val directory = fileDialog.directory
                if (file != null) {
                    val fullPath = directory + file
                    try {
                        val result = ZKTecoParser.parseExcel(fullPath, shiftSettings)
                        val detectedDate = result.second.detectedYearMonth
                        if (detectedDate != null) {
                            shiftSettings = shiftSettings.copy(yearMonth = detectedDate)
                        }
                        employees = result.first
                        summary = result.second

                        // Varsayılan personel ayarlarını oluştur
                        result.first.forEach { emp ->
                            if (!payrollSettingsMap.containsKey(emp.id)) {
                                payrollSettingsMap[emp.id] = EmployeePayrollSettings(emp.id, 6500.0)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        )
    }
}
