package com.example.mtmp_zadanie_1

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import java.util.Locale
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow

class TrajectoryAnimationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var trajectoryPoints = ArrayList<TrajectoryPointParcel>()

    private var animator: ValueAnimator? = null
    private var currentPointIndex = 0

    // Convert dp to pixels for consistent sizing across devices.
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
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
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

    fun setTrajectory(points: ArrayList<TrajectoryPointParcel>) {
        stopAnimation()
        trajectoryPoints = ArrayList(points)
        currentPointIndex = 0
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw a white background.
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

        // Define margins around the graph.
        val leftMargin = dp(65f)
        val rightMargin = dp(25f)
        val topMargin = dp(65f)
        val bottomMargin = dp(100f)

        val graphWidth = width - leftMargin - rightMargin
        val graphHeight = height - topMargin - bottomMargin

        if (graphWidth <= 0f || graphHeight <= 0f) return

        // Calculate the maximum horizontal and vertical positions.
        val maxX = max(
            trajectoryPoints.maxOf { it.x },
            0.1
        ) * 1.05

        val maxY = max(
            trajectoryPoints.maxOf { it.y },
            0.1
        ) * 1.10

        // Use equal scaling to preserve the trajectory's physical shape.
        val pixelsPerMeter = minOf(
            graphWidth / maxX.toFloat(),
            graphHeight / maxY.toFloat()
        )

        if (pixelsPerMeter <= 0f) return

        val graphMaxX = graphWidth / pixelsPerMeter
        val graphMaxY = graphHeight / pixelsPerMeter

        // Convert trajectory coordinates into screen coordinates.
        fun screenX(x: Double): Float {
            return leftMargin + (x * pixelsPerMeter).toFloat()
        }

        fun screenY(y: Double): Float {
            return height - bottomMargin -
                    (y * pixelsPerMeter).toFloat()
        }

        // Calculate readable grid intervals.
        val xStep = calculateStep(graphMaxX.toDouble())
        val yStep = calculateStep(graphMaxY.toDouble())

        // Draw vertical grid lines and horizontal-position labels.
        var x = 0.0

        while (x <= graphMaxX + xStep * 0.001) {
            val px = screenX(x)

            canvas.drawLine(
                px,
                topMargin,
                px,
                height - bottomMargin,
                gridPaint
            )

            textPaint.textAlign = Paint.Align.CENTER

            canvas.drawText(
                formatAxisNumber(x),
                px,
                height - bottomMargin + dp(20f),
                textPaint
            )

            x += xStep
        }

        // Draw horizontal grid lines and vertical-position labels.
        var y = 0.0

        while (y <= graphMaxY + yStep * 0.001) {
            val py = screenY(y)

            canvas.drawLine(
                leftMargin,
                py,
                width - rightMargin,
                py,
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

        // Draw the horizontal and vertical axes.
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

        // Draw the trajectory completed by the projectile so far.
        val lastIndex = currentPointIndex.coerceIn(
            0,
            trajectoryPoints.lastIndex
        )

        val path = Path()

        for (index in 0..lastIndex) {
            val point = trajectoryPoints[index]
            val px = screenX(point.x)
            val py = screenY(point.y)

            if (index == 0) {
                path.moveTo(px, py)
            } else {
                path.lineTo(px, py)
            }
        }

        canvas.drawPath(path, trajectoryPaint)

        // Draw the current projectile position.
        val projectile = trajectoryPoints[lastIndex]

        canvas.drawCircle(
            screenX(projectile.x),
            screenY(projectile.y),
            dp(6f),
            pointPaint
        )

        // Draw the graph title.
        canvas.drawText(
            "Projectile Trajectory",
            width / 2f,
            dp(35f),
            titlePaint
        )

        // Draw the horizontal-axis label.
        textPaint.textAlign = Paint.Align.CENTER

        canvas.drawText(
            "Horizontal Distance x (m)",
            leftMargin + graphWidth / 2f,
            height - dp(50f),
            textPaint
        )

        // Draw the vertical-axis label.
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

    fun startAnimation() {
        if (trajectoryPoints.isEmpty()) return

        // Cancel any previous animation and start from the beginning.
        animator?.cancel()
        animator = null

        currentPointIndex = 0
        invalidate()

        if (trajectoryPoints.size == 1) return

        animator = ValueAnimator.ofInt(
            0,
            trajectoryPoints.lastIndex
        ).apply {
            // Set the total animation duration.
            duration = 5000L

            interpolator = LinearInterpolator()

            addUpdateListener { animation ->
                currentPointIndex = animation.animatedValue as Int
                invalidate()
            }

            start()
        }
    }

    fun restartAnimation() {
        // Restart the animation from the initial position.
        startAnimation()
    }

    fun stopAnimation() {
        // Cancel the animation without leaving it running in the background.
        animator?.cancel()
        animator = null

        currentPointIndex = 0
        invalidate()
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
                Locale.US,
                "%.1f",
                value
            ).trimEnd('0').trimEnd('.')
        }
    }
}