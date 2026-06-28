package com.example.coverdesign.domain.repo

import com.example.coverdesign.domain.util.Resource


expect interface AWStorageRepo {
    suspend fun putObjectPresigned(content: String): Resource<String>
}
