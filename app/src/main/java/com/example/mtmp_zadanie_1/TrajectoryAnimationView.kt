package com.example.mtmp_zadanie_1

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class TrajectoryAnimationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var trajectoryPoints = ArrayList<TrajectoryPointParcel>()

    private var animator: ValueAnimator? = null
    private var currentPointIndex = 0

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

    fun setTrajectory(points: ArrayList<TrajectoryPointParcel>) {
        trajectoryPoints = points
        currentPointIndex = 0
        invalidate()
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

        if (maxX <= 0.0 || maxY <= 0.0) {
            return
        }

        val xScale =
            graphWidth / (maxX * 1.1).toFloat()

        val yScale =
            graphHeight / (maxY * 1.1).toFloat()

        val pixelsPerMeter = minOf(xScale, yScale)

        val graphMaxX = graphWidth / pixelsPerMeter
        val graphMaxY = graphHeight / pixelsPerMeter

        val xLabelStep = calculateLabelStep(graphMaxX)
        val yLabelStep = calculateLabelStep(graphMaxY)

        // Grid
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

        // Axes
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

        // Draw trajectory behind projectile
        val path = Path()

        val lastIndex =
            currentPointIndex.coerceAtMost(trajectoryPoints.size - 1)

        for (i in 0..lastIndex) {

            val point = trajectoryPoints[i]

            val screenX =
                leftMargin +
                        (point.x * pixelsPerMeter).toFloat()

            val screenY =
                height - bottomMargin -
                        (point.y * pixelsPerMeter).toFloat()

            if (i == 0) {
                path.moveTo(screenX, screenY)
            } else {
                path.lineTo(screenX, screenY)
            }
        }

        canvas.drawPath(path, trajectoryPaint)

        // Draw projectile
        val projectile =
            trajectoryPoints[lastIndex]

        val projectileX =
            leftMargin +
                    (projectile.x * pixelsPerMeter).toFloat()

        val projectileY =
            height - bottomMargin -
                    (projectile.y * pixelsPerMeter).toFloat()

        canvas.drawCircle(
            projectileX,
            projectileY,
            14f,
            pointPaint
        )
    }

    fun startAnimation() {

        if (trajectoryPoints.isEmpty()) {
            return
        }

        animator?.cancel()

        animator = ValueAnimator.ofInt(
            currentPointIndex,
            trajectoryPoints.size - 1
        ).apply {

            // Animation duration
            duration = 5000L

            interpolator = LinearInterpolator()

            addUpdateListener { animation ->

                currentPointIndex =
                    animation.animatedValue as Int

                invalidate()
            }

            start()
        }
    }

    fun pauseAnimation() {
        animator?.pause()
    }

    fun restartAnimation() {

        animator?.cancel()

        currentPointIndex = 0

        invalidate()

        startAnimation()
    }

    fun isRunning(): Boolean {
        return animator?.isRunning == true
    }

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