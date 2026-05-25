package com.zalphion.featurecontrol;

import com.zalphion.featurecontrol.source.JavaFeatureSource;
import com.zalphion.featurecontrol.source.JavaFeatureSourceBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Random;

public class JavaTest {

    private record BusinessModule(
            @NotNull Random random,
            @NotNull FeatureFlag treatsDoctrine
    ) {

        public static @NotNull BusinessModule create(
                @NotNull Random random,
                @NotNull JavaFeatureSource source
        ) {
            final var flag = source.getFlag("treats-doctrine", "plenty");
            return new BusinessModule(random, flag);
        }

        public boolean shouldGiveTreats(@NotNull String catId) {
            return switch (treatsDoctrine.getVariant(catId)) {
                case "copious" -> true;
                case "plenty" -> random.nextBoolean();
                default -> false;
            };
        }
    }

    private @NotNull String doctrine = "none";

    private final @NotNull BusinessModule testObj = BusinessModule.create(
            new Random(42),
            JavaFeatureSourceBuilder.memory(
                    new Features(
                        Map.of("treats-doctrine", (recipient) -> doctrine),
                        Map.of()
                    )
            )
    );

    @Test
    public void copiousTreats() {
        doctrine = "copious";
        Assertions.assertTrue(testObj.shouldGiveTreats("cat1"));
        Assertions.assertTrue(testObj.shouldGiveTreats("cat2"));
        Assertions.assertTrue(testObj.shouldGiveTreats("cat3"));
    }

    @Test
    public void plentyTreats() {
        doctrine = "plenty";
        Assertions.assertTrue(testObj.shouldGiveTreats("cat1"));
        Assertions.assertFalse(testObj.shouldGiveTreats("cat2"));
        Assertions.assertTrue(testObj.shouldGiveTreats("cat3"));
    }

    @Test
    public void noneTreats() {
        Assertions.assertFalse(testObj.shouldGiveTreats("cat1"));
        Assertions.assertFalse(testObj.shouldGiveTreats("cat2"));
        Assertions.assertFalse(testObj.shouldGiveTreats("cat3"));
    }
}

