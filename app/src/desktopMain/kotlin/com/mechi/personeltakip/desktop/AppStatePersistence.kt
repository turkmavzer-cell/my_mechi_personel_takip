package com.mechi.personeltakip.desktop

import com.mechi.personeltakip.domain.models.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class AppPersistentState(
    val employees: List<EmployeeAttendance> = emptyList(),
    val summary: ParsingSummary? = null,
    val shiftSettings: GlobalShiftSettings = GlobalShiftSettings(),
    val payrollSettingsMap: Map<String, EmployeePayrollSettings> = emptyMap()
)

object PersistenceManager {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    private val stateFile: File by lazy {
        val appData = System.getenv("APPDATA") ?: System.getProperty("user.home")
        val dir = File(appData, "MyMechiPersonelTakip")
        if (!dir.exists()) dir.mkdirs()
        File(dir, "state.json")
    }

    fun saveState(state: AppPersistentState) {
        try {
            val content = json.encodeToString(state)
            stateFile.writeText(content)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadState(): AppPersistentState? {
        return try {
            if (stateFile.exists()) {
                val content = stateFile.readText()
                json.decodeFromString<AppPersistentState>(content)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
