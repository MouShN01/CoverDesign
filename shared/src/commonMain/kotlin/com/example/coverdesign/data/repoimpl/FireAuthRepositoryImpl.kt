package com.example.coverdesign.data.repoimpl

import com.example.coverdesign.domain.model.AppUser
import com.example.coverdesign.domain.model.UserRole
import com.example.coverdesign.domain.repo.FireAuthRepository
import com.example.coverdesign.domain.util.Resource
import com.example.coverdesign.domain.util.currentEpochMillis
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

class FireAuthRepositoryImpl : FireAuthRepository {
    private val auth = Firebase.auth
    override val currentUser: Flow<FirebaseUser?> =
        auth.authStateChanged   // emits on sign-in / sign-out
    // call this after any successful sign-in

    private val firestore = Firebase.firestore

    // Firestore document model — separate from domain so field names are explicit
    @Serializable
    private data class UserDocument(
        val uid: String = "",
        val email: String = "",
        val displayName: String = "",
        val phone: String = "",
        val role: String = UserRole.CUSTOMER.name,
        val createdAt: Long = 0L
    )

    private fun usersCollection() = firestore.collection("users")


    override suspend fun createUserProfile(user: AppUser): Resource<Unit> = try {
        val doc = UserDocument(
            uid = user.uid,
            email = user.email,
            displayName = user.displayName,
            phone = user.phone,
            role = user.role.name,
            createdAt = user.createdAt
        )
        // set() replaces the whole document — safe for a brand-new user
        usersCollection().document(user.uid).set(doc)
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error("Failed to create user profile: ${e.message}", e)
    }

    /**
     * Fetches the user profile from Firestore.
     * Called on app start after Firebase Auth confirms the user is signed in,
     * to load role + contact info into AppUiState.
     *
     * Returns Resource.Error (not null) when the document doesn't exist yet
     * (e.g. Google sign-in before profile completion step) so the caller
     * can redirect to the profile-completion screen.
     */
    override suspend fun getUserProfile(uid: String): Resource<AppUser> = try {
        val snapshot = usersCollection().document(uid).get()

        if (!snapshot.exists) {
            // document not yet created — happens for Google sign-in before
            // the user fills in their name + phone
            return Resource.Error("profile_not_found")
        }

        val doc = snapshot.data<UserDocument>()
        val user = AppUser(
            uid = doc.uid,
            email = doc.email,
            displayName = doc.displayName,
            phone = doc.phone,
            role = runCatching { UserRole.valueOf(doc.role) }
                .getOrDefault(UserRole.CUSTOMER),
            createdAt = doc.createdAt
        )
        Resource.Success(user)
    } catch (e: Exception) {
        Resource.Error("Failed to load user profile: ${e.message}", e)
    }

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    // ── Email / password ──────────────────────────────────────────────────────

    override suspend fun signInWithEmail(
        email: String, password: String
    ): Resource<FirebaseUser> = try {
        val result = auth.signInWithEmailAndPassword(email, password)
        val user = result.user ?: return Resource.Error("Sign-in returned no user")

        val isNewUser = result.additionalUserInfo?.isNewUser == true
        if (isNewUser) {
            createUserProfile(
                AppUser(
                    uid = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: "",
                    role = UserRole.CUSTOMER,   // default role
                    createdAt = currentEpochMillis()
                )
            )
        }

        Resource.Success(user)

    } catch (e: Exception) {
        Resource.Error(e.message ?: "Sign-in failed", e)
    }

    override suspend fun registerWithEmail(
        email: String, password: String
    ): Resource<FirebaseUser> = try {
        val result = auth.createUserWithEmailAndPassword(email, password)
        val user = result.user ?: return Resource.Error("Registration returned no user")
        Resource.Success(user)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Registration failed", e)
    }

    // ── Google Sign-In ────────────────────────────────────────────────────────
    // idToken comes from platform-side Credential Manager (androidMain)
    // or Google SDK (iosMain) via GoogleAuthenticator.getIdToken()

    override suspend fun signInWithGoogle(idToken: String): Resource<FirebaseUser> = try {
        val credential = GoogleAuthProvider.credential(idToken, accessToken = null)
        val result = auth.signInWithCredential(credential)
        val user = result.user ?: return Resource.Error("Google sign-in returned no user")
        val isNewUser = result.additionalUserInfo?.isNewUser == true
        if (isNewUser) {
            createUserProfile(
                AppUser(
                    uid = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: "",
                    role = UserRole.CUSTOMER,   // default role
                    createdAt = currentEpochMillis()
                )
            )
        }

        Resource.Success(user)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Google sign-in failed", e)
    }

    // ── Session ───────────────────────────────────────────────────────────────

    override suspend fun signOut(): Resource<Unit> = try {
        auth.signOut()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Sign-out failed", e)
    }

    override suspend fun sendPasswordReset(email: String): Resource<Unit> = try {
        auth.sendPasswordResetEmail(email)
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Password reset failed", e)
    }

    override fun isAuthenticated(): Flow<Boolean> =
        auth.authStateChanged.map { it != null }
}