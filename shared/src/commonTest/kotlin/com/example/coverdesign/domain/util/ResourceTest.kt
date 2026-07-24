package com.example.coverdesign.domain.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame

class ResourceTest {

    // ── Success ───────────────────────────────────────────────────────────────

    @Test
    fun success_holdsData() {
        val resource = Resource.Success(42)
        assertEquals(42, resource.data)
    }

    // ── Error ─────────────────────────────────────────────────────────────────

    @Test
    fun error_holdsMessage() {
        val resource = Resource.Error("something went wrong")
        assertEquals("something went wrong", resource.message)
    }

    @Test
    fun error_holdsOptionalCause() {
        val cause = RuntimeException("root cause")
        val resource = Resource.Error("failed", cause)
        assertSame(cause, resource.cause)
    }

    @Test
    fun error_causeIsNullByDefault() {
        val resource = Resource.Error("oops")
        assertNull(resource.cause)
    }

    // ── onSuccess ─────────────────────────────────────────────────────────────

    @Test
    fun onSuccess_invokedForSuccess() {
        var received: Int? = null
        Resource.Success(10).onSuccess { received = it }
        assertEquals(10, received)
    }

    @Test
    fun onSuccess_notInvokedForError() {
        var invoked = false
        Resource.Error("err").onSuccess<Int> { invoked = true }
        assertEquals(false, invoked)
    }

    @Test
    fun onSuccess_notInvokedForLoading() {
        var invoked = false
        Resource.Loading.onSuccess<Int> { invoked = true }
        assertEquals(false, invoked)
    }

    @Test
    fun onSuccess_returnsOriginalResource() {
        val original: Resource<Int> = Resource.Success(99)
        val returned = original.onSuccess {}
        assertSame(original, returned)
    }

    // ── onError ───────────────────────────────────────────────────────────────

    @Test
    fun onError_invokedForError() {
        var receivedMsg = ""
        Resource.Error("bad").onError { msg, _ -> receivedMsg = msg }
        assertEquals("bad", receivedMsg)
    }

    @Test
    fun onError_notInvokedForSuccess() {
        var invoked = false
        Resource.Success("ok").onError { _, _ -> invoked = true }
        assertEquals(false, invoked)
    }

    @Test
    fun onError_notInvokedForLoading() {
        var invoked = false
        Resource.Loading.onError { _, _ -> invoked = true }
        assertEquals(false, invoked)
    }

    @Test
    fun onError_returnsOriginalResource() {
        val original: Resource<String> = Resource.Error("x")
        val returned = original.onError { _, _ -> }
        assertSame(original, returned)
    }

    // ── mapSuccess ────────────────────────────────────────────────────────────

    @Test
    fun mapSuccess_transformsSuccessData() {
        val result = Resource.Success(5).mapSuccess { it * 2 }
        val success = assertIs<Resource.Success<Int>>(result)
        assertEquals(10, success.data)
    }

    @Test
    fun mapSuccess_passesErrorThrough() {
        val original: Resource<Int> = Resource.Error("fail")
        val result = original.mapSuccess { it.toString() }
        val error = assertIs<Resource.Error>(result)
        assertEquals("fail", error.message)
    }

    @Test
    fun mapSuccess_passesLoadingThrough() {
        val result = Resource.Loading.mapSuccess { _: Nothing -> "" }
        assertIs<Resource.Loading>(result)
    }
}