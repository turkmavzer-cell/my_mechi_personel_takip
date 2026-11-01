package com.mechi.personeltakip.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mechi.personeltakip.domain.models.ParsingSummary
import com.mechi.personeltakip.presentation.components.KPICards
import com.mechi.personeltakip.presentation.theme.*

@Composable
fun UploadScreen(
    summary: ParsingSummary?,
    onReset: () -> Unit,
    onOpenFilePicker: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        KPICards(emptyList())
        Spacer(Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            backgroundColor = Slate900.copy(alpha = 0.6f),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, Brush.linearGradient(listOf(Slate800, Indigo500.copy(alpha = 0.25f))))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(Slate800, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (summary != null) Icons.Default.CheckCircle else Icons.Default.AddCircle,
                        null,
                        tint = if (summary != null) Emerald400 else Indigo500,
                        modifier = Modifier.size(42.dp)
                    )
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    if (summary != null) "Excel Raporu Başarıyla İşlendi" else "ZKTeco Excel Raporunu Seçin",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Att.log report sayfasını içeren standart ZKTeco parmak izi matris raporunu yükleyin",
                    color = Slate400,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(32.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = onOpenFilePicker,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Indigo600),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(50.dp)
                            .padding(horizontal = 24.dp)
                    ) {
                        Text(
                            if (summary != null) "BAŞKA DOSYA YÜKLE" else "DOSYA SEÇ (.XLS / .XLSX)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (summary != null) {
                        Button(
                            onClick = onReset,
                            colors = ButtonDefaults.buttonColors(backgroundColor = Slate800),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .padding(horizontal = 24.dp)
                        ) {
                            Icon(Icons.Default.Refresh, null, tint = Rose500, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("SİSTEMİ SIFIRLA", color = Rose500, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                summary?.let {
                    Spacer(Modifier.height(28.dp))
                    Card(
                        backgroundColor = Slate950,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Slate800)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Dosya: ${it.fileName}", color = Emerald400, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(Modifier.width(16.dp))
                            Text("Personel: ${it.totalEmployeesFound}", color = Slate300, fontSize = 12.sp)
                            Spacer(Modifier.width(16.dp))
                            Text("Gün Sayısı: ${it.totalDaysDetected}", color = Slate300, fontSize = 12.sp)
                            Spacer(Modifier.width(16.dp))
                            Text("Dönem: ${it.detectedYearMonth ?: it.dateRange}", color = Indigo400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
