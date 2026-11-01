package com.mechi.personeltakip.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mechi.personeltakip.domain.calculator.CalendarUtils
import com.mechi.personeltakip.domain.models.GlobalShiftSettings
import com.mechi.personeltakip.presentation.theme.*

@Composable
fun PremiumNavBar(
    activeTab: String,
    shiftSettings: GlobalShiftSettings,
    onTabSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Slate900)
            .padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(Indigo600, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Menu, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Mechi Puantaj", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Indigo500.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    val displayMonth = CalendarUtils.getMonthNameTr(shiftSettings.yearMonth)
                    Text(
                        "DÖNEM: $displayMonth",
                        color = Indigo400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Text("Personel Takip, Mesai & Bordro Otomasyonu", color = Slate400, fontSize = 11.sp)
        }

        Spacer(Modifier.weight(1f))

        PremiumNavItem("1. Excel Yükleme & Parser", activeTab == "upload", Icons.Default.Add) { onTabSelected("upload") }
        PremiumNavItem("2. Parametrik Ayarlar", activeTab == "settings", Icons.Default.Settings) { onTabSelected("settings") }
        PremiumNavItem("3. Bordro & Puantaj Tablosu", activeTab == "payroll", Icons.AutoMirrored.Filled.List, badge = "Hazır") { onTabSelected("payroll") }
        PremiumNavItem("4. Personel Detay İnceleme", activeTab == "detail", Icons.Default.AccountCircle) { onTabSelected("detail") }
    }
}

@Composable
fun PremiumNavItem(label: String, isActive: Boolean, icon: ImageVector, badge: String? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) Indigo600.copy(alpha = 0.16f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (isActive) Indigo400 else Slate400, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            color = if (isActive) Color.White else Slate400,
            fontSize = 13.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
        )
        if (badge != null) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .background(Emerald500.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(badge, color = Emerald400, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
