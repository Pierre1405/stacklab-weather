package com.stacklabs.weather.cache

import com.github.benmanes.caffeine.cache.Cache
import org.slf4j.LoggerFactory
import java.util.function.Function

class DelegateCacheWithLog<K, V>(private val delegate: Cache<K,V>, private val name: String): Cache<K, V> by delegate {

    private val logger = LoggerFactory.getLogger(DelegateCacheWithLog::class.java)

    override fun get(key: K, getFunction: Function<in K, out V>?): V {
        if(delegate.getIfPresent(key) == null) {
            logger.debug("Cache {} miss: {}", name, key)
        } else {
            logger.debug("Cache {} hit: {}", name, key)
        }
        return delegate.get(key, getFunction)
    }
}