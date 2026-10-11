package com.example.mtmp_zadanie_1

import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat

class TrajectoryListActivity : AppCompatActivity() {

    private lateinit var listViewTrajectory: ListView

    private val decimalFormat = DecimalFormat("#.##")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trajectory_list)

        listViewTrajectory = findViewById(R.id.listViewTrajectory)

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

        if (points.isNullOrEmpty()) {
            Toast.makeText(
                this,
                "No trajectory data found.",
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }

        val rows = points.map {
            "T: ${decimalFormat.format(it.time)}s, " +
                    "X: ${decimalFormat.format(it.x)}m, " +
                    "Y: ${decimalFormat.format(it.y)}m"
        }

        listViewTrajectory.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            rows
        )

        supportActionBar?.title = "Trajectory List"
    }
}