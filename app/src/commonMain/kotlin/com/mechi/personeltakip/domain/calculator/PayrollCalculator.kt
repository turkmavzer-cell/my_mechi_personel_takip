package com.mechi.personeltakip.domain.calculator

import com.mechi.personeltakip.domain.models.*
import kotlin.math.max
import kotlin.math.roundToInt

object PayrollCalculator {

    fun formatHours(decimalHours: Double): String {
        if (decimalHours <= 0) return "0 dk"
        val totalMinutes = (decimalHours * 60).roundToInt()
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60

        return when {
            hours > 0 && mins > 0 -> "${hours} sa ${mins} dk"
            hours > 0 -> "${hours} sa"
            else -> "${mins} dk"
        }
    }

    fun calculatePayroll(
        employees: List<EmployeeAttendance>,
        payrollSettingsMap: Map<String, EmployeePayrollSettings>,
        shiftSettings: GlobalShiftSettings
    ): List<EmployeePayrollResult> {
        return employees.map { emp ->
            val empSettings = payrollSettingsMap[emp.id] ?: EmployeePayrollSettings(
                id = emp.id,
                baseSalary = 6500.0,
                overtimeMultiplierWeekday = 2.0,
                overtimeMultiplierWeekend = 2.0,
                absenceDeductionMultiplier = 2.0
            )

            val baseSalary = empSettings.baseSalary
            val advancePayment = empSettings.advancePayment
            val previousMonthDebt = empSettings.previousMonthDebt

            val maxLogDay = emp.dailyLogs.keys.maxOrNull() ?: shiftSettings.totalDaysInMonth
            val effectiveTotalDays = max(shiftSettings.totalDaysInMonth, maxLogDay)
            val totalDaysInMonthDouble = effectiveTotalDays.toDouble()

            val dailyRate = baseSalary / totalDaysInMonthDouble
            val hourlyRate = dailyRate / shiftSettings.standardDailyHours

            var daysPresent = 0
            var daysAbsent = 0
            var daysWeekendPresent = 0
            var totalWorkedHours = 0.0
            var totalOvertimePayableWeekday = 0.0
            var totalOvertimePayableWeekend = 0.0
            var totalActualOvertimeHours = 0.0
            var totalLatenessMinutes = 0.0
            var totalEarlyOutMinutes = 0.0

            val stdHours = shiftSettings.standardDailyHours.toDouble()
            val globalShiftStartMin = timeToMinutes(shiftSettings.workStartTime)
            var globalShiftEndMin = timeToMinutes(shiftSettings.workEndTime)
            if (globalShiftEndMin < globalShiftStartMin) globalShiftEndMin += 24 * 60

            for (day in 1..effectiveTotalDays) {
                val punch = emp.dailyLogs[day]
                val isWeekend = CalendarUtils.isWeekend(shiftSettings.yearMonth, day, shiftSettings.weekendDays)

                if (punch != null && punch.status == "present") {
                    daysPresent++

                    var worked = punch.workedHours
                    val shiftStartMin = timeToMinutes(shiftSettings.workStartTime)
                    var shiftEndMin = timeToMinutes(shiftSettings.workEndTime)
                    if (shiftEndMin < shiftStartMin) shiftEndMin += 24 * 60

                    if (punch.checkIn != null) {
                        val checkInMin = timeToMinutes(punch.checkIn)
                        if (checkInMin < shiftStartMin && punch.isEarlyInDisabled) {
                            worked -= (shiftStartMin - checkInMin) / 60.0
                        }
                    }

                    totalWorkedHours += worked

                    if (!isWeekend && punch.checkIn != null && !punch.isLatenessDisabled) {
                        val checkInMin = timeToMinutes(punch.checkIn)
                        if (checkInMin > globalShiftStartMin) {
                            totalLatenessMinutes += (checkInMin - globalShiftStartMin)
                        }
                    }

                    if (!isWeekend && punch.checkIn != null && punch.checkOut != null && !punch.isEarlyOutDisabled) {
                        val checkInMin = timeToMinutes(punch.checkIn)
                        var checkOutMin = timeToMinutes(punch.checkOut)
                        if (checkOutMin < checkInMin) checkOutMin += 24 * 60 // Gece vardiyası

                        if (checkOutMin < globalShiftEndMin) {
                            totalEarlyOutMinutes += (globalShiftEndMin - checkOutMin)
                        }
                    }

                    if (isWeekend) {
                        daysWeekendPresent++
                        val weekendMult = empSettings.overtimeMultiplierWeekend
                        val multipliedWeekendHours = if (punch.isOvertimeDisabled) 0.0 else worked * weekendMult
                        totalOvertimePayableWeekend += multipliedWeekendHours
                        totalActualOvertimeHours += multipliedWeekendHours
                    } else {
                        if (worked > stdHours) {
                            val rawOvertime = worked - stdHours
                            val weekdayMult = empSettings.overtimeMultiplierWeekday
                            val multipliedWeekdayHours = if (punch.isOvertimeDisabled) 0.0 else rawOvertime * weekdayMult
                            totalOvertimePayableWeekday += multipliedWeekdayHours
                            totalActualOvertimeHours += multipliedWeekdayHours
                        }
                    }
                } else if (!isWeekend) {
                    daysAbsent++
                }
            }

            val overtimePay = ((totalOvertimePayableWeekday + totalOvertimePayableWeekend) * hourlyRate * 100.0).roundToInt() / 100.0

            val defaultAbsenceMultiplier = empSettings.absenceDeductionMultiplier
            val dailyAbsenceMultipliers = empSettings.dailyAbsenceMultipliers

            var totalAbsenceDeductionUnits = 0.0
            for (day in 1..effectiveTotalDays) {
                val punch = emp.dailyLogs[day]
                val isWeekend = CalendarUtils.isWeekend(shiftSettings.yearMonth, day, shiftSettings.weekendDays)
                val isAbsent = !isWeekend && (punch == null || punch.status == "absent")

                if (isAbsent) {
                    val mult = dailyAbsenceMultipliers[day] ?: defaultAbsenceMultiplier
                    totalAbsenceDeductionUnits += mult
                }
            }

            val absenceDeduction = totalAbsenceDeductionUnits * dailyRate
            val latenessDeduction = (totalLatenessMinutes / 60.0) * hourlyRate
            val earlyOutDeduction = (totalEarlyOutMinutes / 60.0) * hourlyRate
            val deductions = ((absenceDeduction + latenessDeduction + earlyOutDeduction) * 100.0).roundToInt() / 100.0

            val netSalary = ((baseSalary - deductions + overtimePay - advancePayment - previousMonthDebt) * 100.0).roundToInt() / 100.0

            EmployeePayrollResult(
                id = emp.id,
                name = emp.name,
                department = emp.department,
                daysPresent = daysPresent,
                daysAbsent = daysAbsent,
                daysWeekendPresent = daysWeekendPresent,
                totalWorkedHours = (totalWorkedHours * 100.0).roundToInt() / 100.0,
                totalOvertimeHours = (totalActualOvertimeHours * 100.0).roundToInt() / 100.0,
                totalOvertimeHoursWeekday = (totalOvertimePayableWeekday * 100.0).roundToInt() / 100.0,
                totalOvertimeHoursWeekend = (totalOvertimePayableWeekend * 100.0).roundToInt() / 100.0,
                baseSalary = baseSalary,
                advancePayment = advancePayment,
                previousMonthDebt = previousMonthDebt,
                dailyRate = (dailyRate * 100.0).roundToInt() / 100.0,
                hourlyRate = (hourlyRate * 100.0).roundToInt() / 100.0,
                overtimePay = overtimePay,
                deductions = deductions,
                netSalary = netSalary,
                dailyLogs = emp.dailyLogs,
                dailyAbsenceMultipliers = dailyAbsenceMultipliers,
                defaultAbsenceMultiplier = defaultAbsenceMultiplier,
                overtimeMultiplierWeekday = empSettings.overtimeMultiplierWeekday,
                overtimeMultiplierWeekend = empSettings.overtimeMultiplierWeekend
            )
        }
    }

    private fun timeToMinutes(timeStr: String): Int {
        val parts = timeStr.split(":")
        if (parts.size < 2) return 0
        return (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].take(2).toIntOrNull() ?: 0)
    }
}
