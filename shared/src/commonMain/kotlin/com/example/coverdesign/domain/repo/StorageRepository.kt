package com.example.coverdesign.domain.repo

import com.example.coverdesign.domain.util.Resource
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────────────────────────────────────
// StorageRepository — abstract over Firebase Storage (and any future provider).
// Lives in commonMain/domain so the ViewModel never imports Firebase directly.
// ─────────────────────────────────────────────────────────────────────────────

interface StorageRepository {

    /**
     * Upload a local image (content:// or file:// URI string) to Firebase Storage.
     * Returns the permanent Firebase download URL.
     * That URL is safe to store in Firestore and send to the 3D generation server.
     */
    fun uploadTextureImage(
        localUri : String,
        configId : String,
        userId   : String
    ): Flow<UploadState>
    /**
     * Upload the user's reference photo (used for AR fitting, not sent to 3D server).
     * Stored separately under /photos/{userId}/{configId}.jpg
     */
    fun uploadReferencePhoto(
        localUri  : String,
        configId  : String,
        userId    : String
    ): Flow<UploadState>

    /**
     * Delete previously uploaded texture image when config is discarded.
     */
    suspend fun deleteTextureImage(configId: String, userId: String): Resource<Unit>
//    suspend fun uploadTextureImage(
//        localUri: String,
//        configId: String,
//        userId: String,
//        onProgress: ((Int) -> Unit)? = null
//    ): Resource<String>
}
sealed class UploadState {
    data class Progress(val percent: Int)  : UploadState()
    data class Success(val downloadUrl: String) : UploadState()
    data class Error(val message: String)  : UploadState()
}