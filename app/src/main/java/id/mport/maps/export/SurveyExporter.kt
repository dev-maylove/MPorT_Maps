package id.mport.maps.export

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import id.mport.maps.data.entity.SurveyEntity
import id.mport.maps.domain.measurement.PointCodec
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SurveyExporter {

    fun toCsv(survey: SurveyEntity): String {
        val points = PointCodec.decode(survey.pointsJson)
        val sb = StringBuilder()
        sb.appendLine("name,mode,distance_m,area_m2,perimeter_m,notes,created_at")
        sb.appendLine(
            "\"${escapeCsv(survey.name)}\",${survey.mode},${survey.distanceMeters}," +
                "${survey.areaSquareMeters},${survey.perimeterMeters}," +
                "\"${escapeCsv(survey.notes)}\",${survey.createdAt}"
        )
        sb.appendLine()
        sb.appendLine("index,latitude,longitude")
        points.forEachIndexed { i, p ->
            sb.appendLine("$i,${p.latitude},${p.longitude}")
        }
        return sb.toString()
    }

    fun toJson(survey: SurveyEntity): String {
        val points = PointCodec.decode(survey.pointsJson)
        val pts = points.joinToString(",") {
            """{"lat":${it.latitude},"lng":${it.longitude}}"""
        }
        return buildString {
            append("{\n")
            append("  \"name\": \"${escapeJson(survey.name)}\",\n")
            append("  \"mode\": \"${survey.mode}\",\n")
            append("  \"distanceMeters\": ${survey.distanceMeters},\n")
            append("  \"areaSquareMeters\": ${survey.areaSquareMeters},\n")
            append("  \"perimeterMeters\": ${survey.perimeterMeters},\n")
            append("  \"notes\": \"${escapeJson(survey.notes)}\",\n")
            append("  \"createdAt\": ${survey.createdAt},\n")
            append("  \"points\": [$pts]\n")
            append("}")
        }
    }

    fun toKml(survey: SurveyEntity): String {
        val points = PointCodec.decode(survey.pointsJson)
        val coords = points.joinToString(" ") { "${it.longitude},${it.latitude},0" }
        val name = escapeXml(survey.name)
        val notes = escapeXml(survey.notes)

        return if (survey.mode == "AREA" && points.size >= 3) {
            val closed = coords + " ${points.first().longitude},${points.first().latitude},0"
            """
            |<?xml version="1.0" encoding="UTF-8"?>
            |<kml xmlns="http://www.opengis.net/kml/2.2">
            |  <Document>
            |    <name>$name</name>
            |    <Placemark>
            |      <name>$name</name>
            |      <description>$notes</description>
            |      <Polygon>
            |        <outerBoundaryIs>
            |          <LinearRing>
            |            <coordinates>$closed</coordinates>
            |          </LinearRing>
            |        </outerBoundaryIs>
            |      </Polygon>
            |    </Placemark>
            |  </Document>
            |</kml>
            """.trimMargin()
        } else {
            """
            |<?xml version="1.0" encoding="UTF-8"?>
            |<kml xmlns="http://www.opengis.net/kml/2.2">
            |  <Document>
            |    <name>$name</name>
            |    <Placemark>
            |      <name>$name</name>
            |      <description>$notes</description>
            |      <LineString>
            |        <coordinates>$coords</coordinates>
            |      </LineString>
            |    </Placemark>
            |  </Document>
            |</kml>
            """.trimMargin()
        }
    }

    /**
     * Save to public Downloads/MPorT Maps on API 29+,
     * fallback to app external files dir on older APIs (no storage permission required).
     */
    fun saveToDownloads(
        context: Context,
        fileName: String,
        mimeType: String,
        content: String
    ): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(
                        MediaStore.Downloads.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS + "/MPorT Maps"
                    )
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: return false
                resolver.openOutputStream(uri)?.use { out: OutputStream ->
                    out.write(content.toByteArray(Charsets.UTF_8))
                } ?: return false
                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                true
            } else {
                val dir = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                    "MPorT Maps"
                )
                if (!dir.exists()) dir.mkdirs()
                File(dir, fileName).writeText(content, Charsets.UTF_8)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun defaultFileName(survey: SurveyEntity, ext: String): String {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(survey.createdAt))
        val safe = survey.name.replace(Regex("[^A-Za-z0-9_-]"), "_").take(40)
        return "MPorT_${safe}_$stamp.$ext"
    }

    private fun escapeCsv(s: String) = s.replace("\"", "\"\"")
    private fun escapeJson(s: String) = s
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
    private fun escapeXml(s: String) = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}
