package com.example.ung_dung_chay_bo.core.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.ung_dung_chay_bo.data.local.entity.ActivityRecordEntity
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object GpxExporter {
    private const val TAG = "GpxExporter"

    fun exportToGpx(
        context: Context,
        record: ActivityRecordEntity,
        points: List<Pair<Double, Double>>
    ): Uri? {
        return try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val cleanTitle = record.title.replace(Regex("[^a-zA-Z0-9_]"), "_").take(30)
            val file = File(exportDir, "activity_${record.id}_${cleanTitle}.gpx")

            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val startTimeStr = isoFormat.format(Date(record.timestamp))

            val sportTypeGpx = when (record.sportType.uppercase()) {
                "RIDE" -> "biking"
                "WALK" -> "walking"
                else -> "running"
            }

            FileWriter(file).use { writer ->
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                writer.write("<gpx version=\"1.1\" creator=\"StravaRunnerApp\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n")
                writer.write("  <metadata>\n")
                writer.write("    <name>${escapeXml(record.title)}</name>\n")
                writer.write("    <time>$startTimeStr</time>\n")
                writer.write("  </metadata>\n")
                writer.write("  <trk>\n")
                writer.write("    <name>${escapeXml(record.title)}</name>\n")
                writer.write("    <type>$sportTypeGpx</type>\n")
                writer.write("    <trkseg>\n")

                val totalPoints = points.size
                val durationSec = record.durationSeconds.coerceAtLeast(1L)
                val timeStepMs = if (totalPoints > 1) (durationSec * 1000L) / (totalPoints - 1) else 1000L

                points.forEachIndexed { index, pt ->
                    val ptTime = isoFormat.format(Date(record.timestamp + index * timeStepMs))
                    writer.write("      <trkpt lat=\"${pt.first}\" lon=\"${pt.second}\">\n")
                    writer.write("        <ele>15.0</ele>\n")
                    writer.write("        <time>$ptTime</time>\n")
                    writer.write("      </trkpt>\n")
                }

                writer.write("    </trkseg>\n")
                writer.write("  </trk>\n")
                writer.write("</gpx>\n")
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi xuất file GPX: ${e.message}", e)
            null
        }
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
