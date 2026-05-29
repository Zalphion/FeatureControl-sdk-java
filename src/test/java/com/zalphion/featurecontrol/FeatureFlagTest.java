package com.zalphion.featurecontrol;

import com.zalphion.featurecontrol.bundle.FeatureBundle;
import com.zalphion.featurecontrol.bundle.FlagBundle;
import com.zalphion.featurecontrol.bundle.VariantBucket;
import com.zalphion.featurecontrol.lib.result.Failure;
import com.zalphion.featurecontrol.lib.result.Result;
import com.zalphion.featurecontrol.source.FeatureSource;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import lombok.val;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class FeatureFlagTest {

    private final FeatureSource source = TestFixtures.bundle1.toSource();
    private final FeatureFlag flag = source.flag("lasers", "off");

    @Test
    public void getVariant_fromOverride() {
        assertThat(flag.getVariant("user1")).isEqualTo("on");
        assertThat(flag.getVariant("user2")).isEqualTo("off");
    }

    @Test
    public void getVariant_notFound() {
        assertThat(source.flag("missing", "default").getVariant("user1"))
                .isEqualTo("default");

    }

    @Test
    public void getVariant_sourceFailure() {
        val source = FeatureSource.create(new Failure<>("foo"));
        assertThat(source.flag("lasers", "default").getVariant("user1"))
                .isEqualTo("default");
    }

    @Test
    public void getVariant_sourceException() {
        val source = new FeatureSource() {
            @Override
            protected @NonNull @lombok.NonNull Result<FeatureBundle> getInternal() throws Exception {
                throw new IOException("stuff broke");
            }
        };

        assertThat(source.flag("lasers", "default").getVariant("user1"))
                .isEqualTo("default");
    }

    @Test
    public void getVariant_fromBucketing() {
        assertThat(flag.getVariant("user3")).isEqualTo("on");
        assertThat(flag.getVariant("user6")).isEqualTo("off");
    }

    @Test
    public void getVariant_emptyBuckets() {
        assertThat(source.flag("treats", "none").getVariant("toggles"))
                .isEqualTo("none");
    }

    @Test
    public void getVariant_stickyBuckets() {
        val offThreshold = new AtomicInteger(2);

        val flag = FeatureSource.create(() -> {
            val flagBundle = FlagBundle.builder()
                    .bucket(new VariantBucket("off", offThreshold.get()))
                    .bucket(new VariantBucket("on", 8))
                    .saltBase64("bGFzZXJz")
                    .build();

            return FeatureBundle.builder()
                    .flag("lasers", flagBundle)
                    .build();
        }).flag("lasers", "off");

        val offSubjectsBefore = getManyVariants(flag)
                .stream()
                .filter(e -> e.getValue().equals("off"))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        offThreshold.set(4);

        val offSubjectsAfter = getManyVariants(flag)
                .stream()
                .filter(e -> e.getValue().equals("off"))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        assertThat(offSubjectsAfter).hasSizeGreaterThan(offSubjectsBefore.size());
        assertThat(offSubjectsAfter).containsAll(offSubjectsBefore);
    }

    private static @NonNull List<Map.Entry<String, String>> getManyVariants(FeatureFlag flag) {
        val results = new ArrayList<Map.Entry<String, String>>();

        for (int i = 0; i < 1000; i++) {
            val recipient = "user" + i;
            val variant = flag.getVariant(recipient);
            results.add(new AbstractMap.SimpleEntry<>(recipient, variant));
        }

        return results;
    }
}
