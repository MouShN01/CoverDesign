package com.example.coverdesign.domain.repo

import com.example.coverdesign.domain.util.Resource

actual interface AWStorageRepo {
    actual suspend fun putObjectPresigned(content: String): Resource<String>
}