package com.zalphion.featurecontrol.source;

import com.zalphion.featurecontrol.FeatureControl;
import com.zalphion.featurecontrol.TestFixtures;
import com.zalphion.featurecontrol.bundle.ApplicationBundle;
import com.zalphion.featurecontrol.lib.http.HttpFunction;
import com.zalphion.featurecontrol.lib.http.cache.CacheAwareHttpFunction;
import com.zalphion.featurecontrol.lib.result.Failure;
import com.zalphion.featurecontrol.lib.result.Success;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class ApplicationSourceContract {

    private final AtomicReference<Instant> clock = new AtomicReference<>(Instant.parse("2026-01-01T12:00:00Z"));
    private final FakeServer server = new FakeServer(clock, Duration.ofMinutes(30))
            .withBundle("key1", TestFixtures.bundle1);

    private final FeatureControl client;

    protected ApplicationSourceContract(@NonNull @lombok.NonNull HttpFunction http) {
        client = new FeatureControl(
                URI.create("http://localhost:" + server.start()),
                new CacheAwareHttpFunction(http, clock::get)
        );
    }

    @AfterEach
    public void cleanup() {
        server.stop();
    }

    @Test
    public void get_unauthorized() {
        assertThat(client.toFeatureSource("key2").get()).isEqualTo(new Failure<>("Invalid SDK key"));
        assertThat(server.getResponses()).containsExactly(
                new AbstractMap.SimpleEntry<>("key2", 401)
        );
    }

    @Test
    public void get_present() {
        assertThat(client.toFeatureSource("key1").get()).isEqualTo(new Success<>(TestFixtures.bundle1));
        assertThat(server.getResponses()).containsExactly(
                new AbstractMap.SimpleEntry<>("key1", 200)
        );
    }

    @Test
    public void get_cached_byMaxAge() {
        assertThat(client.toFeatureSource("key1").get()).isEqualTo(new Success<>(TestFixtures.bundle1));
        assertThat(client.toFeatureSource("key1").get()).isEqualTo(new Success<>(TestFixtures.bundle1));

        assertThat(server.getResponses()).containsExactly(
                new AbstractMap.SimpleEntry<>("key1", 200)
        );
    }

    @Test
    public void get_eTagExpired() {
        val bundle2 = ApplicationBundle.builder()
                .property("foo", "bar")
                .build();

        assertThat(client.toFeatureSource("key1").get()).isEqualTo(new Success<>(TestFixtures.bundle1));

        clock.updateAndGet(now -> now.plus(server.getMaxAge().plusSeconds(1)));
        server.withBundle("key1", bundle2);

        assertThat(client.toFeatureSource("key1").get()).isEqualTo(new Success<>(bundle2));

        assertThat(server.getResponses()).containsExactly(
                new AbstractMap.SimpleEntry<>("key1", 200),
                new AbstractMap.SimpleEntry<>("key1", 200)
        );
    }

    @Test
    public void get_cached_byETag() {
        assertThat(client.toFeatureSource("key1").get()).isEqualTo(new Success<>(TestFixtures.bundle1));
        clock.updateAndGet(now -> now.plus(server.getMaxAge().plusSeconds(1)));
        assertThat(client.toFeatureSource("key1").get()).isEqualTo(new Success<>(TestFixtures.bundle1));

        assertThat(server.getResponses()).containsExactly(
                new AbstractMap.SimpleEntry<>("key1", 200),
                new AbstractMap.SimpleEntry<>("key1", 304)
        );
    }
}
