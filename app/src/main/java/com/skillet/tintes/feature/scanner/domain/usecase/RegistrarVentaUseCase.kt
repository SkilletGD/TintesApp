package com.skillet.tintes.feature.scanner.domain.usecase

import com.skillet.tintes.core.util.ErrorHandler
import com.skillet.tintes.feature.scanner.domain.repository.ScannerRepository

sealed interface ResultadoVenta {
    data class Exito(val marca: String, val tono: String, val stockRestante: Int) : ResultadoVenta
    data class SinStock(val marca: String, val tono: String) : ResultadoVenta
    data object ProductoNoExiste : ResultadoVenta
    data object SucursalSinRegistro : ResultadoVenta
    data class Error(val mensaje: String) : ResultadoVenta
}

class RegistrarVentaUseCase(
    private val repository: ScannerRepository
) {
    suspend operator fun invoke(sucursalId: String, codigoBarras: String): ResultadoVenta {
        val detalle = repository.obtenerDetalleProducto(codigoBarras).getOrNull()
        val marca = detalle?.first ?: "Desconocido"
        val tono = detalle?.second ?: codigoBarras

        val res = repository.restarStockVenta(sucursalId, codigoBarras)
        if (res.isFailure) {
            return ResultadoVenta.Error(ErrorHandler.parseUserFriendlyMessage(res.exceptionOrNull()))
        }

        return when (val code = res.getOrDefault(-99)) {
            -1 -> ResultadoVenta.ProductoNoExiste
            -2 -> ResultadoVenta.SucursalSinRegistro
            -3 -> ResultadoVenta.SinStock(marca, tono)
            in 0..Int.MAX_VALUE -> ResultadoVenta.Exito(marca, tono, stockRestante = code)
            else -> ResultadoVenta.Error("Error al procesar la venta en el servidor")
        }
    }
}