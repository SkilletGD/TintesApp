package com.skillet.tintes.core.config

/**
 * Enum que define las tiendas de la aplicación con su ID único y su nombre oficial.
 */
enum class Tienda(val id: String, val nombre: String) {
    LLUVIA(
        id = "2bf48705-b3c5-41f4-98e0-eb9c8aad515e",
        nombre = "Lluvia"
    ),
    SANTA_ROSA(
        id = "7024d164-94e2-48da-88c5-d1511aed3e91",
        nombre = "Santa Rosa"
    ),
    CINCO_DE_MAYO(
        id = "db32ce95-a04a-426b-ae1c-70ab31965149",
        nombre = "5 de Mayo"
    )
}

/**
 * Configuración global de la aplicación.
 * Para generar el APK de cada tienda, cambia únicamente la variable [TIENDA_ACTIVA].
 */
object AppConfig {

    // 🔴 MODIFICA ÚNICAMENTE ESTA VARIABLE PARA CAMBIAR LA TIENDA DEL APK 🔴
    // Opciones disponibles: Tienda.LLUVIA, Tienda.SANTA_ROSA, Tienda.CINCO_DE_MAYO
    val TIENDA_ACTIVA = Tienda.LLUVIA

    val sucursalId: String
        get() = TIENDA_ACTIVA.id

    val sucursalNombre: String
        get() = TIENDA_ACTIVA.nombre
}
