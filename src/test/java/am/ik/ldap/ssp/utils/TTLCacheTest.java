package am.ik.ldap.ssp.utils;

import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TTLCacheTest {

	// Create a cache instance with a cleanup interval of 500 milliseconds
	private final TTLCache<String, String> cache = new TTLCache<>(Duration.ofMillis(500));

	@AfterEach
	void tearDown() {
		cache.shutdown();
	}

	@Test
	void testPutAndGetBeforeExpiration() {
		// Put a key-value pair with a TTL of 2 seconds
		cache.put("testKey", "testValue", Duration.ofSeconds(2));
		// Retrieve the value immediately, before TTL expiration
		String value = cache.get("testKey");
		assertThat(value).isEqualTo("testValue");
	}

	@Test
	void testGetAfterExpiration() throws InterruptedException {
		// Put a key-value pair with a TTL of 500 milliseconds
		cache.put("testKey", "testValue", Duration.ofMillis(500));
		// Wait for the TTL to expire
		Thread.sleep(501);
		// The value should now be expired and return null
		String value = cache.get("testKey");
		assertThat(value).isNull();
	}

	@Test
	void testRemove() {
		// Put a key-value pair with a TTL of 2 seconds
		cache.put("testKey", "testValue", Duration.ofSeconds(2));
		// Remove the entry explicitly
		cache.remove("testKey");
		// The value should be null after removal
		String value = cache.get("testKey");
		assertThat(value).isNull();
	}

	@Test
	void testAutomaticCleanup() throws InterruptedException {
		// Put a key-value pair with a TTL of 500 milliseconds
		cache.put("testKey", "testValue", Duration.ofMillis(500));
		// Wait long enough for the TTL to expire and the cleanup task to run
		Thread.sleep(1000);
		// The cleanup task should have removed the expired entry
		String value = cache.get("testKey");
		assertThat(value).isNull();
	}

}
