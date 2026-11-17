package com.pluschat.adbcontrol

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Navy = Color(0xFF101827)
private val Panel = Color(0xFF1B293B)
private val Blue = Color(0xFF4D9CFF)
private val Muted = Color(0xFFB8C5D6)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Blue,
                background = Navy,
                surface = Panel,
                onPrimary = Color.White,
                onBackground = Color.White,
                onSurface = Color.White
            )) {
                var refreshKey by remember { mutableStateOf(0) }
                val diagnostics = remember(refreshKey) { readDiagnostics() }
                var command by remember { mutableStateOf("adb devices") }
                var output by remember { mutableStateOf("Команды ADB пока недоступны: нет авторизованного ADB-моста.") }

                Surface(modifier = Modifier.fillMaxSize(), color = Navy) {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = Blue.copy(alpha = 0.16f), shape = RoundedCornerShape(14.dp)) {
                                androidx.compose.material3.Icon(Icons.Default.Bolt, null, tint = Blue, modifier = Modifier.padding(12.dp).size(28.dp))
                            }
                            Column(Modifier.padding(start = 12.dp)) {
                                Text("ADB Control", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                                Text("Диагностика Android и ADB", color = Muted, fontSize = 13.sp)
                            }
                        }

                        Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("УСТРОЙСТВО", color = Blue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("${Build.MANUFACTURER} ${Build.MODEL}", fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                                Text("Android ${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}", color = Muted)
                                Text("Прошивка: ${Build.DISPLAY}", color = Muted, fontSize = 12.sp)
                            }
                        }

                        Text("СОСТОЯНИЕ ОТЛАДКИ", color = Blue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        StatusCard("Отладка по USB", diagnostics.usb, diagnostics.usbDetails)
                        StatusCard("Беспроводная отладка", diagnostics.wireless, diagnostics.wirelessDetails)
                        StatusCard("Режим разработчика", diagnostics.developer, diagnostics.developerDetails)

                        Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    androidx.compose.material3.Icon(Icons.Default.Security, null, tint = Blue)
                                    Text("Почему ADB может быть недоступен", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.SemiBold)
                                }
                                Text(
                                    "Обычное приложение не может само выдать себе ADB-права или включить беспроводную отладку. Нужна авторизация ADB-хоста, root или заранее разрешённый привилегированный мост (например, Shizuku).",
                                    color = Muted
                                )
                                Text("ADB Control не обходит системную защиту и показывает реальный статус.", color = Color(0xFFFFD28A), fontSize = 13.sp)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = { refreshKey++ }, modifier = Modifier.weight(1f)) {
                                androidx.compose.material3.Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                                Text(" Обновить")
                            }
                            Button(onClick = {
                                runCatching { startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)) }
                            }, modifier = Modifier.weight(1f)) {
                                androidx.compose.material3.Icon(Icons.Default.Settings, null, modifier = Modifier.size(18.dp))
                                Text(" Настройки")
                            }
                        }

                        Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    androidx.compose.material3.Icon(Icons.Default.Code, null, tint = Blue)
                                    Text("Консоль ADB", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.SemiBold)
                                }
                                OutlinedTextField(
                                    value = command,
                                    onValueChange = { command = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Команда") },
                                    singleLine = true
                                )
                                Button(onClick = {
                                    val normalized = command.trim()
                                    output = when {
                                        normalized.isEmpty() -> "Введите команду."
                                        normalized == "help" -> "Примеры: adb devices, adb shell getprop, adb shell pm list packages. Для выполнения нужен авторизованный ADB-мост."
                                        normalized.startsWith("adb ") || normalized == "adb" ->
                                            "НЕ ВЫПОЛНЕНО: ADB-мост не подключён. Обычное приложение Android не содержит системного ADB-хоста и не имеет права выполнять эту команду. Нужна авторизованная сессия."
                                        else -> "Локальная команда не запускалась. В этой версии консоль принимает ADB-команды только для проверки готовности; произвольный shell не исполняется."
                                    }
                                }, modifier = Modifier.fillMaxWidth()) { Text("Проверить команду") }
                                Surface(color = Color(0xFF0D1521), shape = RoundedCornerShape(10.dp)) {
                                    Text(output, modifier = Modifier.fillMaxWidth().padding(12.dp), color = Color(0xFFB9E6C8), fontSize = 13.sp)
                                }
                            }
                        }
                        Text("ADB Control 1.0.0 · Без root · Без скрытого обхода ограничений", color = Muted, fontSize = 11.sp)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    private fun readDiagnostics(): Diagnostics {
        fun setting(name: String): Int? = try {
            Settings.Global.getInt(contentResolver, name)
        } catch (_: Exception) { null }

        val usb = setting(Settings.Global.ADB_ENABLED)
        val wireless = setting("adb_wifi_enabled")
        val developer = setting("development_settings_enabled")
        return Diagnostics(
            usb = usb == 1,
            usbDetails = if (usb == null) "Системное значение недоступно приложению" else if (usb == 1) "Включена в настройках Android" else "Выключена",
            wireless = wireless == 1,
            wirelessDetails = if (wireless == null) "Система не раскрыла состояние (или функция отсутствует)" else if (wireless == 1) "Включена в настройках Android" else "Выключена",
            developer = developer == 1,
            developerDetails = if (developer == null) "Системное значение недоступно приложению" else if (developer == 1) "Включён" else "Выключен"
        )
    }
}

private data class Diagnostics(
    val usb: Boolean,
    val usbDetails: String,
    val wireless: Boolean,
    val wirelessDetails: String,
    val developer: Boolean,
    val developerDetails: String
)

@androidx.compose.runtime.Composable
private fun StatusCard(title: String, enabled: Boolean, details: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(details, color = Muted, fontSize = 12.sp)
            }
            Text(if (enabled) "ВКЛ" else "НЕТ", color = if (enabled) Color(0xFF7FE0A0) else Color(0xFFFFB86B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}
