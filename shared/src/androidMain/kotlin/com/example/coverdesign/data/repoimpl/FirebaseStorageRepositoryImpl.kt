package com.example.coverdesign.data.repoimpl

import android.net.Uri
import androidx.core.net.toUri
import com.example.coverdesign.domain.repo.StorageRepository
import com.example.coverdesign.domain.repo.UploadState
import com.example.coverdesign.domain.util.Resource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirebaseStorageRepositoryImpl( //TODO change pass for textures
    private val storage: FirebaseStorage
) : StorageRepository {

    override fun uploadTextureImage(
        localUri: String,
        configId: String,
        userId: String
    ): Flow<UploadState> = callbackFlow {

        val ref = storage.reference.child("textures/$userId/$configId.jpg")
        val uploadTask = ref.putFile(localUri.toUri())

        uploadTask.addOnProgressListener { snapshot ->
            val percent = if (snapshot.totalByteCount > 0)
                (100.0 * snapshot.bytesTransferred / snapshot.totalByteCount).toInt()
            else 0
            trySend(UploadState.Progress(percent))
        }

        uploadTask.addOnSuccessListener {
            ref.downloadUrl
                .addOnSuccessListener { uri ->
                    trySend(UploadState.Success(uri.toString()))
                    close()
                }
                .addOnFailureListener { e ->
                    close(e)
                }
        }

        uploadTask.addOnFailureListener { e ->
            close(e)
        }

        awaitClose { uploadTask.cancel() }
    }


//    suspend fun uploadReferencePhoto(
//        localUri: String,
//        configId: String,
//        userId: String,
//        onProgress: ((Int) -> Unit)? = null
//    ): Resource<String> = upload(
//        uri = Uri.parse(localUri),
//        path = "photos/$userId/$configId.jpg",
//        onProgress = onProgress
//    )


    override fun uploadReferencePhoto(
        localUri: String,
        configId: String,
        userId: String
    ): Flow<UploadState> = callbackFlow {

        val ref = storage.reference.child("photos/$userId/$configId.jpg")
        val uploadTask = ref.putFile(localUri.toUri())

        uploadTask.addOnProgressListener { snapshot ->
            val percent = if (snapshot.totalByteCount > 0)
                (100.0 * snapshot.bytesTransferred / snapshot.totalByteCount).toInt()
            else 0
            trySend(UploadState.Progress(percent))
        }

        uploadTask.addOnSuccessListener {
            ref.downloadUrl
                .addOnSuccessListener { uri ->
                    trySend(UploadState.Success(uri.toString()))
                    close()
                }
                .addOnFailureListener { e ->
                    close(e)
                }
        }

        uploadTask.addOnFailureListener { e ->
            close(e)
        }

        awaitClose { uploadTask.cancel() }
    }

    override suspend fun deleteTextureImage(
        configId: String,
        userId: String
    ): Resource<Unit> = try {
        Tasks.await(
            storage.reference
                .child("textures/$userId/$configId.jpg")
                .delete()
        )
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error("Delete failed: ${e.message}", e)
    }

    private suspend fun upload(
        uri: Uri,
        path: String,
        onProgress: ((Int) -> Unit)? = null
    ): Resource<String> = suspendCancellableCoroutine { continuation ->

        val ref = storage.reference.child(path)
        val uploadTask = ref.putFile(uri)   // putFile handles ContentResolver internally

        // ── progress monitoring ───────────────────────────────────────────────
        uploadTask.addOnProgressListener { snapshot ->
            val total = snapshot.totalByteCount
            val transferred = snapshot.bytesTransferred
            if (total > 0) {
                val percent = (100.0 * transferred / total).toInt()
                onProgress?.invoke(percent)
            }
        }

        // ── success — now fetch the download URL ──────────────────────────────
        uploadTask.addOnSuccessListener {
            ref.downloadUrl
                .addOnSuccessListener { downloadUri ->
                    continuation.resume(Resource.Success(downloadUri.toString()))
                }
                .addOnFailureListener { e ->
                    continuation.resume(Resource.Error("getDownloadUrl failed: ${e.message}", e))
                }
        }

        // ── failure ───────────────────────────────────────────────────────────
        uploadTask.addOnFailureListener { e ->
            continuation.resume(Resource.Error("Upload failed: ${e.message}", e))
        }

        // ── cancellation — cancel the Firebase task too ───────────────────────
        continuation.invokeOnCancellation {
            uploadTask.cancel()
        }
    }
}