package com.example.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(exception: Exception, operationType: OperationType, path: String?): String {
    return try {
        val auth = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
        val currentUser = auth?.currentUser

        val providerInfoArray = JSONArray()
        currentUser?.providerData?.forEach { provider ->
            val obj = JSONObject()
            obj.put("providerId", provider.providerId ?: JSONObject.NULL)
            obj.put("email", provider.email ?: JSONObject.NULL)
            providerInfoArray.put(obj)
        }

        val authInfoJson = JSONObject().apply {
            put("userId", currentUser?.uid ?: JSONObject.NULL)
            put("email", currentUser?.email ?: JSONObject.NULL)
            put("emailVerified", currentUser?.isEmailVerified ?: JSONObject.NULL)
            put("tenantId", currentUser?.tenantId ?: JSONObject.NULL)
            put("providerInfo", providerInfoArray)
        }

        val errorInfoJson = JSONObject().apply {
            put("error", exception.message ?: exception.toString())
            put("operationType", operationType.value)
            put("path", path ?: JSONObject.NULL)
            put("authInfo", authInfoJson)
        }

        val jsonString = errorInfoJson.toString()
        Log.e("FirestoreError", "Firestore Error: $jsonString")
        jsonString
    } catch (t: Throwable) {
        val fallback = """{"error":"${exception.message}","operationType":"${operationType.value}","path":"$path"}"""
        Log.e("FirestoreError", "Firestore Error: $fallback")
        fallback
    }
}
