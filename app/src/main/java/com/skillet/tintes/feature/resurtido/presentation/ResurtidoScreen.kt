package com.skillet.tintes.feature.resurtido.presentation

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skillet.tintes.core.theme.ThemeDialog
import com.skillet.tintes.core.theme.ThemeMode
import com.skillet.tintes.core.theme.ThemePreferences
import com.skillet.tintes.core.util.Sucursales
import com.skillet.tintes.ui.theme.*
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResurtidoScreen(
    sucursalId: String,
    themePreferences: ThemePreferences = koinInject(),
    viewModel: ResurtidoViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val nombreSucursal = remember(sucursalId) { Sucursales.obtenerNombre(sucursalId) }

    val currentTheme by themePreferences.themeMode.collectAsState()
    val isDark = when (currentTheme) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    var showThemeDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        ThemeDialog(
            currentThemeMode = currentTheme,
            onThemeSelected = { newMode ->
                themePreferences.setThemeMode(newMode)
            },
            onDismissRequest = { showThemeDialog = false }
        )
    }

    LaunchedEffect(sucursalId) {
        viewModel.cargarFaltantes(sucursalId)
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
                                    text = "Recepción de Mercancía",
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
                                text = "Registro de entradas al inventario",
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
        },
        bottomBar = {
            if (uiState is ResurtidoUiState.Success) {
                val state = uiState as ResurtidoUiState.Success
                val totalPiezas = state.items.sumOf { it.cantidadARecibir }

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 1.dp, color = MaterialTheme.colorScheme.outline)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.guardarRecepcion(sucursalId) {
                                    Toast.makeText(
                                        context,
                                        "Entrada registrada exitosamente",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            enabled = !state.guardando && totalPiezas > 0,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandOrange,
                                contentColor = Color.White,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            if (state.guardando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    text = if (totalPiezas > 0) {
                                        "Confirmar Entrada al Inventario ($totalPiezas pzs)"
                                    } else {
                                        "Selecciona piezas a recibir"
                                    },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is ResurtidoUiState.Loading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = BrandOrange,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Cargando faltantes de inventario...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is ResurtidoUiState.Empty -> {
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📦",
                                fontSize = 42.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Inventario Completo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "No hay tintes por debajo del stock mínimo en esta sucursal.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            OutlinedButton(
                                onClick = { viewModel.cargarFaltantes(sucursalId) },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BrandOrange),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandOrange)
                            ) {
                                Text("Actualizar lista", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                is ResurtidoUiState.Error -> {
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CriticalRedBg.copy(alpha = 0.2f)),
                        border = BorderStroke(1.dp, CriticalRedBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "⚠️",
                                fontSize = 36.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Error al cargar información",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CriticalRedText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = CriticalRedText
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.cargarFaltantes(sucursalId) },
                                colors = ButtonDefaults.buttonColors(containerColor = CriticalRedText)
                            ) {
                                Text("Reintentar", color = Color.White)
                            }
                        }
                    }
                }
                is ResurtidoUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.items, key = { it.dto.codigoBarras }) { item ->
                            val estaSeleccionado = item.cantidadARecibir > 0

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (estaSeleccionado) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    width = if (estaSeleccionado) 1.5.dp else 1.dp,
                                    color = if (estaSeleccionado) BrandOrange else MaterialTheme.colorScheme.outline
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        // Etiqueta de Marca
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        ) {
                                            Text(
                                                text = item.dto.marca.uppercase(),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        // Nombre / Tono
                                        Text(
                                            text = item.dto.tono,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Badge de Estado del Stock
                                        val (badgeBg, badgeText, badgeBorder, badgeLabel) = when {
                                            item.dto.stockActual == 0 -> Quadruple(
                                                if (isDark) CriticalRedBgDark else CriticalRedBg,
                                                if (isDark) CriticalRedTextDark else CriticalRedText,
                                                if (isDark) CriticalRedBorderDark else CriticalRedBorder,
                                                "Sin stock (0) • Mín: ${item.dto.stockMinimo}"
                                            )
                                            item.dto.stockActual <= item.dto.stockMinimo -> Quadruple(
                                                if (isDark) WarningAmberBgDark else WarningAmberBg,
                                                if (isDark) WarningAmberTextDark else WarningAmberText,
                                                if (isDark) WarningAmberBorderDark else WarningAmberBorder,
                                                "Stock bajo: ${item.dto.stockActual} • Mín: ${item.dto.stockMinimo}"
                                            )
                                            else -> Quadruple(
                                                MaterialTheme.colorScheme.surfaceVariant,
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                                MaterialTheme.colorScheme.outline,
                                                "Stock: ${item.dto.stockActual} • Mín: ${item.dto.stockMinimo}"
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = badgeBg,
                                            border = BorderStroke(1.dp, badgeBorder)
                                        ) {
                                            Text(
                                                text = badgeLabel,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = badgeText
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Contadores numéricos de recepción [-] [0] [+]
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { viewModel.modificarCantidad(item.dto.codigoBarras, -1) },
                                            enabled = item.cantidadARecibir > 0,
                                            modifier = Modifier.size(38.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(0.dp),
                                            border = BorderStroke(
                                                1.dp,
                                                if (item.cantidadARecibir > 0) BrandOrange else MaterialTheme.colorScheme.outline
                                            ),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (item.cantidadARecibir > 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = if (item.cantidadARecibir > 0) BrandOrange else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        ) {
                                            Text(
                                                text = "-",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .widthIn(min = 36.dp)
                                                .padding(horizontal = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${item.cantidadARecibir}",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (estaSeleccionado) BrandOrange else MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Button(
                                            onClick = { viewModel.modificarCantidad(item.dto.codigoBarras, 1) },
                                            modifier = Modifier.size(38.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(0.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = BrandOrange,
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Text(
                                                text = "+",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
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
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
