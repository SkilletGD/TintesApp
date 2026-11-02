package com.skillet.tintes.feature.scanner.presentation

sealed interface DialogoVenta {
    data class Exito(
        val marca: String,
        val tono: String,
        val codigoBarras: String,
        val stockRestante: Int
    ) : DialogoVenta

    data class SinStock(val marca: String, val tono: String) : DialogoVenta
    data class Error(val mensaje: String) : DialogoVenta
}


data class ScannerUiState(
    val camaraAbierta: Boolean = false,
    val procesando: Boolean = false,
    val deshaciendoVenta: Boolean = false,
    val dialogo: DialogoVenta? = null,
    val mensajeToast: String? = null
)