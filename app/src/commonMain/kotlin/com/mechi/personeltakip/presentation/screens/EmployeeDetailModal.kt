package com.mechi.personeltakip.presentation.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mechi.personeltakip.domain.calculator.CalendarUtils
import com.mechi.personeltakip.domain.calculator.PayrollCalculator
import com.mechi.personeltakip.domain.models.*
import com.mechi.personeltakip.presentation.components.TimePickerDialog
import com.mechi.personeltakip.presentation.components.TimePickerTarget
import com.mechi.personeltakip.presentation.theme.*
import kotlin.math.max

@Composable
fun EmployeeDetailModal(
    employee: EmployeePayrollResult,
    onClose: () -> Unit,
    onUpdateSalary: (String, Double) -> Unit,
    onUpdateAdvance: (String, Double) -> Unit,
    onUpdatePreviousMonthDebt: (String, Double) -> Unit = { _, _ -> },
    onUpdateDayPunch: (String, Int, Boolean, Double) -> Unit,
    onUpdateDayTimes: (String, Int, String?, String?) -> Unit = { _, _, _, _ -> },
    onAddDay: (String, Int) -> Unit = { _, _ -> },
    onDeleteDay: (String, Int) -> Unit = { _, _ -> },
    onToggleOvertime: (String, Int) -> Unit,
    onToggleLateness: (String, Int) -> Unit,
    onToggleEarlyIn: (String, Int) -> Unit,
    onToggleEarlyOut: (String, Int) -> Unit,
    onUpdateAbsenceMultiplier: (String, Int, Double) -> Unit,
    shiftSettings: GlobalShiftSettings
) {
    var salaryInput by remember(employee.id) { mutableStateOf(employee.baseSalary.toString()) }
    var advanceInput by remember(employee.id) { mutableStateOf(employee.advancePayment.toString()) }
    var debtInput by remember(employee.id) { mutableStateOf(employee.previousMonthDebt.toString()) }
    var timePickerTarget by remember { mutableStateOf<TimePickerTarget?>(null) }

    val maxDayInLogs = employee.dailyLogs.keys.maxOrNull() ?: shiftSettings.totalDaysInMonth
    val totalDaysToRender = max(shiftSettings.totalDaysInMonth, maxDayInLogs)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .clickable(enabled = false) { },
            backgroundColor = Slate900,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Slate800),
            elevation = 24.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header (Personel Bilgileri)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate950)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Indigo600.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.DateRange, null, tint = Indigo400)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(employee.name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .background(Indigo600.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("ID: ${employee.id}", color = Indigo400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            "Dönem: ${shiftSettings.yearMonth} • Departman: ${employee.department} • Toplam Çalışma: ${PayrollCalculator.formatHours(employee.totalWorkedHours)} • Yevmiye: ${"%.2f".format(employee.dailyRate)} TL • Saatlik: ${"%.2f".format(employee.hourlyRate)} TL",
                            color = Slate400,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onClose, modifier = Modifier.background(Slate800, CircleShape)) {
                        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }

                // Stats Banner (Maaş, Borç, Avans, Net Tutar)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Taban Maaş
                    Card(
                        modifier = Modifier.weight(1f),
                        backgroundColor = Slate950,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Slate800)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("TABAN MAAŞ (TL)", color = Slate400, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            BasicTextField(
                                value = salaryInput,
                                onValueChange = {
                                    salaryInput = it
                                    it.toDoubleOrNull()?.let { num -> onUpdateSalary(employee.id, num) }
                                },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White),
                                cursorBrush = SolidColor(Emerald400),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Slate900, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Geçen Aydan Kalan Borç
                    Card(
                        modifier = Modifier.weight(1f),
                        backgroundColor = Slate950,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Slate800)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("GEÇEN AYDAN BORÇ (TL)", color = Rose400, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            BasicTextField(
                                value = debtInput,
                                onValueChange = {
                                    debtInput = it
                                    it.toDoubleOrNull()?.let { num -> onUpdatePreviousMonthDebt(employee.id, num) }
                                },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Rose400),
                                cursorBrush = SolidColor(Rose400),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Slate900, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Avans
                    Card(
                        modifier = Modifier.weight(1f),
                        backgroundColor = Slate950,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Slate800)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("AVANS (TL)", color = Amber400, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            BasicTextField(
                                value = advanceInput,
                                onValueChange = {
                                    advanceInput = it
                                    it.toDoubleOrNull()?.let { num -> onUpdateAdvance(employee.id, num) }
                                },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Amber400),
                                cursorBrush = SolidColor(Amber400),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Slate900, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Net Sonuç Göstergesi
                    val isDebt = employee.isDebt
                    val balanceColor = if (isDebt) Rose500 else Emerald500
                    Card(
                        modifier = Modifier.weight(1.3f),
                        backgroundColor = balanceColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, balanceColor.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    if (isDebt) "DEVREDEN BORÇ" else "NET ÖDENECEK",
                                    color = if (isDebt) Rose400 else Emerald400,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    "${if (isDebt) -employee.debtAmount.toInt() else employee.payableAmount.toInt()} TL",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Text(
                                "Mesai: +${employee.overtimePay.toInt()} TL\nKesinti: -${employee.deductions.toInt()} TL",
                                color = Slate400,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Right
                            )
                        }
                    }
                }

                Divider(color = Slate800)

                // Günlük Puantaj Kartları Grid'i
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 175.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(totalDaysToRender) { idx ->
                        val day = idx + 1
                        val punch = employee.dailyLogs[day]
                        val isWeekend = CalendarUtils.isWeekend(shiftSettings.yearMonth, day, shiftSettings.weekendDays)
                        val dayName = CalendarUtils.getDayNameTr(shiftSettings.yearMonth, day)

                        DayCard(
                            day = day,
                            dayName = dayName,
                            punch = punch,
                            isWeekend = isWeekend,
                            defaultAbsenceMult = employee.defaultAbsenceMultiplier,
                            dailyAbsenceMult = employee.dailyAbsenceMultipliers[day],
                            onEditTime = { isCheckIn, curTime ->
                                timePickerTarget = TimePickerTarget(day, isCheckIn, curTime)
                            },
                            onToggleOvertime = { onToggleOvertime(employee.id, day) },
                            onToggleLateness = { onToggleLateness(employee.id, day) },
                            onToggleAbsenceMultiplier = {
                                val current = employee.dailyAbsenceMultipliers[day] ?: employee.defaultAbsenceMultiplier
                                val next = if (current == 1.0) 2.0 else 1.0
                                onUpdateAbsenceMultiplier(employee.id, day, next)
                            },
                            onAddDay = { onAddDay(employee.id, day) },
                            onDeleteDay = { onDeleteDay(employee.id, day) }
                        )
                    }
                }
            }
        }

        // Saat Düzenleme Diyaloğu
        timePickerTarget?.let { target ->
            TimePickerDialog(
                target = target,
                onConfirm = { newTime ->
                    val curPunch = employee.dailyLogs[target.day]
                    val inTime = if (target.isCheckIn) newTime else curPunch?.checkIn
                    val outTime = if (!target.isCheckIn) newTime else curPunch?.checkOut
                    onUpdateDayTimes(employee.id, target.day, inTime, outTime)
                    timePickerTarget = null
                },
                onDismiss = { timePickerTarget = null }
            )
        }
    }
}

@Composable
private fun DayCard(
    day: Int,
    dayName: String,
    punch: DailyPunch?,
    isWeekend: Boolean,
    defaultAbsenceMult: Double,
    dailyAbsenceMult: Double?,
    onEditTime: (isCheckIn: Boolean, currentTime: String?) -> Unit,
    onToggleOvertime: () -> Unit,
    onToggleLateness: () -> Unit,
    onToggleAbsenceMultiplier: () -> Unit,
    onAddDay: () -> Unit,
    onDeleteDay: () -> Unit
) {
    val isPresent = punch != null && punch.status == "present"
    val isAbsent = !isWeekend && (punch == null || punch.status == "absent")
    val effectiveMult = dailyAbsenceMult ?: defaultAbsenceMult

    val cardBg = when {
        isWeekend -> Slate950.copy(alpha = 0.8f)
        isPresent -> Slate850
        else -> Slate950
    }

    val borderColor = when {
        isWeekend -> Amber400.copy(alpha = 0.3f)
        isPresent -> Indigo500.copy(alpha = 0.3f)
        else -> Rose500.copy(alpha = 0.2f)
    }

    Card(
        backgroundColor = cardBg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Gün Başlığı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$day", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(dayName, color = if (isWeekend) Amber400 else Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                if (punch != null) {
                    IconButton(onClick = onDeleteDay, modifier = Modifier.size(18.dp)) {
                        Icon(Icons.Default.Clear, null, tint = Slate600, modifier = Modifier.size(12.dp))
                    }
                } else {
                    IconButton(onClick = onAddDay, modifier = Modifier.size(18.dp)) {
                        Icon(Icons.Default.Add, null, tint = Indigo400, modifier = Modifier.size(12.dp))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (isPresent) {
                // Giriş - Çıkış Saatleri
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    // Giriş
                    Box(
                        modifier = Modifier
                            .background(Slate950, RoundedCornerShape(6.dp))
                            .clickable { onEditTime(true, punch?.checkIn) }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "G: ${punch?.checkIn ?: "--:--"}",
                            color = Emerald400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Çıkış
                    Box(
                        modifier = Modifier
                            .background(Slate950, RoundedCornerShape(6.dp))
                            .clickable { onEditTime(false, punch?.checkOut) }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "Ç: ${punch?.checkOut ?: "--:--"}",
                            color = Indigo400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                // Mesai Durumu (Tıklanabilir)
                if (punch != null && punch.overtimeHours > 0) {
                    val otDisabled = punch.isOvertimeDisabled
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (otDisabled) Slate950 else Amber500.copy(alpha = 0.15f))
                            .clickable { onToggleOvertime() }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "Mesai: +${PayrollCalculator.formatHours(punch.overtimeHours)}${if (otDisabled) " (İptal)" else ""}",
                            color = if (otDisabled) Slate600 else Amber400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Geç Kalma Durumu (Tıklanabilir)
                if (punch != null && punch.latenessHours > 0) {
                    val lateDisabled = punch.isLatenessDisabled
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (lateDisabled) Slate950 else Rose500.copy(alpha = 0.15f))
                            .clickable { onToggleLateness() }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "Geç: -${PayrollCalculator.formatHours(punch.latenessHours)}${if (lateDisabled) " (Muaf)" else ""}",
                            color = if (lateDisabled) Slate600 else Rose400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                punch?.note?.let { note ->
                    Spacer(Modifier.height(4.dp))
                    Text(note, color = Sky500, fontSize = 9.sp, maxLines = 1)
                }

            } else if (isAbsent) {
                // Devamsız Gün (1x veya 2x Kesinti Toggle'ı)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Rose500.copy(alpha = 0.12f))
                        .clickable { onToggleAbsenceMultiplier() }
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DEVAMSIZ", color = Rose400, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Text(
                            "Kesinti: ${effectiveMult}x (${if (effectiveMult == 1.0) "İzinli" else "İzinsiz"})",
                            color = if (effectiveMult == 1.0) Amber400 else Rose500,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (isWeekend) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("HAFTA TATİLİ", color = Amber400.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
