package com.skillet.tintes.feature.scanner.domain.usecase

import com.skillet.tintes.feature.scanner.domain.repository.ScannerRepository

class DeshacerVentaUseCase(
    private val repository: ScannerRepository
) {
    suspend operator fun invoke(sucursalId: String, codigoBarras: String): Result<Unit> {
        return repository.sumarStockVenta(sucursalId, codigoBarras)
    }
}
