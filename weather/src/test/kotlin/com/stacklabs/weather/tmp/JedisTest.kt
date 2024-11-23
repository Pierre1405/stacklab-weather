package com.stacklabs.weather.tmp

import org.junit.jupiter.api.Test
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig


class JedisTest {

    @Test
    fun test() {
        val jedisPoolConfig = JedisPoolConfig()
        val jedisPool = JedisPool(jedisPoolConfig, "localhost", 6379, "default", "redis-pwd")
        val jedis = jedisPool.resource
        jedis.set("test", "test")
        //SetParams
        println(jedis.get("test"))
        println(jedis.get("toto"))

        jedis.close()
    }
}