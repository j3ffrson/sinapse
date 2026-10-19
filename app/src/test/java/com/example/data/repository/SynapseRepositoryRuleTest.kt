package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.CampusZone
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SynapseRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createAndObserveStudyTable_authenticatedOwner_succeedsAndEmitsUpdates() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = SynapseRepository(firestore)

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createStudyTable(
                subject = "Cálculo Diferencial e Integral",
                topic = "Regla de la cadena y optimización",
                campusZone = CampusZone.LIBRARY.id,
                spotReference = "Biblioteca Piso 2 - Mesa 5",
                studyVibe = "Resolución de taller",
                hostName = "Camila",
                hostProgram = "Ingeniería de Software",
                maxSeats = 4
            )
        }
        assertTrue(createResult.isSuccess)
        val tableId = createResult.getOrThrow()

        val emittedTables = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeStudyTables(uid).first { list -> list.any { it.id == tableId } }
        }
        assertTrue(emittedTables.any { it.id == tableId && it.subject == "Cálculo Diferencial e Integral" })
    }

    @Test
    fun createAndResolveSosAlert_authenticatedOwner_updatesStateCorrectly() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = SynapseRepository(firestore)

        val alertId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createSosAlert(
                subject = "Estructura de Datos y Algoritmos",
                questionTitle = "Rotación doble en Árbol AVL",
                questionDetail = "No entiendo cuándo aplicar rotación izquierda-derecha.",
                examUrgency = "Parcial en 1 hora",
                campusZone = CampusZone.CUBICLES.id,
                spotReference = "Cubículo 2",
                authorName = "Camila",
                authorProgram = "Ingeniería de Software"
            ).getOrThrow()
        }

        val alerts = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeSosAlerts(uid).first { list -> list.any { it.id == alertId } }
        }
        val targetAlert = alerts.first { it.id == alertId }
        assertFalse(targetAlert.isResolved)

        val resolveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.toggleSosResolved(targetAlert)
        }
        assertTrue(resolveResult.isSuccess)
        assertTrue(resolveResult.getOrThrow())
    }

    @Test
    fun getStudyTableById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = SynapseRepository(firestore)
        val tableId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createStudyTable(
                subject = "Contabilidad",
                topic = "Flujo de caja",
                campusZone = CampusZone.CAFETERIA.id,
                spotReference = "Mesa 2",
                studyVibe = "Práctica",
                hostName = "Alice",
                hostProgram = "Financiera",
                maxSeats = 3
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = SynapseRepository(firestore)
        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getStudyTableById(tableId)
        }
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as? FirebaseFirestoreException
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, exception?.code)
    }

    @Test
    fun observeStudyTables_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = SynapseRepository(firestore)
        val tableId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createStudyTable(
                subject = "Estadística",
                topic = "Probabilidad condicional",
                campusZone = CampusZone.LIBRARY.id,
                spotReference = "Mesa 1",
                studyVibe = "Taller",
                hostName = "Alice",
                hostProgram = "Negocios",
                maxSeats = 4
            ).getOrThrow()
        }

        // Explicitly sign out and wait briefly for InternalAuthProvider token clear to propagate
        auth.signOut()
        delay(250)

        val getResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getStudyTableById(tableId)
        }
        assertTrue(
            "Expected failure for unauthenticated get but got: $getResult",
            getResult.isFailure
        )
        val getEx = getResult.exceptionOrNull() as? FirebaseFirestoreException
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, getEx?.code)

        var caughtException: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeStudyTables(aliceUid).first()
            }
        } catch (t: Throwable) {
            caughtException = t
        }
        assertTrue(
            "Expected FirebaseFirestoreException on observeStudyTables but got: $caughtException",
            caughtException is FirebaseFirestoreException
        )
        assertEquals(
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            (caughtException as FirebaseFirestoreException).code
        )
    }

    private companion object {
        const val ALICE_EMAIL = "alice.fesc@test.com"
        const val BOB_EMAIL = "bob.fesc@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
