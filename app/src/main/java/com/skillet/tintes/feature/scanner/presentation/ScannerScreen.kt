package com.skillet.tintes.feature.scanner.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.skillet.tintes.core.theme.ThemeDialog
import com.skillet.tintes.core.theme.ThemeMode
import com.skillet.tintes.core.theme.ThemePreferences
import com.skillet.tintes.core.util.BarcodeAnalyzer
import com.skillet.tintes.core.util.ScannerFeedbackHelper
import com.skillet.tintes.core.util.Sucursales
import com.skillet.tintes.ui.theme.*
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    sucursalId: String,
    themePreferences: ThemePreferences = koinInject(),
    viewModel: ScannerViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val nombreSucursal = remember(sucursalId) { Sucursales.obtenerNombre(sucursalId) }

    val currentTheme by themePreferences.themeMode.collectAsState()
    val isDark = when (currentTheme) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    var showThemeDialog by remember { mutableStateOf(false) }

    var showManualInputDialog by remember { mutableStateOf(false) }
    var inputManualText by remember { mutableStateOf("") }

    // Toast de notificaciones (ej: al deshacer venta)
    LaunchedEffect(uiState.mensajeToast) {
        uiState.mensajeToast?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.limpiarMensajeToast()
        }
    }

    if (showThemeDialog) {
        ThemeDialog(
            currentThemeMode = currentTheme,
            onThemeSelected = { newMode ->
                themePreferences.setThemeMode(newMode)
            },
            onDismissRequest = { showThemeDialog = false }
        )
    }

    // Diálogo de Ingreso Manual por Teclado
    if (showManualInputDialog) {
        AlertDialog(
            onDismissRequest = { showManualInputDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "⌨️ Ingreso Manual por Teclado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Ingresa el código de barras del tinte si la etiqueta o cámara no responde:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputManualText,
                        onValueChange = { inputManualText = it },
                        label = { Text("Código de barras o clave") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputManualText.isNotBlank()) {
                            val code = inputManualText.trim()
                            showManualInputDialog = false
                            val aceptado = viewModel.onBarcodeScanned(sucursalId, code)
                            if (aceptado) {
                                ScannerFeedbackHelper.playBeepAndVibrate(context)
                            }
                        }
                    },
                    enabled = inputManualText.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                ) {
                    Text("Registrar Venta", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showManualInputDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Diálogos de Confirmación / Alerta
    uiState.dialogo?.let { dialogo ->
        AlertDialog(
            onDismissRequest = { viewModel.cerrarDialogo() },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            title = {
                val (bg, textColor, border, titleText) = when (dialogo) {
                    is DialogoVenta.Exito -> Quadruple(
                        if (isDark) SuccessGreenBgDark else SuccessGreenBg,
                        if (isDark) SuccessGreenTextDark else SuccessGreenText,
                        if (isDark) SuccessGreenBorderDark else SuccessGreenBorder,
                        "✔ Venta Registrada"
                    )
                    is DialogoVenta.SinStock -> Quadruple(
                        if (isDark) CriticalRedBgDark else CriticalRedBg,
                        if (isDark) CriticalRedTextDark else CriticalRedText,
                        if (isDark) CriticalRedBorderDark else CriticalRedBorder,
                        "⚠️ Sin Stock Disponible"
                    )
                    is DialogoVenta.Error -> Quadruple(
                        if (isDark) WarningAmberBgDark else WarningAmberBg,
                        if (isDark) WarningAmberTextDark else WarningAmberText,
                        if (isDark) WarningAmberBorderDark else WarningAmberBorder,
                        "❌ Atención"
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = bg,
                    border = BorderStroke(1.dp, border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = titleText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = textColor
                    )
                }
            },
            text = {
                when (dialogo) {
                    is DialogoVenta.Exito -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Text(
                                    text = dialogo.marca.uppercase(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = dialogo.tono,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Se ha descontado 1 pieza del inventario.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDark) SuccessGreenBgDark else SuccessGreenBg,
                                border = BorderStroke(1.dp, if (isDark) SuccessGreenBorderDark else SuccessGreenBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Quedan en existencia: ${dialogo.stockRestante} piezas",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isDark) SuccessGreenTextDark else SuccessGreenText
                                )
                            }
                        }
                    }
                    is DialogoVenta.SinStock -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Text(
                                    text = dialogo.marca.uppercase(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = dialogo.tono,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDark) CriticalRedBgDark else CriticalRedBg,
                                border = BorderStroke(1.dp, if (isDark) CriticalRedBorderDark else CriticalRedBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "No se puede registrar la venta porque el stock actual en esta sucursal es 0.",
                                    modifier = Modifier.padding(12.dp),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = if (isDark) CriticalRedTextDark else CriticalRedText
                                )
                            }
                        }
                    }
                    is DialogoVenta.Error -> {
                        Text(
                            text = dialogo.mensaje,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dialogo is DialogoVenta.Exito) {
                        OutlinedButton(
                            onClick = {
                                viewModel.deshacerVenta(sucursalId, dialogo.codigoBarras)
                            },
                            enabled = !uiState.deshaciendoVenta,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CriticalRedText),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CriticalRedText)
                        ) {
                            if (uiState.deshaciendoVenta) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = CriticalRedText,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("↩ Deshacer", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.cerrarDialogo() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (dialogo) {
                                is DialogoVenta.Exito -> BrandOrange
                                is DialogoVenta.SinStock -> CriticalRedText
                                is DialogoVenta.Error -> MaterialTheme.colorScheme.onSurface
                            },
                            contentColor = Color.White
                        )
                    ) {
                        Text("Aceptar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.border(width = 1.dp, color = MaterialTheme.colorScheme.outline)
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Control de Mostrador",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isDark) BrandOrangeDarkContainer else BrandOrangeLight,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = "📍 $nombreSucursal",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Text(
                                text = "Escáner de salidas e inventario",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showThemeDialog = true }) {
                            Text(
                                text = currentTheme.icon,
                                fontSize = 20.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            if (uiState.camaraAbierta) {
                if (hasCameraPermission) {
                    // Cámara activa
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val executor = Executors.newSingleThreadExecutor()
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()
                                    .also {
                                        it.setAnalyzer(executor, BarcodeAnalyzer { barcode ->
                                            val aceptado = viewModel.onBarcodeScanned(sucursalId, barcode)
                                            if (aceptado) {
                                                ScannerFeedbackHelper.playBeepAndVibrate(ctx)
                                            }
                                        })
                                    }

                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalysis
                                )
                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Marco visual de escaneo
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Slate900.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text(
                                text = "Alinea el código de barras dentro del marco",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(290.dp, 170.dp)
                                .border(2.5.dp, BrandOrange, RoundedCornerShape(16.dp))
                                .background(Color.Transparent)
                        )

                        Button(
                            onClick = { viewModel.cerrarCamara() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Slate900.copy(alpha = 0.85f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .padding(bottom = 24.dp)
                                .height(48.dp)
                        ) {
                            Text("Cancelar Escaneo", fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    LaunchedEffect(Unit) {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            } else {
                // Pantalla de espera / bienvenida minimalista tipo SaaS
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(1.dp, BrandOrange.copy(alpha = 0.2f)),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = "📷", fontSize = 36.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Text(
                                    text = "📍 Sucursal $nombreSucursal",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Escanear Salida de Producto",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Apunta la cámara al código de barras del tinte vendido para descontar automáticamente 1 pieza del inventario.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = { viewModel.abrirCamara() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandOrange,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "📷 Escanear con Cámara",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = {
                                    inputManualText = ""
                                    showManualInputDialog = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(
                                    text = "⌨️ Ingreso Manual por Teclado",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (uiState.procesando) {
                                Spacer(modifier = Modifier.height(20.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = BrandOrange,
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Procesando código de barras...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
