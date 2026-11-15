package com.vozbarrial.domain.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.vozbarrial.R

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

@Composable
fun getLocalizedLevelName(nivel: Nivel): String {
    return when (nivel) {
        Nivel.NOVATO -> stringResource(R.string.level_novato)
        Nivel.COLABORADOR -> stringResource(R.string.level_colaborador)
        Nivel.GUARDIAN -> stringResource(R.string.level_guardian)
        Nivel.HEROE -> stringResource(R.string.level_heroe)
    }
}

@Composable
fun getLocalizedLevelName(nivelValue: String?): String {
    return getLocalizedLevelName(Nivel.fromValue(nivelValue))
}
