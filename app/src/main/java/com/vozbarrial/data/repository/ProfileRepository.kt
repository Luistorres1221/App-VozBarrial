package com.vozbarrial.data.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.vozbarrial.domain.model.Usurious
import com.vozbarrial.domain.repository.ProfileGateway
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.URL

class ProfileRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : ProfileGateway {
    override fun hasActiveSession(): Boolean = auth.currentUser != null

    override fun loadProfile(onResult: (Result<Usurious?>) -> Unit) {
        val user = auth.currentUser ?: return onResult(Result.success(null))
        firestore.collection(USERS).document(user.uid).get()
            .addOnSuccessListener { snapshot ->
                onResult(Result.success(snapshot.toObject(Usurious::class.java) ?: Usurious(
                    uid = user.uid,
                    email = user.email.orEmpty(),
                    name = user.email.orEmpty().substringBefore('@'),
                )))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    override fun updateProfile(context: Context, name: String, photoUrl: String?, onResult: (Result<Usurious>) -> Unit) {
        val user = auth.currentUser ?: return onResult(Result.failure(IllegalStateException("No hay una sesión activa.")))

        CoroutineScope(Dispatchers.Main).launch {
            val finalPhotoUrl = withContext(Dispatchers.IO) {
                val uri = photoUrl?.let(Uri::parse)
                if (uri != null && uri.scheme in setOf("content", "file")) {
                    uploadToCloudinary(context, uri) ?: photoUrl
                } else {
                    photoUrl
                }
            }

            val cleanName = name.trim()
            val updated = mapOf("name" to cleanName, "photoUrl" to finalPhotoUrl)
            firestore.collection(USERS).document(user.uid).set(updated, SetOptions.merge())
                .addOnSuccessListener {
                    firestore.collection("reportes").whereEqualTo("authorUid", user.uid).get()
                        .addOnSuccessListener { docs ->
                            for (doc in docs) {
                                doc.reference.update("authorName", cleanName)
                            }
                        }
                    val userEmail = user.email.orEmpty()
                    if (userEmail.isNotBlank()) {
                        firestore.collection("reportes").whereEqualTo("authorEmail", userEmail).get()
                            .addOnSuccessListener { docs ->
                                for (doc in docs) {
                                    doc.reference.update("authorName", cleanName)
                                }
                            }
                    }

                    onResult(Result.success(Usurious(uid = user.uid, name = cleanName, email = userEmail, photoUrl = finalPhotoUrl)))
                }
                .addOnFailureListener { onResult(Result.failure(it)) }
        }
    }

    private suspend fun uploadToCloudinary(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val bytes = inputStream.readBytes()
            inputStream.close()

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

            var scale = 1
            while (options.outWidth / scale / 2 >= 800 && options.outHeight / scale / 2 >= 800) {
                scale *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = scale }
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions) ?: return@withContext null

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 60, outputStream)
            val compressedBytes = outputStream.toByteArray()

            val boundary = "Boundary-${System.currentTimeMillis()}"
            val url = URL("https://api.cloudinary.com/v1_1/ibiejjya/image/upload")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                doInput = true
                connectTimeout = 10000
                readTimeout = 10000
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            }

            val connOutputStream = connection.outputStream
            val writer = PrintWriter(OutputStreamWriter(connOutputStream, "UTF-8"), true)

            // Add upload_preset
            writer.append("--$boundary\r\n")
            writer.append("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n")
            writer.append("voz_barrial\r\n")

            // Add file
            writer.append("--$boundary\r\n")
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"profile.jpg\"\r\n")
            writer.append("Content-Type: image/jpeg\r\n\r\n")
            writer.flush()

            connOutputStream.write(compressedBytes)
            connOutputStream.flush()

            writer.append("\r\n--$boundary--\r\n")
            writer.flush()
            writer.close()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                json.optString("secure_url").ifBlank { null }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    override fun deleteAccount(onResult: (Result<Unit>) -> Unit) {
        val user = auth.currentUser ?: return onResult(Result.failure(IllegalStateException("No hay una sesión activa.")))
        user.delete().addOnSuccessListener {
            firestore.collection(USERS).document(user.uid).delete()
                .addOnSuccessListener { onResult(Result.success(Unit)) }
                .addOnFailureListener { onResult(Result.failure(it)) }
        }.addOnFailureListener { onResult(Result.failure(it)) }
    }

    override fun signOut() = auth.signOut()

    companion object { private const val USERS = "usuarios" }
}
