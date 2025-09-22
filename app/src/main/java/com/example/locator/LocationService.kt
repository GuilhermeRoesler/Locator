package com.example.locator

import android.Manifest
import android.content.Intent
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class LocationService : Service() {

    // Constantes para o Foreground Service
    private val NOTIFICATION_CHANNEL_ID = "LocationChannel"
    private val NOTIFICATION_ID = 101

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var apiManager: ApiManager
    private var job: Job? = null

    // Callback para as atualizações de localização
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            locationResult.lastLocation?.let { location ->
                Log.d("LocationService", "Localização recebida: ${location.latitude}, ${location.longitude}")
                // Envia a localização para a API
                apiManager.sendLocationToApi(location.latitude, location.longitude)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        apiManager = ApiManager() // Assume que ApiManager é uma classe existente
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Inicia o serviço em primeiro plano, exibindo uma notificação
        startForeground(NOTIFICATION_ID, createNotification())

        intent?.let {
            val userId = it.getIntExtra("USER_ID", -1)
            if (userId != -1) {
                apiManager.setUserId(userId)
            } else {
                Log.e("LocationService", "ID de usuário não encontrado no Intent.")
            }
        }

        // Configura as requisições de localização
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            TimeUnit.SECONDS.toMillis(10) // Intervalo de 10 segundos
        )
            .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(5)) // Intervalo mínimo entre updates
            .build()

        // Verifica a permissão antes de iniciar o rastreamento
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Inicia o rastreamento de localização
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            // A permissão foi negada, não é possível rastrear. Para o serviço.
            Log.e("LocationService", "Permissão de localização negada. Parando o serviço.")
            stopSelf()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove as atualizações de localização para evitar leaks de memória
        fusedLocationClient.removeLocationUpdates(locationCallback)
        // Cancela a coroutine se estiver rodando
        job?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotification(): Notification {
        // Cria um canal de notificação para o Android Oreo e superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Rastreamento de Localização",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Cria a notificação para ser exibida na barra de status
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Rastreamento de Localização")
            .setContentText("Seu local está sendo enviado em segundo plano.")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use um ícone apropriado
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }
}
