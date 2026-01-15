package com.example.hackathonfitmakers.utils

import android.util.Log
import com.example.hackathonfitmakers.model.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Locale

object FirestoreHelper {
    private val db = FirebaseFirestore.getInstance()
    private const val COLLECTION_USERS = "usuarios"
    private const val COLLECTION_COMMUNITY = "comunidad_chat"
    private const val COLLECTION_VIDEOS = "videos"
    private const val COLLECTION_RELATIONS = "usercmty"
    private const val TAG = "FirestoreHelper"

    fun addUser(user: User, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        // "voz" is already a List<Float> in the User object.
        // Firestore handles lists naturally.
        db.collection(COLLECTION_USERS).document(user.dni)
            .set(user, SetOptions.merge())
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding user", e)
                onFailure(e.message ?: "Unknown error")
            }
    }

    fun getUser(dni: String, onSuccess: (User) -> Unit, onFailure: (String) -> Unit) {
        db.collection(COLLECTION_USERS).document(dni)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    try {
                         val user = document.toObject(User::class.java)
                         if (user != null) {
                             onSuccess(user)
                         } else {
                             onFailure("Failed to parse user data")
                         }
                    } catch (e: Exception) {
                        onFailure("Error parsing data: ${e.message}")
                    }
                } else {
                    onFailure("User not found")
                }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Unknown error")
            }
    }

    // Returns a list of maps/objects for events. 
    fun getCommunityEvents(onSuccess: (List<Map<String, Any>>) -> Unit, onFailure: (String) -> Unit) {
        db.collection(COLLECTION_COMMUNITY)
            .get()
            .addOnSuccessListener { result ->
                val events = mutableListOf<Map<String, Any>>()
                for (document in result) {
                    val data = document.data.toMutableMap()
                    data["id"] = document.id
                    
                    // Handle Date: could be String or Timestamp
                    if (data["fecha"] is Timestamp) {
                        val timestamp = data["fecha"] as Timestamp
                        val sdf = SimpleDateFormat("dd MMM | HH:mm", Locale.getDefault())
                        data["fecha"] = sdf.format(timestamp.toDate())
                    }
                    
                    events.add(data)
                }
                onSuccess(events)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Unknown error")
            }
    }

    fun getExercisesForDay(dayCode: String, onSuccess: (List<Map<String, Any>>) -> Unit, onFailure: (String) -> Unit) {
        // "dia_semana" is a list: ["Lunes", "Martes"]
        // We want docs where "dia_semana" array-contains dayCode (e.g. "Lunes")
        db.collection(COLLECTION_VIDEOS)
            .whereArrayContains("dia_semana", dayCode)
            .get()
            .addOnSuccessListener { result ->
                val exercises = mutableListOf<Map<String, Any>>()
                for (document in result) {
                    val data = document.data.toMutableMap()
                    data["id"] = document.id 
                    exercises.add(data)
                }
                // Limit to 3 as requested
                onSuccess(exercises.take(3))
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Unknown error")
            }
    }
    
    fun joinEvent(dni: String, eventId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val relation = hashMapOf(
            "id_usuario" to dni,
            "id_comunidad" to eventId
        )
        db.collection(COLLECTION_RELATIONS)
            .add(relation)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Error joining") }
    }

    fun leaveEvent(dni: String, eventId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
         db.collection(COLLECTION_RELATIONS)
            .whereEqualTo("id_usuario", dni)
            .whereEqualTo("id_comunidad", eventId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection(COLLECTION_RELATIONS).document(document.id).delete()
                }
                onSuccess()
            }
            .addOnFailureListener { e -> onFailure(e.message ?: "Error leaving") }
    }

    fun getUserCommunityRelations(dni: String, onSuccess: (List<String>) -> Unit, onFailure: (String) -> Unit) {
        db.collection(COLLECTION_RELATIONS)
            .whereEqualTo("id_usuario", dni)
            .get()
            .addOnSuccessListener { documents ->
                val joinedEventIds = documents.mapNotNull { it.getString("id_comunidad") }
                onSuccess(joinedEventIds)
            }
            .addOnFailureListener { e -> onFailure(e.message ?: "Error fetching relations") }
    }

    // Fetches the names of all participants for a specific event
    fun getEventParticipants(eventId: String, onSuccess: (List<String>) -> Unit, onFailure: (String) -> Unit) {
        db.collection(COLLECTION_RELATIONS)
            .whereEqualTo("id_comunidad", eventId)
            .get()
            .addOnSuccessListener { relations ->
                if (relations.isEmpty) {
                    onSuccess(emptyList())
                    return@addOnSuccessListener
                }

                val userDnis = relations.mapNotNull { it.getString("id_usuario") }
                val names = mutableListOf<String>()
                var pendingLookups = userDnis.size

                if (pendingLookups == 0) {
                     onSuccess(emptyList())
                     return@addOnSuccessListener
                }

                for (dni in userDnis) {
                    db.collection(COLLECTION_USERS).document(dni).get()
                        .addOnSuccessListener { doc ->
                            val name = doc.getString("nombre") ?: "Usuario desconocido"
                            names.add(name)
                            pendingLookups--
                            if (pendingLookups == 0) {
                                onSuccess(names)
                            }
                        }
                        .addOnFailureListener {
                            pendingLookups--
                             if (pendingLookups == 0) {
                                onSuccess(names)
                            }
                        }
                }
            }
            .addOnFailureListener { e -> onFailure(e.message ?: "Error getting participants") }
    }

    // Helper to seed data if empty
    fun seedExercises(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val exercises = listOf(
            // Lunes
            hashMapOf("titulo" to "Sentadillas Básicas", "descripcion" to "3 series de 12 reps.", "dia_semana" to listOf("Lunes")),
            hashMapOf("titulo" to "Flexiones", "descripcion" to "3 series de 10 reps.", "dia_semana" to listOf("Lunes")),
            hashMapOf("titulo" to "Plancha", "descripcion" to "30 segundos.", "dia_semana" to listOf("Lunes")),

            // Martes
            hashMapOf("titulo" to "Zancadas", "descripcion" to "3 series de 10 por pierna.", "dia_semana" to listOf("Martes")),
            hashMapOf("titulo" to "Elevación Lateral", "descripcion" to "3 series de 12 reps.", "dia_semana" to listOf("Martes")),
            hashMapOf("titulo" to "Press Militar", "descripcion" to "3 series de 10 reps.", "dia_semana" to listOf("Martes")),

            // Miercoles
            hashMapOf("titulo" to "Burpees", "descripcion" to "3 series de 8 reps.", "dia_semana" to listOf("Miercoles")),
            hashMapOf("titulo" to "Mountain Climbers", "descripcion" to "30 segundos.", "dia_semana" to listOf("Miercoles")),
            hashMapOf("titulo" to "Saltos de Tijera", "descripcion" to "1 minuto.", "dia_semana" to listOf("Miercoles")),

            // Jueves
            hashMapOf("titulo" to "Remo con Mancuerna", "descripcion" to "3 series de 12 reps.", "dia_semana" to listOf("Jueves")),
            hashMapOf("titulo" to "Curl de Bíceps", "descripcion" to "3 series de 12 reps.", "dia_semana" to listOf("Jueves")),
            hashMapOf("titulo" to "Fondos de Tríceps", "descripcion" to "3 series de 10 reps.", "dia_semana" to listOf("Jueves")),

            // Viernes
            hashMapOf("titulo" to "Sentadilla Búlgara", "descripcion" to "3 series de 10 por pierna.", "dia_semana" to listOf("Viernes")),
            hashMapOf("titulo" to "Peso Muerto", "descripcion" to "3 series de 10 reps.", "dia_semana" to listOf("Viernes")),
            hashMapOf("titulo" to "Hip Thrust", "descripcion" to "3 series de 12 reps.", "dia_semana" to listOf("Viernes"))
        )

        val batch = db.batch()
        for (ex in exercises) {
            val docRef = db.collection(COLLECTION_VIDEOS).document() // Auto-ID
            batch.set(docRef, ex)
        }

        batch.commit()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Error seeding data") }
    }
}
