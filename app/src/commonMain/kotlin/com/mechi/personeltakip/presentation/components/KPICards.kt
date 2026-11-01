package com.mechi.personeltakip.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mechi.personeltakip.domain.calculator.PayrollCalculator
import com.mechi.personeltakip.domain.models.EmployeePayrollResult
import com.mechi.personeltakip.domain.models.debtAmount
import com.mechi.personeltakip.domain.models.isDebt
import com.mechi.personeltakip.domain.models.payableAmount
import com.mechi.personeltakip.presentation.theme.*

@Composable
fun KPICards(results: List<EmployeePayrollResult>) {
    val totalNetPayable = results.sumOf { it.payableAmount }
    val totalOvertime = results.sumOf { it.totalOvertimeHours }
    val totalAbsent = results.sumOf { it.daysAbsent }
    val excessDebtCount = results.count { it.isDebt }
    val totalExcessDebt = results.sumOf { it.debtAmount }

    val subNetText = if (excessDebtCount > 0) {
        "Dağıtılacak ($excessDebtCount Pers. -${totalExcessDebt.toInt()} TL Borçlu)"
    } else {
        "Dağıtılacak Toplam Nakit/Banka Tutar"
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        KPICard("İŞLENEN PERSONEL", "${results.size}", "ZKTeco Matris Kaydı", Icons.Default.AccountBox, Indigo600, Modifier.weight(1f))
        KPICard("TOPLAM NET BORDRO", "${totalNetPayable.toInt()} TL", subNetText, Icons.Default.Info, Emerald500, Modifier.weight(1f))
        KPICard("TOPLAM FAZLA MESAİ", PayrollCalculator.formatHours(totalOvertime), "Çalışılan: ${results.sumOf { it.totalWorkedHours }.toInt()} Sa", Icons.Default.Info, Amber500, Modifier.weight(1f))
        KPICard("TOPLAM DEVAMSIZ GÜN", "$totalAbsent Gün", "Giriş Kaydı Bulunmayan", Icons.Default.Warning, Rose500, Modifier.weight(1f))
    }
}

@Composable
fun KPICard(title: String, value: String, subValue: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        backgroundColor = Slate900,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Slate800)
    ) {
        Row(modifier = Modifier.padding(22.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(Modifier.height(8.dp))
                Text(value, color = if (color == Emerald500) Emerald400 else Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(4.dp))
                Text(subValue, color = Slate500, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(color.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
        }
    }
}
