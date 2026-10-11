package com.example.mtmp_zadanie_1

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow

class TrajectoryGraphView(
    context: Context,
    private val trajectoryPoints: ArrayList<TrajectoryPointParcel>
) : View(context) {

    // Convert dp to pixels for consistent spacing across devices.
    private fun dp(value: Float): Float {
        return value * resources.displayMetrics.density
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE0E0E0.toInt()
        strokeWidth = dp(1f)
    }

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF000000.toInt()
        strokeWidth = dp(2f)
    }

    private val trajectoryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF1565C0.toInt()
        strokeWidth = dp(3f)
        style = Paint.Style.STROKE
    }

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFD32F2F.toInt()
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF000000.toInt()
        textSize = dp(14f)
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF000000.toInt()
        textSize = dp(20f)
        textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(0xFFFFFFFF.toInt())

        if (trajectoryPoints.isEmpty()) {
            canvas.drawText(
                "No trajectory data",
                width / 2f,
                height / 2f,
                titlePaint
            )
            return
        }

        // Margins around the graph.
        val leftMargin = dp(65f)
        val rightMargin = dp(25f)
        val topMargin = dp(65f)
        val bottomMargin = dp(100f)

        val graphWidth = width - leftMargin - rightMargin
        val graphHeight = height - topMargin - bottomMargin

        if (graphWidth <= 0f || graphHeight <= 0f) return

        // X-axis represents time in seconds.
        val maxTime = max(
            trajectoryPoints.maxOf { it.time },
            0.1
        ) * 1.05

        // Y-axis represents vertical position in meters.
        val maxY = max(
            trajectoryPoints.maxOf { it.y },
            0.1
        ) * 1.10

        // Calculate independent horizontal and vertical scales.
        val xScale = graphWidth / maxTime.toFloat()
        val yScale = graphHeight / maxY.toFloat()

        // Convert graph coordinates into screen coordinates.
        fun screenX(time: Double): Float {
            return leftMargin + (time * xScale).toFloat()
        }

        fun screenY(y: Double): Float {
            return height - bottomMargin - (y * yScale).toFloat()
        }

        // Calculate readable grid intervals.
        val xStep = calculateStep(maxTime)
        val yStep = calculateStep(maxY)

        // Draw vertical grid lines and time labels.
        var t = 0.0
        while (t <= maxTime + xStep * 0.001) {
            val px = screenX(t)

            canvas.drawLine(
                px, topMargin,
                px, height - bottomMargin,
                gridPaint
            )

            textPaint.textAlign = Paint.Align.CENTER

            canvas.drawText(
                formatAxisNumber(t),
                px,
                height - bottomMargin + dp(20f),
                textPaint
            )

            t += xStep
        }

        // Draw horizontal grid lines and height labels.
        var y = 0.0
        while (y <= maxY + yStep * 0.001) {
            val py = screenY(y)

            canvas.drawLine(
                leftMargin, py,
                width - rightMargin, py,
                gridPaint
            )

            textPaint.textAlign = Paint.Align.RIGHT

            canvas.drawText(
                formatAxisNumber(y),
                leftMargin - dp(10f),
                py + dp(5f),
                textPaint
            )

            y += yStep
        }

        // Draw the X and Y axes.
        canvas.drawLine(
            leftMargin,
            height - bottomMargin,
            width - rightMargin,
            height - bottomMargin,
            axisPaint
        )

        canvas.drawLine(
            leftMargin,
            height - bottomMargin,
            leftMargin,
            topMargin,
            axisPaint
        )

        // Draw the trajectory curve.
        val path = Path()

        trajectoryPoints.forEachIndexed { index, point ->
            val px = screenX(point.time)
            val py = screenY(point.y)

            if (index == 0) {
                path.moveTo(px, py)
            } else {
                path.lineTo(px, py)
            }
        }

        canvas.drawPath(path, trajectoryPaint)

        // Draw the individual trajectory points.
        trajectoryPoints.forEach { point ->
            canvas.drawCircle(
                screenX(point.time),
                screenY(point.y),
                dp(3f),
                pointPaint
            )
        }

        // Draw the graph title with space from the top edge.
        canvas.drawText(
            "Vertical Position y(t)",
            width / 2f,
            dp(75f),
            titlePaint
        )

        // Draw the X-axis label with space from the bottom edge.
        textPaint.textAlign = Paint.Align.CENTER

        canvas.drawText(
            "Time t (s)",
            leftMargin + graphWidth / 2f,
            height - dp(50f),
            textPaint
        )

        // Draw the Y-axis label with space from the left edge.
        canvas.save()

        val labelX = dp(16f)
        val labelY = topMargin + graphHeight / 2f

        canvas.rotate(-90f, labelX, labelY)

        textPaint.textAlign = Paint.Align.CENTER

        canvas.drawText(
            "Height y (m)",
            labelX,
            labelY,
            textPaint
        )

        canvas.restore()
    }

    // Choose a convenient interval for the grid.
    private fun calculateStep(maxValue: Double): Double {
        val rawStep = maxValue / 6.0
        val magnitude = 10.0.pow(floor(log10(rawStep)))
        val normalized = rawStep / magnitude

        val niceStep = when {
            normalized <= 1.0 -> 1.0
            normalized <= 2.0 -> 2.0
            normalized <= 5.0 -> 5.0
            else -> 10.0
        }

        return niceStep * magnitude
    }

    // Format axis numbers without unnecessary decimal places.
    private fun formatAxisNumber(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format(
                java.util.Locale.US,
                "%.1f",
                value
            ).trimEnd('0').trimEnd('.')
        }
    }
}