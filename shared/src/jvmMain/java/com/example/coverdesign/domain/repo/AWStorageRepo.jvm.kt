package com.example.coverdesign.domain.repo

actual interface AWStorageRepo {
    actual suspend fun putObjectPresigned(content: String): com.example.coverdesign.domain.util.Resource<String>
}