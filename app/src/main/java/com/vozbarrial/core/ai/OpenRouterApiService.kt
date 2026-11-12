package com.vozbarrial.core.ai

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.vozbarrial.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object OpenRouterApiService {
    private const val TAG = "OpenRouterApiService"
    private const val URL = "https://openrouter.ai/api/v1/chat/completions"
    
    private var apiKey: String = BuildConfig.OPENROUTER_API_KEY

    fun setApiKey(key: String) {
        if (key.isNotBlank()) {
            apiKey = key
        }
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()
    private val mainHandler = Handler(Looper.getMainLooper())

    private fun runOnMain(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post(action)
        }
    }

    fun translateText(text: String, targetLanguage: String, onResult: (Result<String>) -> Unit) {
        if (text.isBlank()) {
            runOnMain { onResult(Result.success(text)) }
            return
        }

        try {
            val messagesArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "You are a professional direct translator. Translate the provided text into $targetLanguage accurately. Output ONLY the exact translated text with zero conversational filler, zero greetings, and zero commentary.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", text)
                })
            }

            val requestBodyJson = JSONObject().apply {
                put("model", "meta-llama/llama-3.1-8b-instruct")
                put("messages", messagesArray)
                put("temperature", 0.1)
            }

            val body = requestBodyJson.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(URL)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("HTTP-Referer", "https://vozbarrial.com")
                .addHeader("X-Title", "VozBarrial App")
                .post(body)
                .build()

            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    Log.e(TAG, "OpenRouter network failure", e)
                    runOnMain { onResult(Result.failure(e)) }
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    try {
                        val responseString = response.body?.string() ?: ""
                        if (!response.isSuccessful) {
                            runOnMain { onResult(Result.failure(IOException("OpenRouter API error: ${response.code} - $responseString"))) }
                            return
                        }

                        val jsonResp = JSONObject(responseString)
                        val choices = jsonResp.getJSONArray("choices")
                        if (choices.length() > 0) {
                            val firstChoice = choices.getJSONObject(0)
                            val message = firstChoice.getJSONObject("message")
                            val content = message.getString("content").trim()
                            runOnMain { onResult(Result.success(content)) }
                        } else {
                            runOnMain { onResult(Result.failure(IOException("No choices returned from OpenRouter"))) }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse OpenRouter response", e)
                        runOnMain { onResult(Result.failure(e)) }
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Exception calling OpenRouter", e)
            runOnMain { onResult(Result.failure(e)) }
        }
    }

    fun improveReport(title: String, description: String, onResult: (Result<Pair<String, String>>) -> Unit) {
        val prompt = "Analyze this neighborhood report.\nTitle: $title\nDescription: $description\n\nTask: Return a JSON object with two fields:\n1. \"improvedDescription\": a polished, clear, professional description of the issue.\n2. \"suggestedCategory\": one of [Infraestructura, Alumbrado, Seguridad, Mascotas, Comunidad, Emergencias].\n\nReturn ONLY valid JSON."

        callOpenRouter(prompt) { result ->
            result.fold(
                onSuccess = { responseText ->
                    try {
                        val cleanJson = responseText.replace("```json", "").replace("```", "").trim()
                        val json = JSONObject(cleanJson)
                        val improved = json.optString("improvedDescription", description)
                        val category = json.optString("suggestedCategory", "Infraestructura")
                        runOnMain { onResult(Result.success(Pair(improved, category))) }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse AI response JSON: $responseText", e)
                        runOnMain { onResult(Result.failure(e)) }
                    }
                },
                onFailure = { error ->
                    runOnMain { onResult(Result.failure(error)) }
                }
            )
        }
    }

    private fun callOpenRouter(prompt: String, onResult: (Result<String>) -> Unit) {
        try {
            val messagesArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "You are an intelligent assistant for VozBarrial, a community reporting app.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            }

            val requestBodyJson = JSONObject().apply {
                put("model", "meta-llama/llama-3.1-8b-instruct")
                put("messages", messagesArray)
            }

            val body = requestBodyJson.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(URL)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("HTTP-Referer", "https://vozbarrial.com")
                .addHeader("X-Title", "VozBarrial App")
                .post(body)
                .build()

            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    Log.e(TAG, "OpenRouter network failure", e)
                    onResult(Result.failure(e))
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    try {
                        val responseString = response.body?.string() ?: ""
                        if (!response.isSuccessful) {
                            onResult(Result.failure(IOException("OpenRouter API error: ${response.code} - $responseString")))
                            return
                        }

                        val jsonResp = JSONObject(responseString)
                        val choices = jsonResp.getJSONArray("choices")
                        if (choices.length() > 0) {
                            val firstChoice = choices.getJSONObject(0)
                            val message = firstChoice.getJSONObject("message")
                            val content = message.getString("content").trim()
                            onResult(Result.success(content))
                        } else {
                            onResult(Result.failure(IOException("No choices returned from OpenRouter")))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse OpenRouter response", e)
                        onResult(Result.failure(e))
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Exception calling OpenRouter", e)
            onResult(Result.failure(e))
        }
    }
}
