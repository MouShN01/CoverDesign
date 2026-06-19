package com.example.coverdesign.data.repoimpl

import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.model.ModelInstance

class AndroidModelRepository(
    private val modelLoader: ModelLoader
) {
    suspend fun loadModel(
        url: String
    ): ModelInstance? {
        return modelLoader.loadModelInstance(url)
    }
}