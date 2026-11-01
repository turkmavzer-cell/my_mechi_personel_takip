package com.mechi.personeltakip.domain.models

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class DailyPunch(
    val day: Int,
    val dateStr: String,
    val checkIn: String?,
    val checkOut: String?,
    val allPunches: List<String>,
    val workedHours: Double,
    val overtimeHours: Double,
    val latenessHours: Double,
    val isWeekend: Boolean,
    val status: String, // "present", "absent", "weekend", "leave"
    val isAutoCorrected: Boolean = false,
    val note: String? = null,
    val isOvertimeDisabled: Boolean = false,
    val isLatenessDisabled: Boolean = false,
    val isEarlyInDisabled: Boolean = false,
    val isEarlyOutDisabled: Boolean = false
)

@Immutable
@Serializable
data class EmployeeAttendance(
    val id: String,
    val name: String,
    val department: String,
    val dailyLogs: Map<Int, DailyPunch>,
    val daysPresent: Int,
    val daysAbsent: Int,
    val daysWeekendPresent: Int,
    val totalWorkedHours: Double,
    val totalOvertimeHoursWeekday: Double,
    val totalOvertimeHoursWeekend: Double,
    val totalLatenessHours: Double
)

@Serializable
data class ParsingSummary(
    val fileName: String,
    val dateRange: String,
    val totalEmployeesFound: Int,
    val totalDaysDetected: Int,
    val rawSheetNames: List<String>,
    val activeSheet: String,
    val detectedYearMonth: String? = null
)
