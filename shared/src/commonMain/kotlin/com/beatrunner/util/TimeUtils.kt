package com.beatrunner.util

/**
 * Utility to get current timestamp in milliseconds Isolated to resolve resolution issues with
 * Clock.System in some files
 */
expect fun currentTimeMillis(): Long
