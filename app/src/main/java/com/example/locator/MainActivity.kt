package com.example.locator

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private lateinit var textLocationStatus: TextView
    private lateinit var buttonStart: Button
    private lateinit var buttonStop: Button

    // O requestPermissionLauncher agora lida com as permissões em primeiro e segundo plano
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val backgroundLocationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] ?: false
        } else {
            true // Não é necessário em versões mais antigas
        }

        if (fineLocationGranted) {
            textLocationStatus.text = "Permissão de localização em primeiro plano concedida. Agora, por favor, permita o tempo todo."
            if (backgroundLocationGranted) {
                // Todas as permissões necessárias foram concedidas.
                startLocationService()
            } else {
                // Se a permissão de segundo plano não foi concedida (apenas em Android 10+), solicitamos novamente
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestBackgroundLocationPermission()
                } else {
                    // Se a versão é anterior ao Android 10, podemos iniciar o serviço.
                    startLocationService()
                }
            }
        } else {
            textLocationStatus.text = "Permissão de localização negada."
            Log.e("Permissão", "Permissão de localização negada pelo usuário.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        textLocationStatus = findViewById(R.id.textLocationStatus)
        buttonStart = findViewById(R.id.buttonStart)
        buttonStop = findViewById(R.id.buttonStop)

        val userId = intent.getIntExtra("USER_ID", -1)
        if (userId != -1) {
            textLocationStatus.text = "ID do usuário: $userId"
        }

        // Botão para iniciar o serviço
        buttonStart.setOnClickListener {
            // Verifica e solicita permissões antes de iniciar o serviço
            if (checkLocationPermissions()) {
                startLocationService()
            }
        }

        // Botão para parar o serviço
        buttonStop.setOnClickListener {
            stopLocationService()
        }

        // Verificação inicial das permissões
        if (!checkLocationPermissions()) {
            requestLocationPermissions()
        } else {
            textLocationStatus.text = "Permissões concedidas. Pressione Iniciar para começar o rastreamento."
        }
    }

    private fun checkLocationPermissions(): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val backgroundLocationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Não é necessário em versões mais antigas
        }
        return fineLocationGranted && backgroundLocationGranted
    }

    private fun requestLocationPermissions() {
        val permissionsToRequest = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
        }
    }

    private fun startLocationService() {
        if (checkLocationPermissions()) {
            textLocationStatus.text = "Iniciando rastreamento em segundo plano..."
            val serviceIntent = Intent(this, LocationService::class.java)
            serviceIntent.putExtra("USER_ID", intent.getIntExtra("USER_ID", -1))
            ContextCompat.startForegroundService(this, serviceIntent)
        } else {
            requestLocationPermissions()
        }
    }

    private fun stopLocationService() {
        textLocationStatus.text = "Rastreamento em segundo plano parado."
        val serviceIntent = Intent(this, LocationService::class.java)
        stopService(serviceIntent)
    }
}
