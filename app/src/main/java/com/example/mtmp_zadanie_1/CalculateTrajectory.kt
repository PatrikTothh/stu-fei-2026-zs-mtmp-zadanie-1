package com.example.mtmp_zadanie_1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CalculateTrajectory : AppCompatActivity() {

    private lateinit var editTextInitialVelocity: EditText
    private lateinit var editTextAngle: EditText
    private lateinit var buttonCalculate: Button
    private lateinit var buttonList: Button
    private lateinit var buttonGraph: Button
    private lateinit var buttonAnimation: Button

    private val trajectoryPointsParcel = ArrayList<TrajectoryPointParcel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calculate_trajectory)

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

        editTextInitialVelocity = findViewById(R.id.editTextInitialVelocity)
        editTextAngle = findViewById(R.id.editTextAngle)
        buttonCalculate = findViewById(R.id.buttonCalculate)
        buttonList = findViewById(R.id.buttonList)
        buttonGraph = findViewById(R.id.buttonGraph)
        buttonAnimation = findViewById(R.id.buttonAnimation)

        buttonCalculate.setOnClickListener {
            calculateFromServer()
        }

        buttonList.setOnClickListener {
            openTrajectoryScreen(TrajectoryListActivity::class.java, "list")
        }

        buttonGraph.setOnClickListener {
            openTrajectoryScreen(TrajectoryGraphActivity::class.java, "graph")
        }

        buttonAnimation.setOnClickListener {
            openTrajectoryScreen(TrajectoryAnimationActivity::class.java, "animation")
        }
    }

    private fun calculateFromServer() {
        val velocity = editTextInitialVelocity.text.toString().toDoubleOrNull()
        val angle = editTextAngle.text.toString().toDoubleOrNull()

        if (velocity == null || angle == null) {
            Toast.makeText(
                this,
                "Enter valid velocity and angle",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (!velocity.isFinite() || !angle.isFinite() ||
            velocity <= 0 || angle <= 0 || angle >= 90
        ) {
            Toast.makeText(
                this,
                "Velocity must be > 0 and angle must be between 0 and 90",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.calculateTrajectory(
                        TrajectoryRequest(
                            initial_velocity = velocity,
                            angle = angle
                        )
                    )
                }

                if (response.success && response.points != null) {
                    trajectoryPointsParcel.clear()
                    trajectoryPointsParcel.addAll(
                        response.points.map {
                            TrajectoryPointParcel(
                                time = it.time,
                                x = it.x,
                                y = it.y
                            )
                        }
                    )

                    Toast.makeText(
                        this@CalculateTrajectory,
                        "Received ${trajectoryPointsParcel.size} points from server",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@CalculateTrajectory,
                        "Server returned an error",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("SERVER", "Request failed", e)

                Toast.makeText(
                    this@CalculateTrajectory,
                    "Server error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun openTrajectoryScreen(
        activityClass: Class<out AppCompatActivity>,
        screenName: String
    ) {
        if (trajectoryPointsParcel.isEmpty()) {
            Toast.makeText(
                this,
                "Calculate trajectory first to see the $screenName",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val intent = Intent(this, activityClass).apply {
            putParcelableArrayListExtra(
                "trajectory_data",
                ArrayList(trajectoryPointsParcel)
            )
        }

        startActivity(intent)
    }
}