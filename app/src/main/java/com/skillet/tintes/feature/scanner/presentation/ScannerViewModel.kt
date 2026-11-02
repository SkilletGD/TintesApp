package com.skillet.tintes.feature.scanner.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillet.tintes.core.util.ErrorHandler
import com.skillet.tintes.feature.scanner.domain.usecase.DeshacerVentaUseCase
import com.skillet.tintes.feature.scanner.domain.usecase.RegistrarVentaUseCase
import com.skillet.tintes.feature.scanner.domain.usecase.ResultadoVenta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScannerViewModel(
    private val registrarVentaUseCase: RegistrarVentaUseCase,
    private val deshacerVentaUseCase: DeshacerVentaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    fun abrirCamara() {
        _uiState.value = _uiState.value.copy(camaraAbierta = true, dialogo = null)
    }

    fun cerrarCamara() {
        _uiState.value = _uiState.value.copy(camaraAbierta = false)
    }

    fun cerrarDialogo() {
        _uiState.value = _uiState.value.copy(dialogo = null)
    }

    fun limpiarMensajeToast() {
        _uiState.value = _uiState.value.copy(mensajeToast = null)
    }

    fun onBarcodeScanned(sucursalId: String, barcode: String): Boolean {
        val trimmedCode = barcode.trim()
        if (trimmedCode.isEmpty()) return false

        val current = _uiState.value
        // Rechazar re-lecturas si ya se está procesando una venta o si hay un diálogo abierto
        if (current.procesando || current.dialogo != null) {
            return false
        }

        // Aceptar la venta e inmediatamente cerrar la cámara para evitar lecturas repetidas
        _uiState.value = current.copy(camaraAbierta = false, procesando = true)

        viewModelScope.launch {
            val resultado = registrarVentaUseCase(sucursalId, trimmedCode)
            val dialogo = when (resultado) {
                is ResultadoVenta.Exito -> DialogoVenta.Exito(
                    marca = resultado.marca,
                    tono = resultado.tono,
                    codigoBarras = trimmedCode,
                    stockRestante = resultado.stockRestante
                )
                is ResultadoVenta.SinStock -> DialogoVenta.SinStock(resultado.marca, resultado.tono)
                is ResultadoVenta.ProductoNoExiste -> DialogoVenta.Error("El código $trimmedCode no está registrado en el catálogo.")
                is ResultadoVenta.SucursalSinRegistro -> DialogoVenta.Error("Este producto no ha sido asignado a esta sucursal.")
                is ResultadoVenta.Error -> DialogoVenta.Error(resultado.mensaje)
            }

            _uiState.value = _uiState.value.copy(
                procesando = false,
                dialogo = dialogo
            )
        }

        return true
    }

    fun deshacerVenta(sucursalId: String, codigoBarras: String) {
        if (_uiState.value.deshaciendoVenta) return

        _uiState.value = _uiState.value.copy(deshaciendoVenta = true)
        viewModelScope.launch {
            val res = deshacerVentaUseCase(sucursalId, codigoBarras)
            res.onSuccess {
                _uiState.value = _uiState.value.copy(
                    deshaciendoVenta = false,
                    dialogo = null,
                    mensajeToast = "Venta revertida exitosamente. Se devolvió 1 pieza al inventario."
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    deshaciendoVenta = false,
                    dialogo = DialogoVenta.Error("No se pudo deshacer la venta: ${ErrorHandler.parseUserFriendlyMessage(err)}")
                )
            }
        }
    }
}