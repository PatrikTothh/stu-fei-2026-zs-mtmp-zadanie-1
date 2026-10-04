package com.example.mtmp_zadanie_1

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View

class TrajectoryGraphView(
    context: Context,
    private val trajectoryPoints: ArrayList<TrajectoryPointParcel>
) : View(context) {

    private val gridPaint = Paint().apply {
        color = 0xFFE0E0E0.toInt()
        strokeWidth = 1f
    }

    private val axisPaint = Paint().apply {
        color = 0xFF000000.toInt()
        strokeWidth = 3f
    }

    private val trajectoryPaint = Paint().apply {
        color = 0xFF1565C0.toInt()
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val pointPaint = Paint().apply {
        color = 0xFFD32F2F.toInt()
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = 0xFF000000.toInt()
        textSize = 30f
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (trajectoryPoints.isEmpty()) {
            return
        }

        canvas.drawColor(0xFFFFFFFF.toInt())

        val leftMargin = 80f
        val bottomMargin = 80f
        val topMargin = 40f
        val rightMargin = 40f

        val graphWidth = width - leftMargin - rightMargin
        val graphHeight = height - topMargin - bottomMargin

        val maxX = trajectoryPoints.maxOf { it.x }
        val maxY = trajectoryPoints.maxOf { it.y }

        /*
         * Calculate the scale dynamically so that the
         * entire trajectory fits inside the screen.
         */
        val xScale = graphWidth / (maxX * 1.1).toFloat()
        val yScale = graphHeight / (maxY * 1.1).toFloat()

        /*
         * Use the same scale for X and Y.
         * This prevents the trajectory from being distorted.
         */
        val pixelsPerMeter = minOf(xScale, yScale)

        val graphMaxX = graphWidth / pixelsPerMeter
        val graphMaxY = graphHeight / pixelsPerMeter

        /*
         * Decide how often numbers should appear
         * on each axis.
         */
        val xLabelStep = calculateLabelStep(graphMaxX)
        val yLabelStep = calculateLabelStep(graphMaxY)

        // Draw vertical grid lines
        var x = 0.0

        while (x <= graphMaxX) {

            val screenX =
                leftMargin +
                        (x * pixelsPerMeter).toFloat()

            canvas.drawLine(
                screenX,
                topMargin,
                screenX,
                height - bottomMargin,
                gridPaint
            )

            /*
             * Only draw the number when we reach
             * the selected label interval.
             */
            if (x % xLabelStep < 0.001) {

                canvas.drawText(
                    x.toInt().toString(),
                    screenX - 8f,
                    height - bottomMargin + 35f,
                    textPaint
                )
            }

            x += 1.0
        }

        // Draw horizontal grid lines
        var y = 0.0

        while (y <= graphMaxY) {

            val screenY =
                height - bottomMargin -
                        (y * pixelsPerMeter).toFloat()

            canvas.drawLine(
                leftMargin,
                screenY,
                width - rightMargin,
                screenY,
                gridPaint
            )

            /*
             * Only draw the number when we reach
             * the selected label interval.
             */
            if (y > 0 && y % yLabelStep < 0.001) {

                canvas.drawText(
                    y.toInt().toString(),
                    leftMargin - 35f,
                    screenY + 10f,
                    textPaint
                )
            }

            y += 1.0
        }

        // X axis
        canvas.drawLine(
            leftMargin,
            height - bottomMargin,
            width - rightMargin,
            height - bottomMargin,
            axisPaint
        )

        // Y axis
        canvas.drawLine(
            leftMargin,
            height - bottomMargin,
            leftMargin,
            topMargin,
            axisPaint
        )

        // Draw trajectory
        val path = Path()

        trajectoryPoints.forEachIndexed { index, point ->

            val screenX =
                leftMargin +
                        (point.x * pixelsPerMeter).toFloat()

            val screenY =
                height - bottomMargin -
                        (point.y * pixelsPerMeter).toFloat()

            if (index == 0) {
                path.moveTo(screenX, screenY)
            } else {
                path.lineTo(screenX, screenY)
            }
        }

        canvas.drawPath(path, trajectoryPaint)

        // Draw trajectory points
        trajectoryPoints.forEach { point ->

            val screenX =
                leftMargin +
                        (point.x * pixelsPerMeter).toFloat()

            val screenY =
                height - bottomMargin -
                        (point.y * pixelsPerMeter).toFloat()

            canvas.drawCircle(
                screenX,
                screenY,
                9f,
                pointPaint
            )
        }
    }

    /*
     * Determines how frequently numbers should
     * be displayed on the axes.
     */
    private fun calculateLabelStep(maxValue: Float): Int {

        return when {
            maxValue <= 10 -> 1
            maxValue <= 20 -> 2
            maxValue <= 50 -> 5
            maxValue <= 100 -> 10
            maxValue <= 200 -> 20
            maxValue <= 500 -> 50
            maxValue <= 1000 -> 100
            else -> 200
        }
    }
}