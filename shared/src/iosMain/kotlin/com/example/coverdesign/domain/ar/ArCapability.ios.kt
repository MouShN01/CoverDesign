package com.example.coverdesign.domain.ar

import platform.ARKit.ARWorldTrackingConfiguration

actual object ArCapability {
    actual fun isSupported(): Boolean =
        ARWorldTrackingConfiguration.isSupported()
}