package com.vozbarrial.domain.model

enum class Nivel(val value: String, val displayName: String) {
    NOVATO("novato", "Novato"),
    COLABORADOR("colaborador", "Colaborador"),
    GUARDIAN("guardian", "Guardián"),
    HEROE("heroe", "Héroe");

    companion object {
        fun fromValue(value: String?): Nivel {
            return entries.find { it.value.equals(value, true) || it.displayName.equals(value, true) } ?: NOVATO
        }
    }
}
