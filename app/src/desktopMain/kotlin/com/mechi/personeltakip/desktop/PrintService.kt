package com.mechi.personeltakip.desktop

import com.mechi.personeltakip.domain.models.*
import java.awt.*
import java.awt.print.*
import java.text.SimpleDateFormat
import java.util.*

class PayrollPrintable(private val results: List<EmployeePayrollResult>) : Printable {
    override fun print(graphics: Graphics, pageFormat: PageFormat, pageIndex: Int): Int {
        val rowsPerPage = 28
        val totalPages = (results.size + rowsPerPage - 1) / rowsPerPage
        if (pageIndex >= totalPages) return Printable.NO_SUCH_PAGE

        val g2d = graphics as Graphics2D
        g2d.translate(pageFormat.imageableX, pageFormat.imageableY)

        val width = pageFormat.imageableWidth.toInt()
        var y = 35

        // Başlık
        g2d.font = Font("Arial", Font.BOLD, 15)
        g2d.color = Color(15, 23, 42) // Slate 900
        g2d.drawString("Mechi Puantaj - Personel Bordro Dökümü", 20, y)
        y += 18
        g2d.font = Font("Arial", Font.PLAIN, 9)
        g2d.color = Color.DARK_GRAY
        val date = SimpleDateFormat("dd.MM.yyyy HH:mm").format(Date())
        g2d.drawString("Yazdırma Tarihi: $date | Toplam Personel: ${results.size} | Sayfa: ${pageIndex + 1} / $totalPages", 20, y)
        y += 24

        // Tablo Başlıkları
        g2d.font = Font("Arial", Font.BOLD, 8)
        g2d.color = Color.BLACK
        val cols = intArrayOf(20, 50, 160, 205, 250, 300, 350, 400, 450, 500)
        val headers = arrayOf("ID", "AD SOYAD", "GELDİ", "DEV.", "MAAŞ", "DEV.BORÇ", "AVANS", "KESİNTİ", "MESAİ", "ALACAĞI / BORCU")

        for (i in headers.indices) {
            g2d.drawString(headers[i], cols[i], y)
        }
        y += 6
        g2d.drawLine(20, y, width - 20, y)
        y += 14

        // Sayfadaki satırları yazdır
        val startIndex = pageIndex * rowsPerPage
        val endIndex = minOf(startIndex + rowsPerPage, results.size)
        val pageResults = results.subList(startIndex, endIndex)

        g2d.font = Font("Arial", Font.PLAIN, 8)
        pageResults.forEach { res ->
            g2d.color = Color.BLACK
            g2d.drawString(res.id, cols[0], y)
            g2d.drawString(res.name, cols[1], y)
            g2d.drawString("${res.daysPresent} G", cols[2], y)
            g2d.drawString("${res.daysAbsent} G", cols[3], y)
            g2d.drawString("${res.baseSalary.toInt()} TL", cols[4], y)
            g2d.drawString("-${res.previousMonthDebt.toInt()} TL", cols[5], y)
            g2d.drawString("-${res.advancePayment.toInt()} TL", cols[6], y)
            g2d.drawString("-${res.deductions.toInt()} TL", cols[7], y)
            g2d.drawString("+${res.overtimePay.toInt()} TL", cols[8], y)

            val isDebt = res.isDebt
            val balanceStr = if (isDebt) "-${res.debtAmount.toInt()} TL (Borç)" else "+${res.payableAmount.toInt()} TL (Alacak)"
            g2d.color = if (isDebt) Color(220, 38, 38) else Color(16, 185, 129)
            g2d.drawString(balanceStr, cols[9], y)

            y += 18
            g2d.color = Color(226, 232, 240)
            g2d.drawLine(20, y - 5, width - 20, y - 5)
        }

        return Printable.PAGE_EXISTS
    }
}

object PrintService {
    fun printPayroll(results: List<EmployeePayrollResult>) {
        if (results.isEmpty()) return
        val job = PrinterJob.getPrinterJob()
        job.setPrintable(PayrollPrintable(results))
        if (job.printDialog()) {
            try {
                job.print()
            } catch (e: PrinterException) {
                e.printStackTrace()
            }
        }
    }
}
