package com.example.mtmp_zadanie_1

import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TrajectoryAnimationActivity : AppCompatActivity() {

    private lateinit var animationView: TrajectoryAnimationView
    private lateinit var buttonPlay: Button
    private lateinit var buttonRestart: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_trajectory_animation)

        // Apply system bar insets to the main layout.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        // Initialize the animation view and buttons.
        animationView = findViewById(R.id.animationView)
        buttonPlay = findViewById(R.id.buttonPlay)
        buttonRestart = findViewById(R.id.buttonRestart)

        // Retrieve trajectory data from the intent.
        val points: ArrayList<TrajectoryPointParcel>? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayListExtra(
                    "trajectory_data",
                    TrajectoryPointParcel::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableArrayListExtra("trajectory_data")
            }

        // Stop if no trajectory data is available.
        if (points.isNullOrEmpty()) {
            Toast.makeText(
                this,
                "No trajectory data found.",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        animationView.setTrajectory(points)

        // Start the animation from the beginning.
        buttonPlay.text = "Play"

        buttonPlay.setOnClickListener {
            animationView.startAnimation()
        }

        // Reset and immediately replay the animation.
        buttonRestart.setOnClickListener {
            animationView.restartAnimation()
        }
    }

    override fun onPause() {
        if (::animationView.isInitialized) {
            animationView.stopAnimation()
        }

        super.onPause()
    }
}