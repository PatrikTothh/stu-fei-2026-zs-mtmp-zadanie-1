package com.example.mtmp_zadanie_1

import android.content.Intent
import android.health.connect.datatypes.units.Velocity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.pow

class CalculateTrajectory : AppCompatActivity() {
    private lateinit var editTextInitialVelocity: EditText
    private lateinit var editTextAngle: EditText
    private lateinit var buttonCalculate: Button
    private lateinit var buttonList: Button
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
    }
}