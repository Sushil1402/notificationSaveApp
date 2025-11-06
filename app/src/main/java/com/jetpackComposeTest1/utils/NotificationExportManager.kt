package com.jetpackComposeTest1.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.jetpackComposeTest1.db.NotificationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class NotificationExportManager(private val context: Context) {

    companion object {
        private const val FILE_NAME_PREFIX = "notifications_export"
        private const val FILE_EXTENSION = ".xlsx"
        
        // Date format for file name
        private val fileNameDateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        
        // Date format for display in Excel
        private val displayDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    }

    /**
     * Export all notifications to Excel file grouped by app
     * Structure: App Name (header) -> App's notifications (rows) -> Next App Name -> etc.
     * Returns the URI of the created file
     * 
     * @param notifications List of notifications to export
     * @param customFileNamePrefix Optional custom prefix for file name (e.g., "Gmail" for "Gmail_notifications_export_...")
     */
    suspend fun exportToExcel(
        notifications: List<NotificationEntity>,
        customFileNamePrefix: String? = null
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            if (notifications.isEmpty()) {
                return@withContext Result.failure(Exception("No notifications to export"))
            }

            // Group notifications by app (package name)
            val groupedByApp = notifications
                .groupBy { it.packageName }
                .toSortedMap() // Sort by package name for consistent ordering

            // Create workbook and sheet
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Notifications")
            
            // Create styles
            val appHeaderStyle = createAppHeaderStyle(workbook)
            val columnHeaderStyle = createColumnHeaderStyle(workbook)
            val dataStyle = createDataStyle(workbook)
            val dateStyle = createDateStyle(workbook)
            
            var currentRow = 0

            // Process each app group
            groupedByApp.forEach { (packageName, appNotifications) ->
                val appName = appNotifications.firstOrNull()?.appName ?: packageName
                
                // Create app name header row (merged across all columns)
                val appHeaderRow = sheet.createRow(currentRow++)
                val appHeaderCell = appHeaderRow.createCell(0)
                appHeaderCell.setCellValue("📱 $appName")
                appHeaderCell.cellStyle = appHeaderStyle
                
                // Merge cells for app name across all columns (10 columns)
                val mergeRange = CellRangeAddress(
                    currentRow - 1, 
                    currentRow - 1, 
                    0, 
                    9
                )
                sheet.addMergedRegion(mergeRange)
                
                // Create column headers for this app's notifications
                val headerRow = sheet.createRow(currentRow++)
                val headers = arrayOf(
                    "Title", "Message", "Sub Text", "Big Text",
                    "Timestamp", "Post Time", "Read Status", 
                    "Channel", "Category", "Priority"
                )
                
                headers.forEachIndexed { index, header ->
                    val cell = headerRow.createCell(index)
                    cell.setCellValue(header)
                    cell.cellStyle = columnHeaderStyle
                }
                
                // Add notification rows for this app
                appNotifications.sortedByDescending { it.timestamp }.forEach { notification ->
                    val row = sheet.createRow(currentRow++)
                    
                    // Title
                    row.createCell(0).apply {
                        setCellValue(notification.title)
                        cellStyle = dataStyle
                    }
                    
                    // Message
                    row.createCell(1).apply {
                        setCellValue(notification.text)
                        cellStyle = dataStyle
                    }
                    
                    // Sub Text
                    row.createCell(2).apply {
                        setCellValue(notification.subText ?: "")
                        cellStyle = dataStyle
                    }
                    
                    // Big Text
                    row.createCell(3).apply {
                        setCellValue(notification.bigText ?: "")
                        cellStyle = dataStyle
                    }
                    
                    // Timestamp
                    row.createCell(4).apply {
                        setCellValue(displayDateFormat.format(Date(notification.timestamp)))
                        cellStyle = dateStyle
                    }
                    
                    // Post Time
                    row.createCell(5).apply {
                        setCellValue(displayDateFormat.format(Date(notification.postTime)))
                        cellStyle = dateStyle
                    }
                    
                    // Read Status
                    row.createCell(6).apply {
                        setCellValue(if (notification.isRead) "✓ Read" else "○ Unread")
                        cellStyle = dataStyle
                    }
                    
                    // Channel
                    row.createCell(7).apply {
                        setCellValue(notification.channelName ?: "")
                        cellStyle = dataStyle
                    }
                    
                    // Category
                    row.createCell(8).apply {
                        setCellValue(notification.category ?: "")
                        cellStyle = dataStyle
                    }
                    
                    // Priority
                    row.createCell(9).apply {
                        setCellValue(notification.priority.toString())
                        cellStyle = dataStyle
                    }
                }
                
                // Add a blank row after each app group for better readability
                currentRow++
            }
            
            // Set column widths (autoSizeColumn doesn't work on Android due to AWT dependencies)
            // Column widths in units of 1/256th of a character width
            sheet.setColumnWidth(0, 6000)  // Title - 23.4 characters
            sheet.setColumnWidth(1, 12000) // Message - 46.9 characters
            sheet.setColumnWidth(2, 6000)  // Sub Text - 23.4 characters
            sheet.setColumnWidth(3, 10000) // Big Text - 39.1 characters
            sheet.setColumnWidth(4, 5000)  // Timestamp - 19.5 characters
            sheet.setColumnWidth(5, 5000)  // Post Time - 19.5 characters
            sheet.setColumnWidth(6, 4000)  // Read Status - 15.6 characters
            sheet.setColumnWidth(7, 6000)  // Channel - 23.4 characters
            sheet.setColumnWidth(8, 5000)  // Category - 19.5 characters
            sheet.setColumnWidth(9, 3000)  // Priority - 11.7 characters
            
            // Create file in app's external files directory
            val filePrefix = if (customFileNamePrefix != null) {
                // Sanitize app name to remove invalid file name characters
                val sanitizedAppName = customFileNamePrefix.replace(Regex("[^a-zA-Z0-9_-]"), "_")
                "${sanitizedAppName}_${FILE_NAME_PREFIX}"
            } else {
                FILE_NAME_PREFIX
            }
            val fileName = "${filePrefix}_${fileNameDateFormat.format(Date())}$FILE_EXTENSION"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            // Write workbook to file
            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }
            
            workbook.close()
            
            // Get URI using FileProvider
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createAppHeaderStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val style = workbook.createCellStyle() as XSSFCellStyle
        val font = workbook.createFont()
        font.bold = true
        font.fontHeightInPoints = 14.toShort()
        font.color = IndexedColors.WHITE.index
        style.setFont(font)
        style.fillForegroundColor = IndexedColors.DARK_BLUE.index
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        style.alignment = HorizontalAlignment.LEFT
        style.verticalAlignment = VerticalAlignment.CENTER
        style.borderBottom = BorderStyle.MEDIUM
        style.borderTop = BorderStyle.MEDIUM
        style.borderLeft = BorderStyle.MEDIUM
        style.borderRight = BorderStyle.MEDIUM
        style.setIndention(2.toShort())
        return style
    }

    private fun createColumnHeaderStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val style = workbook.createCellStyle() as XSSFCellStyle
        val font = workbook.createFont()
        font.bold = true
        font.fontHeightInPoints = 11.toShort()
        font.color = IndexedColors.WHITE.index
        style.setFont(font)
        style.fillForegroundColor = IndexedColors.GREY_50_PERCENT.index
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        style.alignment = HorizontalAlignment.CENTER
        style.verticalAlignment = VerticalAlignment.CENTER
        style.borderBottom = BorderStyle.THIN
        style.borderTop = BorderStyle.THIN
        style.borderLeft = BorderStyle.THIN
        style.borderRight = BorderStyle.THIN
        return style
    }

    private fun createDataStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val style = workbook.createCellStyle() as XSSFCellStyle
        style.alignment = HorizontalAlignment.LEFT
        style.verticalAlignment = VerticalAlignment.TOP
        style.wrapText = true
        style.borderBottom = BorderStyle.THIN
        style.borderTop = BorderStyle.THIN
        style.borderLeft = BorderStyle.THIN
        style.borderRight = BorderStyle.THIN
        return style
    }

    private fun createDateStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val style = createDataStyle(workbook)
        val dataFormat = workbook.creationHelper.createDataFormat()
        style.dataFormat = dataFormat.getFormat("yyyy-mm-dd hh:mm:ss")
        return style
    }
}

