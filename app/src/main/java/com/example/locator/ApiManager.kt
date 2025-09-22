package com.example.locator

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiManager() {
    private val BASE_URL = "https://souls.pythonanywhere.com/api/"
    private var userId: Int = 0

    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun setUserId(id: Int) {
        userId = id
    }

    fun sendLocationToApi(latitude: Double, longitude: Double) {
        val locationData = LocationData(latitude, longitude)
        apiService.sendLocation(userId, locationData).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("API", "Localização enviada com sucesso para a API!")
                } else {
                    Log.e("API", "Erro ao enviar localização: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("API", "Falha na conexão de rede: ${t.message}")
            }
        })
    }
}