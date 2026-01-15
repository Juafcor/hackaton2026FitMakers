package com.example.hackathonfitmakers.utils

import android.util.Log
import com.example.hackathonfitmakers.model.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Clase de ayuda para todas las operaciones con la base de datos Firebase Firestore.
 * Aquí centralizamos la lógica de guardar usuarios, cargar eventos, ejercicios, etc.
 */
object FirestoreHelper {
    // Instancia de la base de datos
    private val db = FirebaseFirestore.getInstance()
    
    // Nombres de las colecciones en Firebase (como "carpetas" donde se guardan los datos)
    private const val COLLECTION_USERS = "usuarios"
    private const val COLLECTION_COMMUNITY = "comunidad_chat"
    private const val COLLECTION_VIDEOS = "videos"
    private const val COLLECTION_RELATIONS = "usercmty" // Tabla intermedia usuarios-eventos
    private const val TAG = "FirestoreHelper"

    /**
     * Guarda un nuevo usuario o actualiza uno existente.
     * La "voz" ya viene como lista de números en el objeto User, Firestore la guarda bien.
     */
    fun addUser(user: User, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        db.collection(COLLECTION_USERS).document(user.dni)
            .set(user, SetOptions.merge()) // Merge para no borrar campos si ya existían otros
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al añadir usuario", e)
                onFailure(e.message ?: "Error desconocido")
            }
    }

    /**
     * Busca y recupera un usuario por su DNI.
     */
    fun getUser(dni: String, onSuccess: (User) -> Unit, onFailure: (String) -> Unit) {
        db.collection(COLLECTION_USERS).document(dni)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    try {
                         // Convertimos el documento de Firebase a nuestro objeto User de Kotlin
                         val user = document.toObject(User::class.java)
                         if (user != null) {
                             onSuccess(user)
                         } else {
                             onFailure("No se pudieron leer los datos del usuario")
                         }
                    } catch (e: Exception) {
                        onFailure("Error procesando datos: ${e.message}")
                    }
                } else {
                    onFailure("Usuario no encontrado")
                }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Error desconocido")
            }
    }

    /**
     * Obtiene la lista de eventos de la comunidad.
     * Convierte las fechas de Firebase a texto legible.
     */
    fun getCommunityEvents(onSuccess: (List<Map<String, Any>>) -> Unit, onFailure: (String) -> Unit) {
        db.collection(COLLECTION_COMMUNITY)
            .get()
            .addOnSuccessListener { result ->
                val events = mutableListOf<Map<String, Any>>()
                for (document in result) {
                    val data = document.data.toMutableMap()
                    data["id"] = document.id
                    
                    // Si la fecha viene como Timestamp (formato base de datos), la formateamos bonito
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
                onFailure(e.message ?: "Error desconocido")
            }
    }

    /**
     * Busca ejercicios para un día concreto (ej. "Lunes").
     * Devuelve máximo 3 para no saturar la pantalla.
     */
    fun getExercisesForDay(dayCode: String, onSuccess: (List<Map<String, Any>>) -> Unit, onFailure: (String) -> Unit) {
        // En la base de datos, "dia_semana" es una lista (puede ser para varios días).
        // Buscamos documentos donde esa lista contenga el día que queremos.
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
                // Limitamos a 3 ejercicios
                onSuccess(exercises.take(3))
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Error desconocido")
            }
    }
    
    /**
     * Apunta a un usuario a un evento (Crea una relación en la tabla intermedia).
     */
    fun joinEvent(dni: String, eventId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val relation = hashMapOf(
            "id_usuario" to dni,
            "id_comunidad" to eventId
        )
        db.collection(COLLECTION_RELATIONS)
            .add(relation)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Error al unirse") }
    }

    /**
     * Desapunta a un usuario de un evento (Borra la relación).
     */
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
            .addOnFailureListener { e -> onFailure(e.message ?: "Error al salir") }
    }

    /**
     * Devuelve una lista con los IDs de los eventos a los que el usuario está apuntado.
     */
    fun getUserCommunityRelations(dni: String, onSuccess: (List<String>) -> Unit, onFailure: (String) -> Unit) {
        db.collection(COLLECTION_RELATIONS)
            .whereEqualTo("id_usuario", dni)
            .get()
            .addOnSuccessListener { documents ->
                val joinedEventIds = documents.mapNotNull { it.getString("id_comunidad") }
                onSuccess(joinedEventIds)
            }
            .addOnFailureListener { e -> onFailure(e.message ?: "Error recuperando relaciones") }
    }

    /**
     * Obtiene los nombres de todos los participantes de un evento específico.
     * Hace múltiples consultas: primero busca las relaciones, luego busca los nombres de esos usuarios.
     */
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
                            // Cuando hayamos buscado todos, devolvemos la lista completa
                            if (pendingLookups == 0) {
                                onSuccess(names)
                            }
                        }
                        .addOnFailureListener {
                            // Si falla uno, seguimos contando para no quedarnos colgados
                            pendingLookups--
                             if (pendingLookups == 0) {
                                onSuccess(names)
                            }
                        }
                }
            }
            .addOnFailureListener { e -> onFailure(e.message ?: "Error obteniendo participantes") }
    }

    /**
     * Función auxiliar para meter datos de prueba si la base de datos está vacía.
     * Crea ejercicios básicos para la semana.
     */
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
            val docRef = db.collection(COLLECTION_VIDEOS).document() // Generamos ID automático
            batch.set(docRef, ex)
        }

        batch.commit()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Error guardando datos de prueba") }
    }
}
