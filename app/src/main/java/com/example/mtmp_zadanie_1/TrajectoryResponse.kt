package com.example.mtmp_zadanie_1

data class TrajectoryResponse(
    val success: Boolean,
    val points: List<TrajectoryResponsePoint>?
)

data class TrajectoryResponsePoint(
    val time: Double,
    val x: Double,
    val y: Double
)