package com.skillet.tintes.feature.scanner.data.repository

import com.skillet.tintes.feature.scanner.domain.repository.ScannerRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class ProductoDto(val marca: String, val tono: String)

class ScannerRepositoryImpl(
    private val client: SupabaseClient
) : ScannerRepository {

    override suspend fun restarStockVenta(sucursalId: String, codigoBarras: String): Result<Int> = runCatching {
        val params = buildJsonObject {
            put("p_sucursal_id", sucursalId.trim())
            put("p_codigo_barras", codigoBarras.trim())
            put("p_cantidad", 1)
        }
        val response = client.postgrest.rpc("restar_stock", params)
        response.decodeAs<Int>()
    }

    override suspend fun sumarStockVenta(sucursalId: String, codigoBarras: String): Result<Unit> = runCatching {
        val params = buildJsonObject {
            put("p_sucursal_id", sucursalId.trim())
            put("p_codigo_barras", codigoBarras.trim())
            put("p_cantidad", 1)
        }
        client.postgrest.rpc("sumar_stock", params)
    }

    override suspend fun obtenerDetalleProducto(codigoBarras: String): Result<Pair<String, String>?> = runCatching {
        val list = client.postgrest["productos"]
            .select {
                filter { eq("codigo_barras", codigoBarras) }
            }.decodeList<ProductoDto>()

        list.firstOrNull()?.let { Pair(it.marca, it.tono) }
    }
}