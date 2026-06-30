package com.example.coverdesign.domain.usecase

import com.example.coverdesign.domain.repo.StorageRepository
import com.example.coverdesign.domain.repo.UploadState
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────────────────────────────────────
// UploadReferencePhotoUseCase
//
// Uploads the user's reference photo (used for AR fitting of accessories /
// jewellery). Only relevant when ArMode == OnUserPhoto.
// The download URL is stored in Firestore config but NOT sent to the 3D server
// in ProductConfigDto (userPhotoUri is excluded by the mapper).
// ─────────────────────────────────────────────────────────────────────────────

class UploadReferencePhotoUseCase(
    private val storageRepo: StorageRepository
) {
    operator fun invoke(
        localUri : String,
        configId : String,
        userId   : String
    ): Flow<UploadState> =
        storageRepo.uploadReferencePhoto(
            localUri = localUri,
            configId = configId,
            userId   = userId
        )

    fun needsUpload(previewUrl: String?): Boolean {
        if (previewUrl.isNullOrBlank()) return false
        if (previewUrl.startsWith("http://") ||
            previewUrl.startsWith("https://")
        ) {
            return false
        }

        return previewUrl.startsWith("content://")
                || previewUrl.startsWith("file://")

    }
}