package com.mechi.personeltakip.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mechi.personeltakip.domain.calculator.PayrollCalculator
import com.mechi.personeltakip.domain.models.*
import com.mechi.personeltakip.presentation.components.PremiumNavBar
import com.mechi.personeltakip.presentation.screens.*
import com.mechi.personeltakip.presentation.theme.*

@Composable
fun App(
    employees: List<EmployeeAttendance>,
    summary: ParsingSummary?,
    shiftSettings: GlobalShiftSettings,
    payrollSettingsMap: Map<String, EmployeePayrollSettings>,
    onShiftSettingsChange: (GlobalShiftSettings) -> Unit,
    onUpdatePayrollSettings: (String, EmployeePayrollSettings) -> Unit,
    onUpdateDayPunch: (String, Int, Boolean, Double) -> Unit,
    onUpdateDayTimes: (String, Int, String?, String?) -> Unit = { _, _, _, _ -> },
    onUpdatePreviousMonthDebt: (String, Double) -> Unit = { _, _ -> },
    onAddDay: (String, Int) -> Unit = { _, _ -> },
    onDeleteDay: (String, Int) -> Unit = { _, _ -> },
    onToggleOvertime: (String, Int) -> Unit,
    onToggleLateness: (String, Int) -> Unit,
    onToggleEarlyIn: (String, Int) -> Unit,
    onToggleEarlyOut: (String, Int) -> Unit,
    onUpdateAbsenceMultiplier: (String, Int, Double) -> Unit,
    onPrint: (List<EmployeePayrollResult>) -> Unit,
    onExportExcel: (List<EmployeePayrollResult>) -> Unit,
    onReset: () -> Unit,
    onOpenFilePicker: () -> Unit
) {
    var activeTab by remember { mutableStateOf("upload") }
    var selectedEmpId by remember { mutableStateOf<String?>(null) }

    val payrollResults = remember(employees, payrollSettingsMap, shiftSettings) {
        PayrollCalculator.calculatePayroll(employees, payrollSettingsMap, shiftSettings)
    }

    MaterialTheme(colors = AppDarkColors) {
        Box(modifier = Modifier.fillMaxSize().background(Slate950)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Navbar
                PremiumNavBar(
                    activeTab = activeTab,
                    shiftSettings = shiftSettings,
                    onTabSelected = { activeTab = it }
                )

                // Ana İçerik
                Box(modifier = Modifier.fillMaxSize().padding(28.dp)) {
                    when (activeTab) {
                        "upload" -> UploadScreen(
                            summary = summary,
                            onReset = onReset,
                            onOpenFilePicker = onOpenFilePicker
                        )
                        "payroll" -> PayrollTableScreen(
                            results = payrollResults,
                            onPrint = onPrint,
                            onExportExcel = onExportExcel,
                            onSelectEmployee = { selectedEmpId = it }
                        )
                        "settings" -> SettingsScreen(
                            shiftSettings = shiftSettings,
                            onShiftSettingsChange = onShiftSettingsChange,
                            employees = employees,
                            payrollSettingsMap = payrollSettingsMap,
                            onUpdatePayrollSettings = onUpdatePayrollSettings
                        )
                        "detail" -> EmployeeOverviewScreen(
                            results = payrollResults,
                            onSelectEmployee = { selectedEmpId = it }
                        )
                    }
                }
            }

            // Personel Detay Modalı
            selectedEmpId?.let { id ->
                val empResult = payrollResults.find { it.id == id }
                empResult?.let {
                    EmployeeDetailModal(
                        employee = it,
                        onClose = { selectedEmpId = null },
                        onUpdateSalary = { eid, salary ->
                            val current = payrollSettingsMap[eid] ?: EmployeePayrollSettings(eid, salary)
                            onUpdatePayrollSettings(eid, current.copy(baseSalary = salary))
                        },
                        onUpdateAdvance = { eid, advance ->
                            val current = payrollSettingsMap[eid] ?: EmployeePayrollSettings(eid, 6500.0)
                            onUpdatePayrollSettings(eid, current.copy(advancePayment = advance))
                        },
                        onUpdatePreviousMonthDebt = { eid, debt ->
                            val current = payrollSettingsMap[eid] ?: EmployeePayrollSettings(eid, 6500.0)
                            onUpdatePayrollSettings(eid, current.copy(previousMonthDebt = debt))
                        },
                        onUpdateDayPunch = onUpdateDayPunch,
                        onUpdateDayTimes = onUpdateDayTimes,
                        onAddDay = onAddDay,
                        onDeleteDay = onDeleteDay,
                        onToggleOvertime = onToggleOvertime,
                        onToggleLateness = onToggleLateness,
                        onToggleEarlyIn = onToggleEarlyIn,
                        onToggleEarlyOut = onToggleEarlyOut,
                        onUpdateAbsenceMultiplier = onUpdateAbsenceMultiplier,
                        shiftSettings = shiftSettings
                    )
                }
            }
        }
    }
}
