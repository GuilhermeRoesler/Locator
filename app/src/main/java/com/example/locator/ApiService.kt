package com.example.locator

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("send_location/{id}") // Exemplo: "api/location"
    fun sendLocation(
        @Path("id") userId: Int,
        @Body locationData: LocationData
    ): Call<Void>
}