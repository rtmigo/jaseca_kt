/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * SPDX-FileCopyrightText: (c) 2022 Artyom IG <github.com/rtmigo>
 *
 * This file also includes excerpts from the Ehcache documentation
 * SPDX-FileCopyrightText: (c) Terracotta, Inc.
 */

package io.github.rtmigo.jasecache

import org.ehcache.PersistentCacheManager
import org.ehcache.config.builders.*
import org.ehcache.config.units.*
import org.ehcache.spi.loaderwriter.*
import java.io.Closeable
import java.nio.file.Path
import kotlin.time.*

@RequiresOptIn
annotation class Experimental

typealias Jase = java.io.Serializable

class JaseFileCacheConfig(
    var maxEntries: Int = 1_000_000, // one million
    var maxSizeBytes: Int = 1024 * 1024 * 256, // 256 MiB

    /**
     * The value for [ExpiryPolicyBuilder.timeToLiveExpiration].
     *
     * `null` means no expiration.
     **/
    var timeToLive: Duration? = null,

    /**
     * The value for [ExpiryPolicyBuilder.timeToIdleExpiration].
     *
     * `null` means no expiration.
     **/
    var timeToIdle: Duration? = null,
)

data class JaseEntry<K : Jase, V : Jase>(override val key: K, override val value: V) :
    Map.Entry<K, V>


/**
 * Persistent file cache for [java.io.Serializable] keys and values.
 **/
class JaseFileCache<K : Jase, V : Jase> private constructor(
    private val manager: PersistentCacheManager,
    private val data: org.ehcache.Cache<K, V>,
) : Closeable {

    override fun close() {
        this.manager.close()
    }

    companion object {
        /** Fast way to create a cache in temporary directory.
         *
         * ```
         * val cache = JisFileCache.createInTempDir("myCacheId")
         * ```
         *
         * The temporary directory will remain on disk after program completion. So the cache can be
         * reused between restarts. But after a reboot, there is a high chance that the files will
         * be deleted.
         *
         **/
        inline fun <reified K : Jase, reified V : Jase> inTempDir(
            id: String,
            noinline config: JaseFileCacheConfig.() -> Unit = {},
        ) = createJava<K, V>(toTempCacheDir(id), config, K::class.java, V::class.java)

        fun toTempCacheDir(id: String): Path {
            val parent = systemTempDir()
            val result = parent.resolve("jfc_$id")
            require(result.parent == parent) { "Wrong id value: '$id'" }
            return result
        }

        /** Create new cache instance. */
        inline fun <reified K : Jase, reified V : Jase> inDir(
            directory: Path,
            noinline config: JaseFileCacheConfig.() -> Unit = {},
        ): JaseFileCache<K, V> = createJava(
            rootDir = directory,
            config = config,
            classK = K::class.java,
            classV = V::class.java
        )

        /** Prefer using [inDir] in Kotlin. Don't use this directly. */
        fun <K : Jase, V : Jase> createJava(
            rootDir: Path,
            config: JaseFileCacheConfig.() -> Unit = {},
            classK: Class<K>,
            classV: Class<V>,
        ): JaseFileCache<K, V> {
            val alias = "cache"

            val cfg = JaseFileCacheConfig().apply(config)

            return CacheManagerBuilder.newCacheManagerBuilder()
                .with(
                    CacheManagerBuilder.persistence(rootDir.toFile()))
                .withCache(
                    alias,
                    CacheConfigurationBuilder
                        .newCacheConfigurationBuilder(
                            classK,
                            classV,
                            ResourcePoolsBuilder.newResourcePoolsBuilder()
                                .heap(cfg.maxEntries.toLong(), EntryUnit.ENTRIES)
                                .disk(cfg.maxSizeBytes.toLong(), MemoryUnit.B, true))
                        .let {
                            if (cfg.timeToLive != null)
                                it.withExpiry(
                                    ExpiryPolicyBuilder.timeToLiveExpiration(
                                        cfg.timeToLive!!.toJavaDuration()))
                            else
                                it
                        }.let {
                            if (cfg.timeToIdle != null)
                                it.withExpiry(
                                    ExpiryPolicyBuilder.timeToIdleExpiration(
                                        cfg.timeToIdle!!.toJavaDuration()))
                            else
                                it
                        }
                ).build(true).let { manager ->
                    JaseFileCache<K, V>(
                        manager,
                        manager.getCache(alias, classK, classV))
                }
        }
    }

    /**
     * Retrieves the value currently mapped to the provided key.
     *
     * @param key the key, may not be `null`
     * @return the value mapped to the key, `null` if none
     *
     * @throws NullPointerException if the provided key is `null`
     * @throws CacheLoadingException if the [CacheLoaderWriter] associated with this cache was
     * invoked and threw an `Exception`
     */
    @Throws(CacheLoadingException::class)
    operator fun get(key: K): V? = this.data.get(key)

    /**
     * Associates the given value to the given key in this `Cache`.
     *
     * @param key the key, may not be `null`
     * @param value the value, may not be `null`
     *
     * @throws NullPointerException if either key or value is `null`
     * @throws CacheWritingException if the [CacheLoaderWriter] associated with this cache threw an
     * [Exception] while writing the value for the given key to the underlying system of record.
     */
    @Throws(CacheWritingException::class)
    fun put(key: K, value: V) = this.data.put(key, value)

    @Throws(CacheWritingException::class)
    operator fun set(key: K, value: V) = this.data.put(key, value)

    /**
     * Checks whether a mapping for the given key is present, without retrieving the associated
     * value.
     *
     * @param key the key, may not be `null`
     * @return `true` if a mapping is present, `false` otherwise
     *
     * @throws NullPointerException if the provided key is `null`
     */
    fun containsKey(key: K): Boolean = this.data.containsKey(key)

    /**
     * Removes the value, if any, associated with the provided key.
     *
     * @param key the key to remove the value for, may not be `null`
     *
     * @throws NullPointerException if the provided key is `null`
     * @throws CacheWritingException if the [CacheLoaderWriter] associated with this cache threw an
     * [Exception] while removing the value for the given key from the underlying system of record.
     */
    @Throws(CacheWritingException::class)
    fun remove(key: K) = this.data.remove(key)

    /**
     * Retrieves all values associated with the given key set.
     *
     * @param keys keys to query for, may not contain `null`
     * @return a map from keys to values or `null` if the key was not mapped
     *
     * @throws NullPointerException if the `Set` or any of the contained keys are `null`.
     * @throws BulkCacheLoadingException if loading some or all values failed
     */
    @Throws(BulkCacheLoadingException::class)
    fun getAll(keys: Set<K>?): Map<K, V>? = this.data.getAll(keys)

    /**
     * Associates all the provided key:value pairs.
     *
     * @param entries key:value pairs to associate, keys or values may not be `null`
     *
     * @throws NullPointerException if the `Map` or any of the contained keys or values are `null`.
     * @throws BulkCacheWritingException if the [CacheLoaderWriter] associated with this cache threw
     * an [Exception] while writing given key:value pairs to the underlying system of record.
     */
    @Throws(BulkCacheWritingException::class)
    fun putAll(entries: Map<out K, V>?) = this.data.putAll(entries)

    /**
     * Removes any associated value for the given key set.
     *
     * @param keys keys to remove values for, may not be `null`
     *
     * @throws NullPointerException if the `Set` or any of the contained keys are `null`. @throws
     * BulkCacheWritingException if the [CacheLoaderWriter] associated with this cache threw an
     * [Exception] while removing mappings for given keys from the underlying system of record.
     */
    @Throws(BulkCacheWritingException::class)
    fun removeAll(keys: Set<K>?) = this.data.removeAll(keys)

    /**
     * Removes all mappings currently present in the `Cache`.
     *
     *
     * It does so without invoking the [CacheLoaderWriter] or any registered
     * [org.ehcache.event.CacheEventListener] instances.
     * *This is not an atomic operation and can potentially be very expensive.*
     */
    fun clear() = this.data.clear()

    /**
     * Maps the specified key to the specified value in this cache, unless a non-expired mapping
     * already exists.
     *
     * The value can be retrieved by calling the `get` method
     * with a key that is equal to the original key.
     *
     * Neither the key nor the value can be `null`.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the value to which the specified key was previously mapped,
     * or `null` if no such mapping existed or the mapping was expired
     *
     * @throws NullPointerException if any of the arguments is `null`
     * @throws CacheWritingException if the [CacheLoaderWriter] associated
     * with this cache threw an [Exception] while writing the value for the
     * given key to the underlying system of record.
     * @throws CacheLoadingException if the [CacheLoaderWriter]
     * associated with this cache was invoked and threw an [Exception]
     */
    @Throws(CacheLoadingException::class, CacheWritingException::class)
    fun putIfAbsent(key: K, value: V): V = this.data.putIfAbsent(key, value)

    /**
     * Removes the entry for a key only if currently mapped to the given value
     * and the entry is not expired.
     *
     * The key cannot be `null`.
     *
     * @param key key with which the specified value is associated
     * @param value value expected to be removed
     * @return true if the value was successfully removed
     *
     * @throws NullPointerException if any of the arguments is `null`
     * @throws CacheWritingException if the [CacheLoaderWriter] associated
     * with this cache threw an [Exception] while removing the value for the
     * given key from the underlying system of record.
     */
    @Throws(CacheWritingException::class)
    fun remove(key: K, value: V): Boolean = this.data.remove(key, value)

    /**
     * Replaces the entry for a key only if currently mapped to some value and the entry is not
     * expired.
     *
     * Neither the key nor the value can be `null`.
     *
     * @param key of the value to be replaced
     * @param value the new value
     * @return the existing value that was associated with the key, or `null` if
     * no such mapping existed or the mapping was expired
     *
     * @throws NullPointerException if any of the arguments is `null`
     * @throws CacheWritingException if the [CacheLoaderWriter] associated
     * with this cache threw an [Exception] while writing the value for the
     * given key to the underlying system of record.
     * @throws CacheLoadingException if the [CacheLoaderWriter]
     * associated with this cache was invoked and threw an [Exception]
     */
    @Throws(CacheLoadingException::class, CacheWritingException::class)
    fun replace(key: K, value: V): V = this.data.replace(key, value)

    /**
     * Replaces the entry for a key only if currently mapped to the given value
     * and the entry is not expired.
     *
     * Neither the key nor the value can be `null`.
     *
     * @param key key with which the specified value is associated
     * @param oldValue value expected to be associated with the specified key
     * @param newValue value to be associated with the specified key
     * @return true if the oldValue was successfully replaced by the newValue
     *
     * @throws NullPointerException if any of the arguments is `null`
     * @throws CacheWritingException if the [CacheLoaderWriter] associated
     * with this cache threw an [Exception] while writing the value for the
     * given key to the underlying system of record.
     * @throws CacheLoadingException if the [CacheLoaderWriter]
     * associated with this cache was invoked and threw an [Exception]
     */
    @Throws(CacheLoadingException::class, CacheWritingException::class)
    fun replace(key: K, oldValue: V, newValue: V): Boolean =
        this.data.replace(key, oldValue, newValue)

    /**
     * Returns a sequence will all the cache entries. Due to the interactions of the cache and
     * iterator contracts it is possible for iteration to return expired entries.
     **/
    val entries: Sequence<Map.Entry<K, V>>
        get() = sequence {
            this@JaseFileCache.data.asSequence().map { JaseEntry(it.key, it.value) }
        }

    /**
     * Returns all keys in this cache.
     *
     * Due to the interactions of the cache and iterator contracts it is possible for iteration to
     * return expired entries.
     */
    val keys: Sequence<K>
        get() =
            this@JaseFileCache.data.asSequence().map { it.key }

    /**
     * Returns all values in this cache.
     *
     * Due to the interactions of the cache and iterator contracts it is possible for iteration to
     * return expired entries.
     */
    val values: Sequence<V>
        get() =
            this@JaseFileCache.data.asSequence().map { it.value }
}

inline fun <K : Jase, V : Jase> JaseFileCache<K, V>.getOrPut(key: K, defaultValue: () -> V): V {
    val value = get(key)
    return if (value == null) {
        val answer = defaultValue()
        put(key, answer)
        answer
    } else {
        value
    }
}

inline fun <reified K : Jase, reified V : Jase> filecache(
    directory: Path,
    noinline config: JaseFileCacheConfig.() -> Unit = {},
) = JaseFileCache.inDir<K, V>(directory, config)

/**
 * Creates a [JaseFileCache] that stores data in the system temporary directory. [id] is an
 * arbitrary string that helps distinguish caches from each other.
 *
 * ```
 * filecache<String, Int>("myCache123")
 * ```
 **/
inline fun <reified K : Jase, reified V : Jase> filecache(
    id: String,
    noinline config: JaseFileCacheConfig.() -> Unit = {},
) = JaseFileCache.inTempDir<K, V>(id, config)