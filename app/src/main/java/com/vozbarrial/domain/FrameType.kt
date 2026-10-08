package com.vozbarrial.domain

enum class FrameType(val frameName: String, val rarity: String, val cost: Int) {
    CLASICO_CIVICO("Clásico Cívico", "Básico", 0),
    GUARDIAN_DORADO("Guardián Dorado", "Legendario", 500),
    ECO_BARRIO_VERDE("Eco Barrio Verde", "Raro", 300),
    ESCUDO_CIUDADANO("Escudo Ciudadano", "Común", 150);

    companion object {
        fun fromName(name: String): FrameType {
            return entries.find { it.frameName.equals(name, ignoreCase = true) } ?: CLASICO_CIVICO
        }
    }
}
