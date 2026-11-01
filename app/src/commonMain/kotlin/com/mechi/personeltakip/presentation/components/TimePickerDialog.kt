package com.mechi.personeltakip.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mechi.personeltakip.presentation.theme.*

data class TimePickerTarget(
    val day: Int,
    val isCheckIn: Boolean,
    val currentTime: String?
)

@Composable
fun TimePickerDialog(
    target: TimePickerTarget,
    onConfirm: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var timeInput by remember { mutableStateOf(target.currentTime ?: "") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(360.dp)
                .clickable(enabled = false) { },
            backgroundColor = Slate900,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Slate800),
            elevation = 16.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (target.isCheckIn) "Giriş Saati Düzenle (Gün ${target.day})" else "Çıkış Saati Düzenle (Gün ${target.day})",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, null, tint = Slate400)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Format: SS:DD (Örn: 09:00, 18:30)", color = Slate400, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))

                TextField(
                    value = timeInput,
                    onValueChange = { timeInput = it },
                    placeholder = { Text("09:00", color = Slate600) },
                    singleLine = true,
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Slate950,
                        textColor = Color.White,
                        focusedIndicatorColor = Indigo500,
                        unfocusedIndicatorColor = Slate700
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { onConfirm(null) }) {
                        Text("Kaldır / Boşalt", color = Rose500, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(timeInput.trim().ifEmpty { null }) },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Indigo600),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Kaydet", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
