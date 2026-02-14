package com.beatrunner.util

actual fun currentTimeMillis(): Long {
    return java.lang.System.currentTimeMillis()
}
