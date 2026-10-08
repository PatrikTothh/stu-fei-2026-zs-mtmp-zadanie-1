package com.example.mtmp_zadanie_1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

class CalculateTrajectory : AppCompatActivity() {
    private lateinit var editTextInitialVelocity: EditText
    private lateinit var editTextAngle: EditText
    private lateinit var buttonCalculate: Button
    private lateinit var buttonList: Button
    private lateinit var buttonGraph: Button
    private lateinit var buttonAnimation: Button
    private val trajectoryPointsParcel = ArrayList<TrajectoryPointParcel>()
    private val GRAVITY = 9.81
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calculate_trajectory)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        editTextInitialVelocity = findViewById(R.id.editTextInitialVelocity)
        editTextAngle = findViewById(R.id.editTextAngle)
        buttonCalculate = findViewById(R.id.buttonCalculate)
        buttonList = findViewById(R.id.buttonList)
        buttonGraph = findViewById(R.id.buttonGraph)
        buttonAnimation = findViewById(R.id.buttonAnimation)

        requestLocalNetworkPermission()

        buttonCalculate.setOnClickListener {
            Toast.makeText(
                this,
                "Calculate button works",
                Toast.LENGTH_SHORT
            ).show()

            calculateFromServer()
        }

        buttonList.setOnClickListener {
            if(trajectoryPointsParcel.isNotEmpty()){
                val intent = Intent(this, TrajectoryListActivity::class.java)
                intent.putParcelableArrayListExtra("trajectory_data",
                    trajectoryPointsParcel)
                startActivity(intent)
            }else{
                Toast.makeText(this,"Calculate trajectory first to see the list",
                    Toast.LENGTH_SHORT).show()
            }
        }
        buttonGraph.setOnClickListener {
            if (trajectoryPointsParcel.isNotEmpty()) {
                val intent = Intent(this, TrajectoryGraphActivity::class.java)
                intent.putParcelableArrayListExtra(
                    "trajectory_data",
                    trajectoryPointsParcel
                )
                startActivity(intent)
            } else {
                Toast.makeText(
                    this,
                    "Calculate trajectory first to see the graph",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        buttonAnimation.setOnClickListener {

            if (trajectoryPointsParcel.isNotEmpty()) {

                val intent = Intent(
                    this,
                    TrajectoryAnimationActivity::class.java
                )

                intent.putParcelableArrayListExtra(
                    "trajectory_data",
                    trajectoryPointsParcel
                )

                startActivity(intent)

            } else {

                Toast.makeText(
                    this,
                    "Calculate trajectory first to see the animation",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun calculateTrajectory(){
        val initialVelocityStr = editTextInitialVelocity.text.toString()
        val angleStr = editTextAngle.text.toString()

        if(initialVelocityStr.isEmpty() || angleStr.isEmpty()){
            Toast.makeText(this,"Please eneter both velocity and angle",
                Toast.LENGTH_SHORT).show()
            return
        }
        val initialVelocity = initialVelocityStr.toDoubleOrNull()
        val angleDegrees = angleStr.toDoubleOrNull()

        if(initialVelocity == null || angleDegrees == null || initialVelocity <= 0 ||
            angleDegrees <= 0 || angleDegrees >= 90){
            Toast.makeText(this,"Invalid input. Velocity and angle must be positive, and angle < 90°",
                Toast.LENGTH_SHORT).show()
            return
        }

        val angleRadians = Math.toRadians(angleDegrees)
        trajectoryPointsParcel.clear()

        val timeOfFlight = (2 * initialVelocity * sin(angleRadians)) / GRAVITY
        val timeStep = 0.1
        var currentTime = 0.0

        if (timeOfFlight >= 0){
            trajectoryPointsParcel.add(TrajectoryPointParcel(0.0,0.0,0.0))
        }

        while (currentTime <= timeOfFlight){
            currentTime += timeStep
            if(currentTime > timeOfFlight && timeOfFlight > 0){
                currentTime = timeOfFlight
            }

            val x = initialVelocity * cos(angleRadians) * currentTime
            val y = (initialVelocity * sin(angleRadians) * currentTime) -
                    (0.5 * GRAVITY * currentTime.pow(2))

            if(y >= 0){
                trajectoryPointsParcel.add(TrajectoryPointParcel(currentTime,x,y))
            }else{
                if(trajectoryPointsParcel.isNotEmpty() && trajectoryPointsParcel.last().y > 0){
                    if(currentTime != timeOfFlight){
                        val finalX = initialVelocity * cos(angleRadians) * timeOfFlight
                        trajectoryPointsParcel.add(TrajectoryPointParcel(timeOfFlight,finalX,0.0))
                    }
                }
                break
            }
            if(currentTime == timeOfFlight) break
        }

        if(trajectoryPointsParcel.isNotEmpty() && trajectoryPointsParcel.last().y > 0.001 &&
            timeOfFlight > 0){
            val finalX = initialVelocity * cos(angleRadians) * timeOfFlight
            if(trajectoryPointsParcel.last().time < timeOfFlight){
                trajectoryPointsParcel.add(TrajectoryPointParcel(timeOfFlight,finalX,0.0))
            }else{
                trajectoryPointsParcel[trajectoryPointsParcel.size -1] = TrajectoryPointParcel(
                    timeOfFlight,finalX,0.0)
            }
        }

        Toast.makeText(this, "Calculation performed. ${trajectoryPointsParcel.size} points " +
                "generated. Click 'List data' to view.", Toast.LENGTH_SHORT).show()
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

        if (velocity <= 0 || angle <= 0 || angle >= 90) {
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

    private fun requestLocalNetworkPermission() {
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_LOCAL_NETWORK
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_LOCAL_NETWORK),
                100
            )
        }
    }

}