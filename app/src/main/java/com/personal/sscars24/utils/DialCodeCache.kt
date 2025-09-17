package com.personal.sscars24.utils

object DialCodeCache {
    private val cache = mutableMapOf<String, String?>()

    fun get(iso: String): String? = cache[iso.uppercase()]
    fun put(iso: String, dial: String?) { cache[iso.uppercase()] = dial }
    fun contains(iso: String): Boolean = cache.containsKey(iso.uppercase())
    fun clear() = cache.clear()
}
