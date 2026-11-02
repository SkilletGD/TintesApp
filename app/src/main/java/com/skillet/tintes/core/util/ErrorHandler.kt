package com.skillet.tintes.core.util

import java.net.ConnectException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

object ErrorHandler {

    /**
     * Convierte excepciones técnicas y de red en mensajes amigables y entendibles para el usuario final,
     * evitando exponer URLs, headers o detalles internos de la base de datos o API.
     */
    fun parseUserFriendlyMessage(throwable: Throwable?): String {
        if (throwable == null) return "Ocurrió un error inesperado. Por favor intenta de nuevo."

        val message = throwable.message ?: ""
        val causeMsg = throwable.cause?.message ?: ""
        val fullMsg = "$message $causeMsg".lowercase()

        return when {
            // Sin conexión a internet o DNS
            throwable is UnknownHostException || throwable is ConnectException ||
                    fullMsg.contains("unknownhost") || fullMsg.contains("failed to connect") ||
                    fullMsg.contains("connection refused") || fullMsg.contains("network") -> {
                "No hay conexión a internet. Por favor verifica tu red e inténtalo de nuevo."
            }

            // Tiempo de espera agotado
            throwable is TimeoutException || fullMsg.contains("timeout") || fullMsg.contains("timed out") -> {
                "La conexión con el servidor tardó demasiado. Por favor reintenta."
            }

            // Clave API o Autenticación
            fullMsg.contains("api key") || fullMsg.contains("apikey") ||
                    fullMsg.contains("unauthorized") || fullMsg.contains("401") ||
                    fullMsg.contains("forbidden") || fullMsg.contains("403") -> {
                "Error de autorización con el servidor. Contacta al administrador."
            }

            // Recurso o tabla no encontrada
            fullMsg.contains("404") || fullMsg.contains("not found") -> {
                "No se encontró la información solicitada en el servidor."
            }

            // Error del servidor 500
            fullMsg.contains("500") || fullMsg.contains("internal server error") -> {
                "El servidor de la aplicación experimentó un problema temporal. Reintenta en unos momentos."
            }

            // Fallback limpio
            else -> "Ocurrió un problema al procesar la solicitud. Por favor reintenta."
        }
    }
}
