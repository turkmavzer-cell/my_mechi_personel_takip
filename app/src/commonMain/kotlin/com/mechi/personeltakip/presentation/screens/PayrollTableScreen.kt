package com.mechi.personeltakip.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mechi.personeltakip.domain.models.*
import com.mechi.personeltakip.presentation.components.KPICards
import com.mechi.personeltakip.presentation.theme.*

@Composable
fun PayrollTableScreen(
    results: List<EmployeePayrollResult>,
    onPrint: (List<EmployeePayrollResult>) -> Unit,
    onExportExcel: (List<EmployeePayrollResult>) -> Unit,
    onSelectEmployee: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDept by remember { mutableStateOf<String?>(null) }

    val departments = remember(results) {
        results.map { it.department }.filter { it.isNotBlank() }.distinct()
    }

    val filteredResults = remember(results, searchQuery, selectedDept) {
        results.filter { res ->
            val matchQuery = searchQuery.isBlank() ||
                    res.name.contains(searchQuery, ignoreCase = true) ||
                    res.id.contains(searchQuery)
            val matchDept = selectedDept == null || res.department.equals(selectedDept, ignoreCase = true)
            matchQuery && matchDept
        }
    }

    Column {
        KPICards(results)
        Spacer(Modifier.height(28.dp))

        Card(
            backgroundColor = Slate900,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Slate800)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Toolbar (Arama, Departman filtreleri, Excel ve Yazdır butonları)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Personel adı veya ID ara...", color = Slate500, fontSize = 13.sp) },
                        modifier = Modifier
                            .width(320.dp)
                            .height(50.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Slate950,
                            textColor = Color.White,
                            focusedIndicatorColor = Indigo500,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Slate500) },
                        singleLine = true
                    )

                    Spacer(Modifier.width(16.dp))

                    // Departman filtre çipleri
                    if (departments.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            DeptFilterChip("Tümü", selectedDept == null) { selectedDept = null }
                            departments.take(3).forEach { dept ->
                                DeptFilterChip(dept, selectedDept == dept) {
                                    selectedDept = if (selectedDept == dept) null else dept
                                }
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // Excel Export Butonu
                    Button(
                        onClick = { onExportExcel(filteredResults) },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF064E3B)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Info, null, tint = Emerald400, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Excel'e Aktar (.xlsx)", color = Emerald400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.width(10.dp))

                    // A4 Yazdır Butonu
                    Button(
                        onClick = { onPrint(filteredResults) },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Indigo600),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Yazdır (A4)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Tablo Başlıkları
                LazyColumn {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate950)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ID", modifier = Modifier.width(40.dp), color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            Text("AD SOYAD", modifier = Modifier.weight(1.3f), color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            Text("DEPARTMAN", modifier = Modifier.weight(0.9f), color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            Text("GELDİĞİ", modifier = Modifier.width(60.dp), color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            Text("DEVAMSIZ", modifier = Modifier.width(60.dp), color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            Text("MAAŞ", modifier = Modifier.width(80.dp), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            Text("DEV.BORÇ", modifier = Modifier.width(75.dp), color = Rose500, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            Text("AVANS", modifier = Modifier.width(75.dp), color = Amber400, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            Text("KESİNTİ", modifier = Modifier.width(75.dp), color = Rose500, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            Text("MESAİ", modifier = Modifier.width(75.dp), color = Amber500, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            Text("ALACAĞI / BORCU", modifier = Modifier.width(120.dp), color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Right)
                            Text("DETAY", modifier = Modifier.width(45.dp), color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                        }
                    }

                    if (filteredResults.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Arama kriterlerine uygun personel kaydı bulunamadı.", color = Slate500, fontSize = 13.sp)
                            }
                        }
                    }

                    items(filteredResults) { res ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectEmployee(res.id) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(res.id, modifier = Modifier.width(40.dp), color = Indigo400, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(res.name, modifier = Modifier.weight(1.3f), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(res.department, modifier = Modifier.weight(0.9f), color = Slate400, fontSize = 11.sp)
                            Text("${res.daysPresent} G", modifier = Modifier.width(60.dp), color = Emerald400, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("${res.daysAbsent} G", modifier = Modifier.width(60.dp), color = Rose500, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("${res.baseSalary.toInt()} TL", modifier = Modifier.width(80.dp), color = Color.White, fontSize = 11.sp)
                            Text("-${res.previousMonthDebt.toInt()} TL", modifier = Modifier.width(75.dp), color = Rose500, fontSize = 11.sp)
                            Text("-${res.advancePayment.toInt()} TL", modifier = Modifier.width(75.dp), color = Amber400, fontSize = 11.sp)
                            Text("-${res.deductions.toInt()} TL", modifier = Modifier.width(75.dp), color = Rose500, fontSize = 11.sp)
                            Text("+${res.overtimePay.toInt()} TL", modifier = Modifier.width(75.dp), color = Amber500, fontWeight = FontWeight.Bold, fontSize = 11.sp)

                            val isDebt = res.isDebt
                            val balanceText = if (isDebt) "-${res.debtAmount.toInt()} TL" else "+${res.payableAmount.toInt()} TL"
                            val balanceColor = if (isDebt) Rose500 else Emerald400
                            Column(modifier = Modifier.width(120.dp), horizontalAlignment = Alignment.End) {
                                Text(balanceText, color = balanceColor, fontWeight = FontWeight.Black, fontSize = 13.sp, textAlign = TextAlign.Right)
                                Text(
                                    if (isDebt) "Devreden Borç" else "Net Alacağı",
                                    color = balanceColor.copy(alpha = 0.8f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Right
                                )
                            }

                            IconButton(onClick = { onSelectEmployee(res.id) }, modifier = Modifier.width(45.dp)) {
                                Icon(Icons.Default.Info, null, tint = Slate600, modifier = Modifier.size(18.dp))
                            }
                        }
                        Divider(color = Slate800, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeptFilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) Indigo600.copy(alpha = 0.25f) else Slate950,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            color = if (isSelected) Indigo400 else Slate400,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
