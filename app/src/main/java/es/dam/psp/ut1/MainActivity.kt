package es.dam.psp.ut1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import es.dam.psp.ut1.servicio.DescargaService

class MainActivity : AppCompatActivity() {

    // Cambiado: Ahora usa la sintaxis correcta del launcher sin el bloque innecesario
    private val pedirPermiso = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        Log.d(
            DescargaService.TAG,
            "MainActivity · PID=${Process.myPid()} · hilo=${Thread.currentThread().name}"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvPid = findViewById<TextView>(R.id.tvPid)
        tvPid.text = "PID de la actividad: ${Process.myPid()}"

        Log.d(
            DescargaService.TAG,
            "MainActivity · PID=${Process.myPid()} · hilo=${Thread.currentThread().name}"
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val btnIniciar = findViewById<Button>(R.id.btnIniciar)
        val btnParar = findViewById<Button>(R.id.btnParar)

        btnIniciar.setOnClickListener {
            ContextCompat.startForegroundService(
                this,
                Intent(this, DescargaService::class.java)
            )
        }

        btnParar.setOnClickListener {
            stopService(Intent(this, DescargaService::class.java))
        }
    }
}