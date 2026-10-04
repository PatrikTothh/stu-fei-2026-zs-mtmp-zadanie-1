package com.example.mtmp_zadanie_1

import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TrajectoryAnimationActivity : AppCompatActivity() {

    private lateinit var animationView: TrajectoryAnimationView
    private lateinit var buttonPlay: Button
    private lateinit var buttonRestart: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trajectory_animation)

        animationView = findViewById(R.id.animationView)
        buttonPlay = findViewById(R.id.buttonPlay)
        buttonRestart = findViewById(R.id.buttonRestart)

        val trajectoryPoints: ArrayList<TrajectoryPointParcel>? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayListExtra(
                    "trajectory_data",
                    TrajectoryPointParcel::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableArrayListExtra("trajectory_data")
            }

        if (trajectoryPoints == null || trajectoryPoints.isEmpty()) {
            Toast.makeText(
                this,
                "No trajectory data found.",
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }

        animationView.setTrajectory(trajectoryPoints)

        buttonPlay.setOnClickListener {
            if (animationView.isRunning()) {
                animationView.pauseAnimation()
                buttonPlay.text = "Play"
            } else {
                animationView.startAnimation()
                buttonPlay.text = "Pause"
            }
        }

        buttonRestart.setOnClickListener {
            animationView.restartAnimation()
            buttonPlay.text = "Pause"
        }
    }

    override fun onPause() {
        super.onPause()
        animationView.pauseAnimation()
    }
}