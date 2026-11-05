package com.example.ung_dung_chay_bo.core.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

object RouteSnapshotGenerator {

    fun createRouteShowcaseBitmap(
        context: Context,
        path: List<Pair<Double, Double>>,
        distanceKm: Double,
        durationFormatted: String,
        paceOrSpeed: String,
        calories: Int,
        sportTitle: String,
        activityTitle: String = "Hoạt động chạy bộ",
        userBackgroundImage: Bitmap? = null,
        mapSnapshot: Bitmap? = null
    ): Bitmap {
        val width = 1080
        val height = 1350
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        if (userBackgroundImage != null) {
            drawScaledBitmap(canvas, userBackgroundImage, width, height)

            val overlayPaint = Paint().apply {
                color = Color.parseColor("#77000000")
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

            if (path.size >= 2) {
                drawRoutePolyline(canvas, path, width, height)
            }
        } else if (mapSnapshot != null) {
            drawScaledBitmap(canvas, mapSnapshot, width, height)

            val topGradient = LinearGradient(
                0f, 0f, 0f, 220f,
                intArrayOf(Color.parseColor("#99000000"), Color.TRANSPARENT),
                null,
                Shader.TileMode.CLAMP
            )
            val topPaint = Paint().apply { shader = topGradient }
            canvas.drawRect(0f, 0f, width.toFloat(), 220f, topPaint)
        } else {
            val gradient = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(
                    Color.parseColor("#111827"),
                    Color.parseColor("#1F2937"),
                    Color.parseColor("#0F172A")
                ),
                null,
                Shader.TileMode.CLAMP
            )
            val bgPaint = Paint().apply { shader = gradient }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            val gridPaint = Paint().apply {
                color = Color.parseColor("#15FFFFFF")
                strokeWidth = 2f
            }
            for (x in 0..width step 120) {
                canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height.toFloat(), gridPaint)
            }
            for (y in 0..height step 120) {
                canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), gridPaint)
            }

            if (path.size >= 2) {
                drawRoutePolyline(canvas, path, width, height)
            } else {
                drawFallbackRoute(canvas, width, height)
            }
        }

        val logoPaint = Paint().apply {
            color = Color.parseColor("#FC4C02")
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("STRAVA", 70f, 100f, logoPaint)

        val subLogoPaint = Paint().apply {
            color = Color.WHITE
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(" | RUNNER", 240f, 100f, subLogoPaint)

        val dateStr = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault()).format(Date())
        val datePaint = Paint().apply {
            color = Color.parseColor("#F3F4F6")
            textSize = 28f
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText(dateStr, width - 70f, 100f, datePaint)

        drawMetricsCard(
            canvas = canvas,
            width = width,
            height = height,
            distanceKm = distanceKm,
            durationFormatted = durationFormatted,
            paceOrSpeed = paceOrSpeed,
            calories = calories,
            sportTitle = sportTitle,
            activityTitle = activityTitle
        )

        return bitmap
    }

    private fun drawScaledBitmap(canvas: Canvas, source: Bitmap, destWidth: Int, destHeight: Int) {
        val srcRatio = source.width.toFloat() / source.height.toFloat()
        val dstRatio = destWidth.toFloat() / destHeight.toFloat()
        val srcRect: Rect
        if (srcRatio > dstRatio) {
            val cutWidth = (source.height * dstRatio).toInt()
            val left = (source.width - cutWidth) / 2
            srcRect = Rect(left, 0, left + cutWidth, source.height)
        } else {
            val cutHeight = (source.width / dstRatio).toInt()
            val top = (source.height - cutHeight) / 2
            srcRect = Rect(0, top, source.width, top + cutHeight)
        }
        val dstRect = Rect(0, 0, destWidth, destHeight)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(source, srcRect, dstRect, paint)
    }

    private fun drawRoutePolyline(
        canvas: Canvas,
        path: List<Pair<Double, Double>>,
        canvasWidth: Int,
        canvasHeight: Int
    ) {
        var minLat = Double.MAX_VALUE
        var maxLat = -Double.MAX_VALUE
        var minLng = Double.MAX_VALUE
        var maxLng = -Double.MAX_VALUE

        for (p in path) {
            minLat = min(minLat, p.first)
            maxLat = max(maxLat, p.first)
            minLng = min(minLng, p.second)
            maxLng = max(maxLng, p.second)
        }

        val latSpan = (maxLat - minLat).coerceAtLeast(0.0005)
        val lngSpan = (maxLng - minLng).coerceAtLeast(0.0005)

        val paddingX = 100f
        val topY = 160f
        val bottomY = 820f
        val drawWidth = canvasWidth - (paddingX * 2)
        val drawHeight = bottomY - topY

        val scale = min(drawWidth / lngSpan, drawHeight / latSpan)
        val offsetX = paddingX + (drawWidth - (lngSpan * scale).toFloat()) / 2
        val offsetY = topY + (drawHeight - (latSpan * scale).toFloat()) / 2

        val points = path.map { p ->
            val x = (offsetX + (p.second - minLng) * scale).toFloat()
            val y = (bottomY - (p.first - minLat) * scale).toFloat()
            PointF(x, y)
        }

        val routePath = android.graphics.Path()
        points.forEachIndexed { index, pt ->
            if (index == 0) routePath.moveTo(pt.x, pt.y)
            else routePath.lineTo(pt.x, pt.y)
        }

        val glowPaint = Paint().apply {
            color = Color.parseColor("#44FC4C02")
            strokeWidth = 24f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }
        canvas.drawPath(routePath, glowPaint)

        val borderPaint = Paint().apply {
            color = Color.WHITE
            strokeWidth = 14f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }
        canvas.drawPath(routePath, borderPaint)

        val linePaint = Paint().apply {
            color = Color.parseColor("#FC4C02")
            strokeWidth = 10f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }
        canvas.drawPath(routePath, linePaint)

        val startPt = points.first()
        val startBgPaint = Paint().apply { color = Color.WHITE; isAntiAlias = true }
        val startDotPaint = Paint().apply { color = Color.parseColor("#007AFF"); isAntiAlias = true }
        canvas.drawCircle(startPt.x, startPt.y, 18f, startBgPaint)
        canvas.drawCircle(startPt.x, startPt.y, 13f, startDotPaint)

        val endPt = points.last()
        val endBgPaint = Paint().apply { color = Color.WHITE; isAntiAlias = true }
        val endDotPaint = Paint().apply { color = Color.parseColor("#FC4C02"); isAntiAlias = true }
        canvas.drawCircle(endPt.x, endPt.y, 20f, endBgPaint)
        canvas.drawCircle(endPt.x, endPt.y, 14f, endDotPaint)
    }

    private fun drawFallbackRoute(canvas: Canvas, canvasWidth: Int, canvasHeight: Int) {
        val centerX = canvasWidth / 2f
        val centerY = 480f

        val path = android.graphics.Path().apply {
            moveTo(centerX - 240f, centerY + 80f)
            cubicTo(
                centerX - 120f, centerY - 140f,
                centerX + 120f, centerY + 180f,
                centerX + 240f, centerY - 80f
            )
        }

        val glowPaint = Paint().apply {
            color = Color.parseColor("#44FC4C02")
            strokeWidth = 24f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }
        canvas.drawPath(path, glowPaint)

        val linePaint = Paint().apply {
            color = Color.parseColor("#FC4C02")
            strokeWidth = 12f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }
        canvas.drawPath(path, linePaint)
    }

    private fun drawMetricsCard(
        canvas: Canvas,
        width: Int,
        height: Int,
        distanceKm: Double,
        durationFormatted: String,
        paceOrSpeed: String,
        calories: Int,
        sportTitle: String,
        activityTitle: String
    ) {
        val cardLeft = 60f
        val cardTop = 880f
        val cardRight = width - 60f
        val cardBottom = height - 60f
        val cornerRadius = 36f

        val cardBgPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
        }
        val cardRect = RectF(cardLeft, cardTop, cardRight, cardBottom)
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, cardBgPaint)

        val titlePaint = Paint().apply {
            color = Color.parseColor("#111827")
            textSize = 40f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(activityTitle, cardLeft + 45f, cardTop + 75f, titlePaint)

        val sportBadgePaint = Paint().apply {
            color = Color.parseColor("#FFF0EB")
            isAntiAlias = true
        }
        val badgeRect = RectF(cardRight - 220f, cardTop + 35f, cardRight - 45f, cardTop + 90f)
        canvas.drawRoundRect(badgeRect, 20f, 20f, sportBadgePaint)

        val sportTextPaint = Paint().apply {
            color = Color.parseColor("#FC4C02")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(sportTitle, badgeRect.centerX(), badgeRect.centerY() + 10f, sportTextPaint)

        val dividerPaint = Paint().apply {
            color = Color.parseColor("#F3F4F6")
            strokeWidth = 2f
        }
        canvas.drawLine(cardLeft + 45f, cardTop + 115f, cardRight - 45f, cardTop + 115f, dividerPaint)

        val distLabelPaint = Paint().apply {
            color = Color.parseColor("#6B7280")
            textSize = 28f
            isAntiAlias = true
        }
        canvas.drawText("QUÃNG ĐƯỜNG", cardLeft + 45f, cardTop + 165f, distLabelPaint)

        val distValuePaint = Paint().apply {
            color = Color.parseColor("#111827")
            textSize = 96f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("%.2f".format(distanceKm), cardLeft + 45f, cardTop + 260f, distValuePaint)

        val kmUnitPaint = Paint().apply {
            color = Color.parseColor("#FC4C02")
            textSize = 44f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val kmTextX = cardLeft + 45f + distValuePaint.measureText("%.2f".format(distanceKm)) + 15f
        canvas.drawText("km", kmTextX, cardTop + 245f, kmUnitPaint)

        val colY = cardTop + 330f
        val colWidth = (cardRight - cardLeft - 90f) / 3f

        val col1X = cardLeft + 45f
        canvas.drawText("THỜI GIAN", col1X, colY, distLabelPaint)
        val metricValPaint = Paint().apply {
            color = Color.parseColor("#111827")
            textSize = 38f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(durationFormatted, col1X, colY + 50f, metricValPaint)

        val col2X = col1X + colWidth
        canvas.drawText("TỐC ĐỘ / PACE", col2X, colY, distLabelPaint)
        canvas.drawText(paceOrSpeed, col2X, colY + 50f, metricValPaint)

        val col3X = col2X + colWidth
        canvas.drawText("CALO", col3X, colY, distLabelPaint)
        canvas.drawText("$calories kcal", col3X, colY + 50f, metricValPaint)
    }

    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, title: String): Uri? {
        val fileName = "STRAVA_${System.currentTimeMillis()}.jpg"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/UngDungChayBo")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                }
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)
            }
            uri
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/UngDungChayBo"
            val file = File(imagesDir)
            if (!file.exists()) file.mkdirs()
            val imageFile = File(file, fileName)
            FileOutputStream(imageFile).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
            }
            Uri.fromFile(imageFile)
        }
    }

    fun saveBitmapToCacheForSharing(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "share_route_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
