package com.skillet.tintes.feature.resurtido.data.repository
import com.skillet.tintes.feature.resurtido.data.dto.ReporteItemDto
import com.skillet.tintes.feature.resurtido.domain.repository.ResurtidoRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ResurtidoRepositoryImpl(
    private val client: SupabaseClient
) : ResurtidoRepository {

    override suspend fun obtenerFaltantesPorSucursal(sucursalId: String): Result<List<ReporteItemDto>> = runCatching {
        client.postgrest["vw_reporte_resurtido"]
            .select {
                filter { eq("sucursal_id", sucursalId) }
            }.decodeList<ReporteItemDto>()
    }

    override suspend fun sumarStockMercancia(
        sucursalId: String,
        codigoBarras: String,
        cantidad: Int
    ): Result<Unit> = runCatching {
        val params = buildJsonObject {
            put("p_sucursal_id", sucursalId)
            put("p_codigo_barras", codigoBarras)
            put("p_cantidad", cantidad)
        }
        client.postgrest.rpc("sumar_stock", params)
    }
}