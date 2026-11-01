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
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mechi.personeltakip.domain.calculator.PayrollCalculator
import com.mechi.personeltakip.domain.models.*
import com.mechi.personeltakip.presentation.components.KPICards
import com.mechi.personeltakip.presentation.theme.*

@Composable
fun EmployeeOverviewScreen(
    results: List<EmployeePayrollResult>,
    onSelectEmployee: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredResults = remember(results, searchQuery) {
        if (searchQuery.isBlank()) results
        else results.filter {
            it.name.contains(searchQuery, ignoreCase = true) || it.id.contains(searchQuery)
        }
    }

    Column {
        KPICards(results)
        Spacer(Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Personel Bazlı Puantaj Takvimi İnceleme", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    "İncelemek istediğiniz personele tıklayarak ay içi tüm saat damgalarını, mesai/kesinti durumlarını ve kartlarını yönetin.",
                    color = Slate400,
                    fontSize = 12.sp
                )
            }

            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Personel Ara...", color = Slate500, fontSize = 12.sp) },
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Slate900,
                    textColor = Color.White,
                    focusedIndicatorColor = Indigo500,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.width(260.dp).height(46.dp),
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Slate500, modifier = Modifier.size(16.dp)) }
            )
        }

        Spacer(Modifier.height(20.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(filteredResults.chunked(3)) { rowItems ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    rowItems.forEach { res ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSelectEmployee(res.id) },
                            backgroundColor = Slate900,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Slate800)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("ID: ${res.id}", color = Indigo400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(res.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                                        Text(res.department, color = Slate400, fontSize = 11.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(Emerald500.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text("${res.daysPresent} Gün Geldi", color = Emerald400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(Modifier.height(14.dp))
                                Divider(color = Slate800)
                                Spacer(Modifier.height(14.dp))
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Mesai: +${PayrollCalculator.formatHours(res.totalOvertimeHours)}", color = Amber400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    val isDebt = res.isDebt
                                    val balanceText = if (isDebt) "Borç: -${res.debtAmount.toInt()} TL" else "Alacak: +${res.payableAmount.toInt()} TL"
                                    val balanceColor = if (isDebt) Rose500 else Emerald400
                                    Text(balanceText, color = balanceColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    if (rowItems.size < 3) {
                        Spacer(Modifier.weight((3 - rowItems.size).toFloat()))
                    }
                }
            }
        }
    }
}
