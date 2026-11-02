package com.skillet.tintes.feature.scanner.domain.repository

interface ScannerRepository {
    suspend fun restarStockVenta(sucursalId: String, codigoBarras: String): Result<Int>
    suspend fun sumarStockVenta(sucursalId: String, codigoBarras: String): Result<Unit>
    suspend fun obtenerDetalleProducto(codigoBarras: String): Result<Pair<String, String>?> // Marca, Tono
}