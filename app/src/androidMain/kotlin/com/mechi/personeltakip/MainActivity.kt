package com.mechi.personeltakip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mechi.personeltakip.domain.models.*
import com.mechi.personeltakip.presentation.App
import com.mechi.personeltakip.presentation.theme.Slate950

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var employees by remember { mutableStateOf<List<EmployeeAttendance>>(emptyList()) }
            var summary by remember { mutableStateOf<ParsingSummary?>(null) }
            var shiftSettings by remember { mutableStateOf(GlobalShiftSettings()) }
            val payrollSettingsMap = remember { mutableStateMapOf<String, EmployeePayrollSettings>() }

            Surface(modifier = Modifier.fillMaxSize(), color = Slate950) {
                App(
                    employees = employees,
                    summary = summary,
                    shiftSettings = shiftSettings,
                    payrollSettingsMap = payrollSettingsMap.toMap(),
                    onShiftSettingsChange = { shiftSettings = it },
                    onUpdatePayrollSettings = { id, s -> payrollSettingsMap[id] = s },
                    onUpdateDayPunch = { id, day, isPresent, hours ->
                        employees = employees.map { emp ->
                            if (emp.id == id) {
                                val current = emp.dailyLogs[day]
                                val newPunch = (current ?: DailyPunch(day, "", null, null, emptyList(), 0.0, 0.0, 0.0, false, "absent")).copy(
                                    status = if (isPresent) "present" else "absent",
                                    workedHours = hours,
                                    overtimeHours = if (isPresent) kotlin.math.max(0.0, hours - shiftSettings.standardDailyHours) else 0.0,
                                    note = "Manuel Düzenleme"
                                )
                                val map = emp.dailyLogs.toMutableMap()
                                map[day] = newPunch
                                emp.copy(dailyLogs = map.toMap())
                            } else emp
                        }
                    },
                    onUpdateDayTimes = { id, day, inTime, outTime ->
                        // Android update time
                    },
                    onAddDay = { id, day -> },
                    onDeleteDay = { id, day -> },
                    onToggleOvertime = { id, day ->
                        employees = employees.map { emp ->
                            if (emp.id == id) {
                                emp.dailyLogs[day]?.let { p ->
                                    val map = emp.dailyLogs.toMutableMap()
                                    map[day] = p.copy(isOvertimeDisabled = !p.isOvertimeDisabled)
                                    emp.copy(dailyLogs = map.toMap())
                                } ?: emp
                            } else emp
                        }
                    },
                    onToggleLateness = { id, day ->
                        employees = employees.map { emp ->
                            if (emp.id == id) {
                                emp.dailyLogs[day]?.let { p ->
                                    val map = emp.dailyLogs.toMutableMap()
                                    map[day] = p.copy(isLatenessDisabled = !p.isLatenessDisabled)
                                    emp.copy(dailyLogs = map.toMap())
                                } ?: emp
                            } else emp
                        }
                    },
                    onToggleEarlyIn = { id, day ->
                        employees = employees.map { emp ->
                            if (emp.id == id) {
                                emp.dailyLogs[day]?.let { p ->
                                    val map = emp.dailyLogs.toMutableMap()
                                    map[day] = p.copy(isEarlyInDisabled = !p.isEarlyInDisabled)
                                    emp.copy(dailyLogs = map.toMap())
                                } ?: emp
                            } else emp
                        }
                    },
                    onToggleEarlyOut = { id, day ->
                        employees = employees.map { emp ->
                            if (emp.id == id) {
                                emp.dailyLogs[day]?.let { p ->
                                    val map = emp.dailyLogs.toMutableMap()
                                    map[day] = p.copy(isEarlyOutDisabled = !p.isEarlyOutDisabled)
                                    emp.copy(dailyLogs = map.toMap())
                                } ?: emp
                            } else emp
                        }
                    },
                    onUpdateAbsenceMultiplier = { id, day, mult ->
                        val cur = payrollSettingsMap[id] ?: EmployeePayrollSettings(id, 6500.0)
                        val map = cur.dailyAbsenceMultipliers.toMutableMap()
                        map[day] = mult
                        payrollSettingsMap[id] = cur.copy(dailyAbsenceMultipliers = map.toMap())
                    },
                    onPrint = { },
                    onExportExcel = { },
                    onReset = {
                        employees = emptyList()
                        summary = null
                    },
                    onOpenFilePicker = { }
                )
            }
        }
    }
}
