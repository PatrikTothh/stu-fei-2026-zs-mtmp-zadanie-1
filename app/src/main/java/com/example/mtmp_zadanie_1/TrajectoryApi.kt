package com.example.mtmp_zadanie_1

import retrofit2.http.Body
import retrofit2.http.POST

interface TrajectoryApi {

    @POST("calculate")
    suspend fun calculateTrajectory(
        @Body request: TrajectoryRequest
    ): TrajectoryResponse
}