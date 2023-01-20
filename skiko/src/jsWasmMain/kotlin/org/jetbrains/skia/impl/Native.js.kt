package org.jetbrains.skia.impl

import org.jetbrains.skia.ModuleImport
import org.jetbrains.skia.ExternalSymbolName
import org.khronos.webgl.ArrayBufferView

actual abstract class Native actual constructor(ptr: NativePointer) {
    actual var _ptr: NativePointer

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (null == other) return false
        if (other !is Native) return false
        return if (_ptr == other._ptr) true else nativeEquals(other)
    }

    override fun hashCode(): Int = _ptr

    internal actual open fun nativeEquals(other: Native?): Boolean {
        return false
    }

    actual companion object {
        actual val NullPointer: NativePointer
            get() = 0
    }

    actual override fun toString(): String {
        return this::class.simpleName + "(_ptr=0x" + _ptr.toString(16) + ")"
    }

    init {
        if (ptr == NullPointer) throw RuntimeException("Can't wrap nullptr")
        _ptr = ptr
    }
}

internal actual fun reachabilityBarrier(obj: Any?) {}

actual typealias NativePointer = Int
actual typealias InteropPointer = Int

private val INTEROP_SCOPE = InteropScope()
private var interopScopeCounter = 0

internal actual inline fun <T> interopScope(block: InteropScope.() -> T): T {
    try {
        interopScopeCounter++
        return INTEROP_SCOPE.block()
    } finally {
        interopScopeCounter--
        if (interopScopeCounter == 0) {
            INTEROP_SCOPE.release()
        }
    }
}

internal actual class InteropScope actual constructor() {
    private val elements = mutableListOf<NativePointer>()
    private var callbacksInitialized = false

    actual fun toInterop(string: String?): InteropPointer {
        return if (string != null) {
            val data = _malloc(string.length * 4)
            stringToUTF8(string, data, string.length * 4)
            elements.add(data)
            data
        } else {
            0
        }
    }

    private fun toInterop(array: ByteArray?, copyArrayToWasm: Boolean): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val data = _malloc(array.size)
            elements.add(data)
            if (copyArrayToWasm) toWasm(data, array)
            data
        } else {
            0
        }
    }

    actual fun toInterop(array: ByteArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = true)

    actual fun toInteropForResult(array: ByteArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = false)

    actual fun InteropPointer.fromInterop(result: ShortArray) {
        fromWasm(this@fromInterop, result)
    }

    private fun toInterop(array: ShortArray?, copyArrayToWasm: Boolean): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val data = _malloc(array.size * 2)
            elements.add(data)
            if (copyArrayToWasm) toWasm(data, array)
            data
        } else {
            0
        }
    }

    actual fun toInterop(array: ShortArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = true)

    actual fun toInteropForResult(array: ShortArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = false)

    actual fun InteropPointer.fromInterop(result: IntArray) {
        fromWasm(this@fromInterop, result)
    }

    private fun toInterop(array: IntArray?, copyArrayToWasm: Boolean): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val data = _malloc(array.size * 4)
            elements.add(data)
            if (copyArrayToWasm) toWasm(data, array)
            data
        } else {
            0
        }
    }

    actual fun toInterop(array: IntArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = true)

    actual fun toInteropForResult(array: IntArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = false)

    actual fun InteropPointer.fromInterop(result: LongArray) {
        TODO("implement wasm fromInterop(LongArray)")
    }

    actual fun toInterop(array: LongArray?): InteropPointer {
        TODO("implement wasm toInterop(LongArray)")
    }

    actual fun InteropPointer.fromInterop(result: FloatArray) {
        fromWasm(this@fromInterop, result)
    }

    private fun toInterop(array: FloatArray?, copyArrayToWasm: Boolean): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val data = _malloc(array.size * 4)
            elements.add(data)
            if (copyArrayToWasm) toWasm(data, array)
            data
        } else {
            0
        }
    }

    actual fun toInterop(array: FloatArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = true)

    actual fun toInteropForResult(array: FloatArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = false)

    actual fun InteropPointer.fromInterop(result: DoubleArray) {
        fromWasm(this@fromInterop, result)
    }

    private fun toInterop(array: DoubleArray?, copyArrayToWasm: Boolean): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val data = _malloc(array.size * 8)
            elements.add(data)
            if (copyArrayToWasm) toWasm(data, array)
            data
        } else {
            0
        }
    }

    actual fun toInterop(array: DoubleArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = true)

    actual fun toInteropForResult(array: DoubleArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = false)

    actual fun InteropPointer.fromInterop(result: ByteArray) {
        fromWasm(this@fromInterop, result)
    }

    private fun toInterop(array: NativePointerArray?, copyArrayToWasm: Boolean): InteropPointer {
        return if (array != null && array.size > 0) {
            val data = _malloc(array.size * 4)
            elements.add(data)
            if (copyArrayToWasm) toWasm(data, array.backing)
            data
        } else {
            0
        }
    }

    actual fun toInterop(array: NativePointerArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = true)

    actual fun toInteropForResult(array: NativePointerArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = false)

    actual fun InteropPointer.fromInterop(result: NativePointerArray) {
        return fromWasm(this@fromInterop, result.backing)
    }

    actual fun toInterop(stringArray: Array<String>?): InteropPointer {
        return if (stringArray != null && stringArray.isNotEmpty()) {
            val ptrs = stringArray.map {
                toInterop(it)
            }.toIntArray()

            toInterop(ptrs)
        } else {
            0
        }
    }

    actual fun InteropPointer.fromInteropNativePointerArray(): NativePointerArray {
        TODO("implement wasm fromInteropNativePointerArray")
    }

    actual inline fun <reified T> InteropPointer.fromInterop(decoder: ArrayInteropDecoder<T>): Array<T> {
        val size = decoder.getArraySize(this)
        val result = Array(size) {
            decoder.getArrayElement(this, it)
        }
        decoder.disposeArray(this)
        return result
    }

    actual fun toInteropForArraysOfPointers(interopPointers: Array<InteropPointer>): InteropPointer {
        return toInterop(interopPointers.toIntArray())
    }

    actual fun booleanCallback(callback: (() -> Boolean)?): NativePointer {
        if (callback == null) { return 0 }
        initCallbacks()
        val data = js("({})") as CallbackDataBoolean
        return _registerCallback({ data.value = callback() }, data, global = false)
    }

    actual fun intCallback(callback: (() -> Int)?): NativePointer {
        if (callback == null) { return 0 }
        initCallbacks()
        val data = js("({})") as CallbackDataInt
        return _registerCallback({ data.value = callback() }, data, global = false)
    }

    actual fun nativePointerCallback(callback: (() -> NativePointer)?): NativePointer {
        if (callback == null) { return 0 }
        initCallbacks()
        val data = js("({})") as CallbackDataNativePointer
        return _registerCallback({ data.value = callback() }, data, global = false)
    }

    actual fun interopPointerCallback(callback: (() -> InteropPointer)?): NativePointer {
        if (callback == null) { return 0 }
        initCallbacks()
        val data = js("({})") as CallbackDataInteropPointer
        return _registerCallback({ data.value = callback() }, data, global = false)
    }

    actual fun callback(callback: (() -> Unit)?): InteropPointer {
        if (callback == null) { return 0 }
        initCallbacks()

        return _registerCallback({ callback() }, null, global = false)
    }

    actual fun virtual(method: () -> Unit): InteropPointer {
        return _registerCallback({ method() }, null, global = true)
    }

    actual fun virtualBoolean(method: () -> Boolean): InteropPointer {
        val data = js("({})") as CallbackDataBoolean
        return _registerCallback({ data.value = method() }, data, global = true)
    }

    actual fun virtualInt(method: () -> Int): InteropPointer {
        val data = js("({})") as CallbackDataInt
        return _registerCallback({ data.value = method() }, data, global = true)
    }

    actual fun virtualNativePointer(method: () -> NativePointer): InteropPointer {
        val data = js("({})") as CallbackDataNativePointer
        return _registerCallback({ data.value = method() }, data, global = true)
    }

    actual fun virtualInteropPointer(method: () -> InteropPointer): InteropPointer {
        val data = js("({})") as CallbackDataInteropPointer
        return _registerCallback({ data.value = method() }, data, global = true)
    }

    actual fun release()  {
        elements.forEach {
            _free(it)
        }
        elements.clear()
        releaseCallbacks()
    }

    private inline fun initCallbacks() {
        if (!callbacksInitialized) {
            _createLocalCallbackScope()
            callbacksInitialized = true
        }
    }

    private inline fun releaseCallbacks() {
        if (callbacksInitialized) {
            _releaseLocalCallbackScope()
            callbacksInitialized = false
        }
    }
}

// Callbacks
internal external interface CallbackDataBoolean { @JsName("value") var value: Boolean? }
internal external interface CallbackDataInt { @JsName("value") var value: Int? }
internal external interface CallbackDataNativePointer { @JsName("value") var value: NativePointer? }
internal external interface CallbackDataInteropPointer { @JsName("value") var value: InteropPointer? }

// See `setup.js`
private external fun _registerCallback(cb: () -> Unit, data: Any?, global: Boolean): Int
private external fun _createLocalCallbackScope()
private external fun _releaseLocalCallbackScope()


// Those functions are defined by Emscripten.
@ExternalSymbolName("_malloc")
@ModuleImport("skia", "malloc")
private external fun _malloc(size: Int): NativePointer

@ExternalSymbolName("_free")
@ModuleImport("skia", "free")
private external fun _free(ptr: NativePointer)

private external fun lengthBytesUTF8(str: String): Int

private external fun stringToUTF8(str: String, outPtr: NativePointer, maxBytesToWrite: Int)

private external fun UTF8ToString(ptr: NativePointer): String

internal external interface HEAP<T> {
    fun set(src: T, dest: NativePointer)
    fun subarray(startIndex: Int, endIndex: Int): ArrayBufferView
}
internal external object HEAPU8: HEAP<ByteArray> {
    override fun set(src: ByteArray, dest: NativePointer)
    override fun subarray(startIndex: Int, endIndex: Int): ArrayBufferView
}

internal external object HEAPU16: HEAP<ShortArray> {
    override fun set(src: ShortArray, dest: NativePointer)
    fun set(src: CharArray, dest: NativePointer)
    override fun subarray(startIndex: Int, endIndex: Int): ArrayBufferView
}

internal external object HEAPU32: HEAP<IntArray> {
    override fun set(src: IntArray, dest: NativePointer)
    override fun subarray(startIndex: Int, endIndex: Int): ArrayBufferView
}

internal external object HEAPF32: HEAP<FloatArray> {
    override fun set(src: FloatArray, dest: NativePointer)
    override fun subarray(startIndex: Int, endIndex: Int): ArrayBufferView
}

internal external object HEAPF64: HEAP<DoubleArray> {
    override fun set(src: DoubleArray, dest: NativePointer)
    override fun subarray(startIndex: Int, endIndex: Int): ArrayBufferView
}

// Data copying routines.
internal expect fun toWasm(dest: NativePointer, src: ByteArray)
internal expect fun toWasm(dest: NativePointer, src: ShortArray)
internal expect fun toWasm(dest: NativePointer, src: CharArray)
internal expect fun toWasm(dest: NativePointer, src: FloatArray)
internal expect fun toWasm(dest: NativePointer, src: DoubleArray)
internal expect fun toWasm(dest: NativePointer, src: IntArray)

internal expect fun fromWasm(src: NativePointer, result: ByteArray)
internal expect fun fromWasm(src: NativePointer, result: ShortArray)
internal expect fun fromWasm(src: NativePointer, result: IntArray)
internal expect fun fromWasm(src: NativePointer, result: FloatArray)
internal expect fun fromWasm(src: NativePointer, result: DoubleArray)

actual class NativePointerArray actual constructor(size: Int) {
    internal val backing = IntArray(size)
    actual operator fun get(index: Int): NativePointer {
        return backing[index]
    }

    actual operator fun set(index: Int, value: NativePointer) {
        backing[index] = value
    }

    actual val size: Int
        get() = backing.size

    companion object {
        internal fun fromIntArray(intArray: IntArray): NativePointerArray {
            return NativePointerArray(intArray.size).apply {
                intArray.copyInto(backing)
            }
        }
    }
}