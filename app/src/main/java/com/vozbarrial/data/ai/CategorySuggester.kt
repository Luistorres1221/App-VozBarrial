package com.vozbarrial.data.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.vozbarrial.domain.model.REPORT_CATEGORY_IDS
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

class CategorySuggester {

    private val model: GenerativeModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                temperature = 0f
                responseMimeType = "text/x.enum"
                responseSchema = Schema.enumeration(REPORT_CATEGORY_IDS)
            },
            systemInstruction = content { text(SYSTEM_PROMPT) }
        )
    }

    suspend fun suggest(title: String, description: String): String? {
        return try {
            val response = withTimeout(TIMEOUT_MS) {
                model.generateContent("Título: $title\nDescripción: $description")
            }
            response.text?.trim()?.takeIf { it in REPORT_CATEGORY_IDS }
        } catch (e: TimeoutCancellationException) {
            null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo sugerir la categoría", e)
            null
        }
    }

    private companion object {
        const val TAG = "CategorySuggester"
        const val MODEL_NAME = "gemini-3.1-flash-lite"
        const val TIMEOUT_MS = 8_000L

        val SYSTEM_PROMPT = """
            Clasificas reportes ciudadanos de un barrio en Colombia.
            El texto puede venir en español o en inglés.
            Responde únicamente con una de estas categorías:
            - Seguridad: robos, vandalismo, actividades sospechosas.
            - Emergencias Médicas: accidentes viales, heridos, primeros auxilios.
            - Infraestructura y Vías: baches, daños en calles, andenes o pavimento.
            - Mascotas y Animales: animales perdidos, encontrados, maltratados o en peligro.
            - Comunidad y Entorno: basuras, contaminación, ruido excesivo.
            - Alumbrado y Servicios: postes sin luz, fugas de agua o gas, cortes de servicios.
        """.trimIndent()
    }
}
