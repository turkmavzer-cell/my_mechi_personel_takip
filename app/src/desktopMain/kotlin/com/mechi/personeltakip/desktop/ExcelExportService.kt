package com.mechi.personeltakip.desktop

import com.mechi.personeltakip.domain.models.*
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date

object ExcelExportService {

    fun exportPayrollToExcel(results: List<EmployeePayrollResult>, outputFile: File): Boolean {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Bordro Dökümü")

            // Stiller
            val headerFont = workbook.createFont().apply {
                bold = true
                color = IndexedColors.WHITE.index
                fontHeightInPoints = 10
            }
            val headerStyle = workbook.createCellStyle().apply {
                setFont(headerFont)
                fillForegroundColor = IndexedColors.INDIGO.index
                fillPattern = FillPatternType.SOLID_FOREGROUND
                alignment = HorizontalAlignment.CENTER
                verticalAlignment = VerticalAlignment.CENTER
            }

            val currencyStyle = workbook.createCellStyle().apply {
                dataFormat = workbook.createDataFormat().getFormat("#,##0.00 TL")
            }

            val headers = arrayOf(
                "Personel ID", "Ad Soyad", "Departman", "Geldiği Gün", "Devamsız Gün",
                "Hafta Sonu Çalışma", "Toplam Çalışma (Saat)", "Toplam Mesai (Saat)",
                "Taban Maaş", "Geçen Ay Borç", "Avans", "Kesintiler", "Mesai Ücreti",
                "Net Bordro / Bakiye", "Bakiye Durumu"
            )

            // Başlık satırı
            val headerRow = sheet.createRow(0)
            headerRow.heightInPoints = 24f
            for (i in headers.indices) {
                val cell = headerRow.createCell(i)
                cell.setCellValue(headers[i])
                cell.cellStyle = headerStyle
            }

            // Veri satırları
            var rowIdx = 1
            for (res in results) {
                val row = sheet.createRow(rowIdx++)
                row.createCell(0).setCellValue(res.id)
                row.createCell(1).setCellValue(res.name)
                row.createCell(2).setCellValue(res.department)
                row.createCell(3).setCellValue(res.daysPresent.toDouble())
                row.createCell(4).setCellValue(res.daysAbsent.toDouble())
                row.createCell(5).setCellValue(res.daysWeekendPresent.toDouble())
                row.createCell(6).setCellValue(res.totalWorkedHours)
                row.createCell(7).setCellValue(res.totalOvertimeHours)

                val cBase = row.createCell(8)
                cBase.setCellValue(res.baseSalary)
                cBase.cellStyle = currencyStyle

                val cDebt = row.createCell(9)
                cDebt.setCellValue(res.previousMonthDebt)
                cDebt.cellStyle = currencyStyle

                val cAdvance = row.createCell(10)
                cAdvance.setCellValue(res.advancePayment)
                cAdvance.cellStyle = currencyStyle

                val cDeduct = row.createCell(11)
                cDeduct.setCellValue(res.deductions)
                cDeduct.cellStyle = currencyStyle

                val cOtPay = row.createCell(12)
                cOtPay.setCellValue(res.overtimePay)
                cOtPay.cellStyle = currencyStyle

                val cNet = row.createCell(13)
                cNet.setCellValue(res.netSalary)
                cNet.cellStyle = currencyStyle

                val cStatus = row.createCell(14)
                cStatus.setCellValue(if (res.isDebt) "Devreden Borç (-${res.debtAmount.toInt()} TL)" else "Net Alacak (+${res.payableAmount.toInt()} TL)")
            }

            // Sütun genişliklerini ayarla
            for (i in headers.indices) {
                sheet.autoSizeColumn(i)
            }

            val fos = FileOutputStream(outputFile)
            workbook.write(fos)
            fos.close()
            workbook.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
