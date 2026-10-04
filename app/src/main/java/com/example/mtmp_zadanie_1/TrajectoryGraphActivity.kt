package com.example.mtmp_zadanie_1

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class TrajectoryGraphActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        val graphView = TrajectoryGraphView(
            this,
            trajectoryPoints ?: arrayListOf()
        )

        setContentView(graphView)

        supportActionBar?.title = "Trajectory Graph"
    }
}