package com.mechi.personeltakip.domain.calculator

import com.mechi.personeltakip.domain.models.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PayrollCalculatorTest {

    @Test
    fun testBaseSalaryAndDailyRate() {
        val shiftSettings = GlobalShiftSettings(standardDailyHours = 9, totalDaysInMonth = 30)
        val emp = EmployeeAttendance(
            id = "1",
            name = "Ahmet Yılmaz",
            department = "Üretim",
            dailyLogs = emptyMap(),
            daysPresent = 0,
            daysAbsent = 30,
            daysWeekendPresent = 0,
            totalWorkedHours = 0.0,
            totalOvertimeHoursWeekday = 0.0,
            totalOvertimeHoursWeekend = 0.0,
            totalLatenessHours = 0.0
        )
        val settingsMap = mapOf(
            "1" to EmployeePayrollSettings(id = "1", baseSalary = 30000.0, advancePayment = 0.0)
        )

        val results = PayrollCalculator.calculatePayroll(listOf(emp), settingsMap, shiftSettings)
        val res = results.first()

        assertEquals(1000.0, res.dailyRate, 0.01)
        assertEquals(111.11, res.hourlyRate, 0.01)
    }

    @Test
    fun testAbsenceMultiplierToggle() {
        val shiftSettings = GlobalShiftSettings(
            standardDailyHours = 9,
            totalDaysInMonth = 30,
            yearMonth = "2026-07",
            weekendDays = listOf(5)
        )
        val emp = EmployeeAttendance(
            id = "2",
            name = "Mehmet Demir",
            department = "Depo",
            dailyLogs = emptyMap(),
            daysPresent = 0,
            daysAbsent = 26,
            daysWeekendPresent = 0,
            totalWorkedHours = 0.0,
            totalOvertimeHoursWeekday = 0.0,
            totalOvertimeHoursWeekend = 0.0,
            totalLatenessHours = 0.0
        )

        // Gün 1 izinli (1x), diğer günler 2x
        val settingsMap = mapOf(
            "2" to EmployeePayrollSettings(
                id = "2",
                baseSalary = 30000.0,
                dailyAbsenceMultipliers = mapOf(1 to 1.0)
            )
        )

        val results = PayrollCalculator.calculatePayroll(listOf(emp), settingsMap, shiftSettings)
        val res = results.first()
        assertTrue(res.deductions > 0)
    }

    @Test
    fun testOvertimeCalculation() {
        val shiftSettings = GlobalShiftSettings(
            standardDailyHours = 9,
            totalDaysInMonth = 30,
            yearMonth = "2026-07",
            weekendDays = listOf(5)
        )

        // Gün 2: Hafta içi, 11 saat çalışmış (2 saat mesai)
        val punchDay2 = DailyPunch(
            day = 2,
            dateStr = "2026-07-02",
            checkIn = "09:00",
            checkOut = "20:00",
            allPunches = listOf("09:00", "20:00"),
            workedHours = 11.0,
            overtimeHours = 2.0,
            latenessHours = 0.0,
            isWeekend = false,
            status = "present"
        )

        val emp = EmployeeAttendance(
            id = "3",
            name = "Ali Veli",
            department = "Baskı",
            dailyLogs = mapOf(2 to punchDay2),
            daysPresent = 1,
            daysAbsent = 25,
            daysWeekendPresent = 0,
            totalWorkedHours = 11.0,
            totalOvertimeHoursWeekday = 2.0,
            totalOvertimeHoursWeekend = 0.0,
            totalLatenessHours = 0.0
        )

        val settingsMap = mapOf(
            "3" to EmployeePayrollSettings(id = "3", baseSalary = 30000.0)
        )

        val results = PayrollCalculator.calculatePayroll(listOf(emp), settingsMap, shiftSettings)
        val res = results.first()

        // 2 saat * 2.0x = 4 saatlik ücret mesai payı
        assertTrue(res.overtimePay > 0)
    }
}
