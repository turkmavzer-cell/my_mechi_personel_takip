package com.mechi.personeltakip.domain.models

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
data class EmployeePayrollSettings(
    val id: String,
    val baseSalary: Double,
    val advancePayment: Double = 0.0,
    val previousMonthDebt: Double = 0.0,
    val overtimeMultiplierWeekday: Double = 2.0,
    val overtimeMultiplierWeekend: Double = 2.0,
    val absenceDeductionMultiplier: Double = 2.0,
    val dailyAbsenceMultipliers: Map<Int, Double> = emptyMap()
)

@Serializable
data class GlobalShiftSettings(
    val standardDailyHours: Int = 9,
    val workStartTime: String = "09:00",
    val workEndTime: String = "18:00",
    val workDaysPerWeek: Int = 6,
    val weekendDays: List<Int> = listOf(5), // 5 = Cuma (Vardiya tatili)
    val yearMonth: String = "2026-07",
    val totalDaysInMonth: Int = 30
)

@Immutable
@Serializable
data class EmployeePayrollResult(
    val id: String,
    val name: String,
    val department: String,
    val daysPresent: Int,
    val daysAbsent: Int,
    val daysWeekendPresent: Int,
    val totalWorkedHours: Double,
    val totalOvertimeHours: Double,
    val totalOvertimeHoursWeekday: Double,
    val totalOvertimeHoursWeekend: Double,
    val baseSalary: Double,
    val advancePayment: Double,
    val previousMonthDebt: Double = 0.0,
    val dailyRate: Double,
    val hourlyRate: Double,
    val overtimePay: Double,
    val deductions: Double,
    val netSalary: Double,
    val dailyLogs: Map<Int, DailyPunch>,
    val dailyAbsenceMultipliers: Map<Int, Double> = emptyMap(),
    val defaultAbsenceMultiplier: Double = 2.0,
    val overtimeMultiplierWeekday: Double = 2.0,
    val overtimeMultiplierWeekend: Double = 2.0
)

val EmployeePayrollResult.payableNetSalary: Double
    get() = if (netSalary > 0) netSalary else 0.0

val EmployeePayrollResult.advanceExcess: Double
    get() = if (netSalary < 0) -netSalary else 0.0

val EmployeePayrollResult.isDebt: Boolean
    get() = netSalary < 0

val EmployeePayrollResult.payableAmount: Double
    get() = if (netSalary >= 0) netSalary else 0.0

val EmployeePayrollResult.debtAmount: Double
    get() = if (netSalary < 0) -netSalary else 0.0
