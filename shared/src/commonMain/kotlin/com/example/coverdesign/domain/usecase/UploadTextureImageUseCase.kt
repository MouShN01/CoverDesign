package com.example.coverdesign.domain.usecase

import com.example.coverdesign.domain.repo.StorageRepository
import com.example.coverdesign.domain.repo.UploadState
import kotlinx.coroutines.flow.Flow


class UploadTextureImageUseCase(
    private val storageRepo: StorageRepository
) {
    operator fun invoke(
        localUri: String,
        configId: String,
        userId: String
    ): Flow<UploadState> =
        storageRepo.uploadTextureImage(
            localUri = localUri,
            configId = configId,
            userId = userId
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