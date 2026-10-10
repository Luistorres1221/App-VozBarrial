package com.vozbarrial.core.validation

/** Validaciones de entrada reutilizables por las distintas funciones de la app. */
object InputValidator {
    private val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(value: String): Boolean = emailPattern.matches(value.trim())
}
