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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.runtime.setValue
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cirupied2.presentation.ListModel
import com.example.cirupied2.presentation.ListaApi
import com.example.cirupied2.presentation.ListaApi2
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date

import com.example.cirupied2.R
import com.example.cirupied2.presentation.theme.Cirupied2Theme

class MainActivity : ComponentActivity() {
    private val URL_BASE = "https://rest-api2-three.vercel.app/api/"
    private val CHANNEL_ID = "new_order_channel"
    private val NOTIFICATION_ID = 1

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
                val navController = rememberNavController()
                val isSplashScreenVisible = remember { mutableStateOf(true) }

                // Launch effect to hide splash screen after a delay
                LaunchedEffect(Unit) {
                    delay(3000) // Duración del SplashScreen
                    isSplashScreenVisible.value = false
                }

                // Show SplashScreen or main content based on the visibility state
                if (isSplashScreenVisible.value) {
                    SplashScreen(content = {
                        // When SplashScreen is finished, navigate to the main content
                        NavHost(navController, startDestination = "home") {
                            composable("home") { HomeScreen(navController) }
                            composable("citas") { CitasScreen(navController, URL_BASE) }
                            composable("citasHoyHora") { CitasHoyHoraScreen(navController, URL_BASE) }
                        }
                    })
                } else {
                    NavHost(navController, startDestination = "home") {
                        composable("home") { HomeScreen(navController) }
                        composable("citas") { CitasScreen(navController, URL_BASE) }
                        composable("citasHoyHora") { CitasHoyHoraScreen(navController, URL_BASE) }
                    }
                }
            }
        }
    }

    private var requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // Manejar el caso donde el permiso no ha sido concedido
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

    private fun sendNotification(cita: ListModel) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.splash_icon) // Reemplaza con tu ícono de notificación
                .setContentTitle("Nuevo Cita")
                .setContentText("Cita de ${cita.Nombre}")
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
fun SplashScreen(content: @Composable () -> Unit) {
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(3000) // Retraso de 3 segundos
        isLoading = false // Desactivar el indicador de carga después del retraso
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo2), // Reemplaza con tu ícono
                contentDescription = "App Icon",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            )
        }
    } else {
        content() // Mostrar contenido después del SplashScreen
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(1.dp)
            .background(Color(0xFFE0F7FA)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

            Text(
                text = "Bienvenido",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Image(
                painter = painterResource(id = R.drawable.logo2), // Reemplaza con tu ícono
                contentDescription = "App Logo",
                modifier = Modifier.size(36.dp)
            )

        Spacer(modifier = Modifier.height(1.dp))
        Button(onClick = { navController.navigate("citas") }) {
            Text(
                "Citas",
                fontSize = 10.sp // Cambia el tamaño del texto
            )
        }
        Spacer(modifier = Modifier.height(1.dp) )
        Button(
            onClick = { navController.navigate("citasHoyHora") }
        // Agrega padding si es necesario
        ) {
            Text(
                "Citas faltantes",
                fontSize = 10.sp, // Cambia el tamaño del texto

            )
        }
    }
}

@Composable
fun CitasScreen(navController: NavController, baseUrl: String) {
    var shouldRefreshList by remember { mutableStateOf(true) }
    val listaPedidos = remember { mutableStateListOf<ListModel>() }

    LaunchedEffect(shouldRefreshList) {
        if (shouldRefreshList) {
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val service = retrofit.create(ListaApi::class.java)
            try {
                val response = service.getList()
                listaPedidos.clear()
                listaPedidos.addAll(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            shouldRefreshList = false
        }
    }

    PedidoList(
        listaPedidos = listaPedidos,
        navController = navController,
        onRefreshRequested = { shouldRefreshList = true }
    )
}

@Composable
fun CitasHoyHoraScreen(navController: NavController, baseUrl: String) {
    var shouldRefreshList by remember { mutableStateOf(true) }
    val listaPedidos = remember { mutableStateListOf<ListModel>() }

    LaunchedEffect(shouldRefreshList) {
        if (shouldRefreshList) {
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val service = retrofit.create(ListaApi2::class.java)
            try {
                val response = service.getList()
                listaPedidos.clear()
                listaPedidos.addAll(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            shouldRefreshList = false
        }
    }

    PedidoList(
        listaPedidos = listaPedidos,
        navController = navController,
        onRefreshRequested = { shouldRefreshList = true }
    )
}

@Composable
fun PedidoList(
    listaPedidos: List<ListModel>,
    navController: NavController,
    onRefreshRequested: () -> Unit
) {
    val listState = remember { LazyListState() }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0F7FA))
    ) {
        // Titulo "Citas" y logo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 45.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Citas",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            Image(
                painter = painterResource(id = R.drawable.logo2),
                contentDescription = "App Logo",
                modifier = Modifier.size(36.dp)
            )
        }

        // Lista de citas o mensaje de no hay citas
        if (listaPedidos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize().weight(1f)
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay citas disponibles.",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(listaPedidos) { cita ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Cita de ${cita.Nombre}",
                                fontSize = 18.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "Horario: ${formatDate(cita.HorarioInicio)} - ${formatDate(cita.HoraFin)}",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "Descripción: ${cita.Descripcion}",
                                fontSize = 14.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Botón de Home en el pie de la lista con padding solo en la parte inferior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp), // Padding solo en la parte inferior
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = {
                    navController.navigate("home")
                }
                    ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Home",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp) // Ajusta el tamaño del icono
                )
            }
            IconButton(
                onClick = {
                    // Refrescar la lista y volver al inicio
                    onRefreshRequested()
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Inicio",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp) // Ajusta el tamaño del icono
                )
            }

        }
    }
}



fun formatDate(apiDateString: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val outputFormat = SimpleDateFormat("HH:mm")

    return try {
        val date = inputFormat.parse(apiDateString)
        outputFormat.format(date)
    } catch (e: Exception) {
        // Manejar cualquier error de análisis de fecha aquí
        "Fecha no válida"
    }
}