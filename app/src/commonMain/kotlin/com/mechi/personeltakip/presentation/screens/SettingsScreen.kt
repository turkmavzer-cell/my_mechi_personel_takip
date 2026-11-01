package com.mechi.personeltakip.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mechi.personeltakip.domain.models.*
import com.mechi.personeltakip.presentation.theme.*

@Composable
fun SettingsScreen(
    shiftSettings: GlobalShiftSettings,
    onShiftSettingsChange: (GlobalShiftSettings) -> Unit,
    employees: List<EmployeeAttendance>,
    payrollSettingsMap: Map<String, EmployeePayrollSettings>,
    onUpdatePayrollSettings: (String, EmployeePayrollSettings) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val firstEmp = employees.firstOrNull()?.id
    val currentGlobalAbsenceMult = payrollSettingsMap[firstEmp]?.absenceDeductionMultiplier ?: 2.0
    val currentGlobalWeekdayMult = payrollSettingsMap[firstEmp]?.overtimeMultiplierWeekday ?: 2.0
    val currentGlobalWeekendMult = payrollSettingsMap[firstEmp]?.overtimeMultiplierWeekend ?: 2.0

    val filteredEmployees = remember(employees, searchQuery) {
        employees.filter {
            it.name.contains(searchQuery, ignoreCase = true) || it.id.contains(searchQuery)
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Sistem Yapılandırması", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Text("Vardiya saatlerini, mesai oranlarını ve genel maaş politikalarını yönetin.", color = Slate400, fontSize = 13.sp)
            Spacer(Modifier.height(28.dp))

            Card(
                backgroundColor = Slate900,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Slate800)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, null, tint = Indigo400, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Genel Ayarlar ve Çarpanlar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(Modifier.height(20.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Vardiya Saati
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Günlük Standart Çalışma Saati", color = Slate400, fontSize = 12.sp)
                            Spacer(Modifier.height(8.dp))
                            TextField(
                                value = shiftSettings.standardDailyHours.toString(),
                                onValueChange = {
                                    val num = it.toIntOrNull() ?: 9
                                    onShiftSettingsChange(shiftSettings.copy(standardDailyHours = num))
                                },
                                colors = TextFieldDefaults.textFieldColors(
                                    backgroundColor = Slate950,
                                    textColor = Color.White,
                                    focusedIndicatorColor = Indigo500
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Kesinti Çarpanı (Gelinmeyen Gün)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Gelinmeyen Gün Kesintisi", color = Rose400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            TextField(
                                value = currentGlobalAbsenceMult.toString(),
                                onValueChange = {
                                    val num = it.toDoubleOrNull() ?: 2.0
                                    employees.forEach { e ->
                                        val cur = payrollSettingsMap[e.id] ?: EmployeePayrollSettings(e.id, 6500.0)
                                        onUpdatePayrollSettings(e.id, cur.copy(absenceDeductionMultiplier = num))
                                    }
                                },
                                colors = TextFieldDefaults.textFieldColors(
                                    backgroundColor = Slate950,
                                    textColor = Rose400,
                                    focusedIndicatorColor = Rose500
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = { Text("x", color = Rose400, fontWeight = FontWeight.Bold) }
                            )
                        }

                        // Hafta İçi Mesai Çarpanı
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hafta İçi Mesai Çarpanı", color = Indigo400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            TextField(
                                value = currentGlobalWeekdayMult.toString(),
                                onValueChange = {
                                    val num = it.toDoubleOrNull() ?: 2.0
                                    employees.forEach { e ->
                                        val cur = payrollSettingsMap[e.id] ?: EmployeePayrollSettings(e.id, 6500.0)
                                        onUpdatePayrollSettings(e.id, cur.copy(overtimeMultiplierWeekday = num))
                                    }
                                },
                                colors = TextFieldDefaults.textFieldColors(
                                    backgroundColor = Slate950,
                                    textColor = Indigo400,
                                    focusedIndicatorColor = Indigo500
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = { Text("x", color = Indigo400, fontWeight = FontWeight.Bold) }
                            )
                        }

                        // Cuma (Hafta Sonu) Çarpanı
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Cuma (Hafta Sonu) Çarpanı", color = Amber400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            TextField(
                                value = currentGlobalWeekendMult.toString(),
                                onValueChange = {
                                    val num = it.toDoubleOrNull() ?: 2.0
                                    employees.forEach { e ->
                                        val cur = payrollSettingsMap[e.id] ?: EmployeePayrollSettings(e.id, 6500.0)
                                        onUpdatePayrollSettings(e.id, cur.copy(overtimeMultiplierWeekend = num))
                                    }
                                },
                                colors = TextFieldDefaults.textFieldColors(
                                    backgroundColor = Slate950,
                                    textColor = Amber400,
                                    focusedIndicatorColor = Amber400
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = { Text("x", color = Amber400, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(36.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Personel Maaş & Avans Yönetimi", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("${employees.size} aktif personel kaydı listeleniyor.", color = Slate400, fontSize = 12.sp)
                }

                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Personel Ara...", color = Slate500, fontSize = 12.sp) },
                    singleLine = true,
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Slate900,
                        textColor = Color.White,
                        focusedIndicatorColor = Indigo500
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.width(260.dp).height(46.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Slate500, modifier = Modifier.size(16.dp)) }
                )
            }

            Spacer(Modifier.height(16.dp))
        }

        items(filteredEmployees) { emp ->
            val settings = payrollSettingsMap[emp.id] ?: EmployeePayrollSettings(emp.id, 6500.0)
            Card(
                backgroundColor = Slate900,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Slate800),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.width(180.dp)) {
                        Text(emp.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("ID: ${emp.id} • ${emp.department}", color = Slate400, fontSize = 11.sp)
                    }

                    Spacer(Modifier.width(16.dp))

                    // Taban Maaş
                    Column(modifier = Modifier.width(140.dp)) {
                        Text("Taban Maaş (TL)", color = Slate400, fontSize = 10.sp)
                        TextField(
                            value = settings.baseSalary.toString(),
                            onValueChange = {
                                val s = it.toDoubleOrNull() ?: settings.baseSalary
                                onUpdatePayrollSettings(emp.id, settings.copy(baseSalary = s))
                            },
                            colors = TextFieldDefaults.textFieldColors(backgroundColor = Slate950, textColor = Color.White),
                            shape = RoundedCornerShape(6.dp),
                            singleLine = true
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    // Avans
                    Column(modifier = Modifier.width(130.dp)) {
                        Text("Avans (TL)", color = Amber400, fontSize = 10.sp)
                        TextField(
                            value = settings.advancePayment.toString(),
                            onValueChange = {
                                val a = it.toDoubleOrNull() ?: settings.advancePayment
                                onUpdatePayrollSettings(emp.id, settings.copy(advancePayment = a))
                            },
                            colors = TextFieldDefaults.textFieldColors(backgroundColor = Slate950, textColor = Amber400),
                            shape = RoundedCornerShape(6.dp),
                            singleLine = true
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    // Devreden Borç
                    Column(modifier = Modifier.width(130.dp)) {
                        Text("Geçen Ay Borç (TL)", color = Rose500, fontSize = 10.sp)
                        TextField(
                            value = settings.previousMonthDebt.toString(),
                            onValueChange = {
                                val d = it.toDoubleOrNull() ?: settings.previousMonthDebt
                                onUpdatePayrollSettings(emp.id, settings.copy(previousMonthDebt = d))
                            },
                            colors = TextFieldDefaults.textFieldColors(backgroundColor = Slate950, textColor = Rose500),
                            shape = RoundedCornerShape(6.dp),
                            singleLine = true
                        )
                    }
                }
            }
        }
    }
}
