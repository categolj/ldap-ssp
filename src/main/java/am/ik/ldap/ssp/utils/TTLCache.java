package am.ik.ldap.ssp.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TTLCache<K, V> {

	// Inner class to hold the value along with its expiration time as an Instant
	private class CacheValue {

		final V value;

		final Instant expiryTime; // The moment when the entry expires

		CacheValue(V value, Instant expiryTime) {
			this.value = value;
			this.expiryTime = expiryTime;
		}

	}

	// A thread-safe map to store cache entries
	private final ConcurrentHashMap<K, CacheValue> map = new ConcurrentHashMap<>();

	// A scheduled executor with a custom thread name ("ttl-cleaner") to periodically
	// remove expired entries
	private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
		Thread thread = new Thread(r);
		thread.setName("ttl-cleaner");
		thread.setDaemon(true); // Daemon thread so it doesn't prevent JVM shutdown
		return thread;
	});

	/**
	 * Constructor.
	 * @param cleanupInterval Duration specifying the interval for running the cleanup
	 * task.
	 */
	public TTLCache(Duration cleanupInterval) {
		long cleanupIntervalMillis = cleanupInterval.toMillis();
		// Schedule a periodic task to remove expired cache entries
		cleaner.scheduleAtFixedRate(() -> {
			Instant now = Instant.now();
			for (K key : map.keySet()) {
				CacheValue cv = map.get(key);
				if (cv != null && now.isAfter(cv.expiryTime)) {
					map.remove(key);
				}
			}
		}, cleanupIntervalMillis, cleanupIntervalMillis, TimeUnit.MILLISECONDS);
	}

	/**
	 * Inserts a value into the cache with a specified TTL.
	 * @param key The key.
	 * @param value The value.
	 * @param ttl Duration representing the time-to-live.
	 */
	public void put(K key, V value, Duration ttl) {
		Instant expiryTime = Instant.now().plus(ttl);
		map.put(key, new CacheValue(value, expiryTime));
	}

	/**
	 * Retrieves the value associated with the given key. If the entry is expired, it
	 * removes the entry and returns null.
	 * @param key The key.
	 * @return The value if it exists and is not expired; otherwise, null.
	 */
	public V get(K key) {
		if (key == null) {
			return null;
		}
		CacheValue cv = map.get(key);
		if (cv == null) {
			return null;
		}
		// Check if the entry has expired
		if (Instant.now().isAfter(cv.expiryTime)) {
			map.remove(key);
			return null;
		}
		return cv.value;
	}

	/**
	 * Removes the entry associated with the given key.
	 * @param key The key to remove.
	 */
	public void remove(K key) {
		map.remove(key);
	}

	/**
	 * Shuts down the cleanup thread.
	 */
	public void shutdown() {
		cleaner.shutdown();
	}

}
