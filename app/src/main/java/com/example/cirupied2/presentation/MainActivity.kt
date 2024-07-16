package com.example.cirupied2.presentation

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.wear.compose.material.Text

import androidx.compose.foundation.layout.fillMaxWidth
import com.example.cirupied.presentation.ListModel
import com.example.cirupied.presentation.ListaApi
import kotlinx.coroutines.delay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import com.example.cirupied2.R
import com.example.cirupied2.presentation.theme.Cirupied2Theme

class MainActivity : ComponentActivity() {
    var URL_BASE = "https://rest-api2-three.vercel.app/api/"
    var CHANNEL_ID = "new_order_channel"
    var NOTIFICATION_ID = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            Cirupied2Theme {
                val listaPedidos = remember { mutableStateListOf<ListModel>() }
                val previousSize = remember { mutableStateOf(0) }

                Box(modifier = Modifier.fillMaxSize()) {
                    PedidoList(listaPedidos)
                }

                // Lanzar una corrutina para obtener los datos de la lista
                LaunchedEffect(Unit) {
                    val retrofit = Retrofit.Builder()
                        .baseUrl(URL_BASE)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    val service = retrofit.create(ListaApi::class.java)

                    while (true) {
                        try {
                            val response = service.getList()
                            listaPedidos.clear()
                            listaPedidos.addAll(response)
                            response.forEach { pedido ->
                                Log.d("API_RESPONSE", pedido.toString())
                            }
                            if (listaPedidos.size > previousSize.value) {
                                sendNotification(listaPedidos.last())
                                previousSize.value = listaPedidos.size
                            }
                        } catch (e: Exception) {
                            // Manejar cualquier error aquí
                            e.printStackTrace()
                        }
                        delay(10000)
                    }
                }
            }
        }

    }

    var requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {

        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "New Order Channel"
            val descriptionText = "Channel for new order notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(Cita: ListModel) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.splash_icon) // Reemplaza con tu ícono de notificación
                .setContentTitle("Nuevo Cita")
                .setContentText("Cita de ${Cita.Nombre}")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(this)) {
                notify(NOTIFICATION_ID, builder.build())
            }
        } else {
            // Maneja el caso donde el permiso no ha sido concedido
            println("Permiso de notificaciones no concedido")
        }
    }
}

@Composable
fun PedidoList(listaPedidos: List<ListModel>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp), // Ajusta el padding horizontal
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(listaPedidos) { Cita ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Cita de ${Cita.Nombre}",
                        fontSize = 18.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Horario: ${Cita.HorarioInicio} - ${Cita.HoraFin}",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Descripción: ${Cita.Descripcion}",
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    // Puedes agregar más detalles de la cita si lo necesitas
                }
            }
        }
    }
}

