package com.skillet.tintes.core.util

import com.skillet.tintes.core.config.Tienda

object Sucursales {
    fun obtenerNombre(sucursalId: String): String {
        return Tienda.entries.find { it.id.equals(sucursalId.trim(), ignoreCase = true) }?.nombre
            ?: "Sucursal"
    }
}
