package com.mechi.personeltakip.domain.calculator

object CalendarUtils {

    /**
     * Zeller uyarlaması ile ISO standardına yakın gün hesaplama:
     * 0 -> Cumartesi, 1 -> Pazar, 2 -> Pazartesi, 3 -> Salı, 4 -> Çarşamba, 5 -> Perşembe, 6 -> Cuma
     * Hafta sonu listesi: varsayılan 6 (Cuma) veya 1 (Pazar)
     */
    fun getDayOfWeekIndex(yearMonth: String, day: Int): Int {
        val parts = yearMonth.split("-")
        if (parts.size < 2) return 0
        val year = parts[0].toIntOrNull() ?: 2026
        val month = parts[1].toIntOrNull() ?: 7

        var y = year
        var m = month
        if (m < 3) {
            m += 12
            y -= 1
        }
        val k = y % 100
        val j = y / 100
        val h = (day + (13 * (m + 1)) / 5 + k + k / 4 + j / 4 + 5 * j) % 7
        // h: 0=Cumartesi, 1=Pazar, 2=Pazartesi, 3=Salı, 4=Çarşamba, 5=Perşembe, 6=Cuma
        return h
    }

    fun isWeekend(yearMonth: String, day: Int, weekendDays: List<Int>): Boolean {
        // Mevcut ZKTeco matris uyumluluğu için dayOfWeek hesabı:
        val parts = yearMonth.split("-")
        if (parts.size < 2) return false
        val year = parts[0].toIntOrNull() ?: 2026
        val month = parts[1].toIntOrNull() ?: 7

        var y = year
        var m = month
        if (m < 3) {
            m += 12
            y -= 1
        }
        val k = y % 100
        val j = y / 100
        val h = (day + (13 * (m + 1)) / 5 + k + k / 4 + j / 4 + 5 * j) % 7
        val dayOfWeek = (h + 6) % 7
        return dayOfWeek in weekendDays
    }

    fun getDayNameTr(yearMonth: String, day: Int): String {
        val parts = yearMonth.split("-")
        if (parts.size < 2) return ""
        val year = parts[0].toIntOrNull() ?: 2026
        val month = parts[1].toIntOrNull() ?: 7

        var y = year
        var m = month
        if (m < 3) {
            m += 12
            y -= 1
        }
        val k = y % 100
        val j = y / 100
        val h = (day + (13 * (m + 1)) / 5 + k + k / 4 + j / 4 + 5 * j) % 7
        val names = arrayOf("Cte", "Paz", "Pzt", "Sal", "Çar", "Per", "Cum (Tatil)")
        return if (h in names.indices) names[h] else ""
    }

    fun getMonthNameTr(yearMonth: String): String {
        val parts = yearMonth.split("-")
        val month = parts.getOrNull(1)
        val year = parts.getOrNull(0) ?: ""
        val monthName = when (month) {
            "01" -> "Ocak"
            "02" -> "Şubat"
            "03" -> "Mart"
            "04" -> "Nisan"
            "05" -> "Mayıs"
            "06" -> "Haziran"
            "07" -> "Temmuz"
            "08" -> "Ağustos"
            "09" -> "Eylül"
            "10" -> "Ekim"
            "11" -> "Kasım"
            "12" -> "Aralık"
            else -> month ?: ""
        }
        return "$monthName $year".trim()
    }
}
