[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# FeatureControl-sdk-jvm

> [!WARNING]
> Work in progress

Official JVM SDK for the Feature Control Platform.  Supports **Kotlin** and **Java**.

## Requirements

- JDK 17+

## Quickstart

<details open>
<summary>Kotlin</summary>

```kotlin
fun main() {
    /*
     * Build a FeatureFlags instance from the cloud provider.
     * The pre-fetching wrapper will cache the latest data and periodically refresh it.
     *
     * Fetching occurs in the background, but you can block on the `FeatureSource` for readiness.
     * This is not recommended for production servers, as it may stall the application.
     */
    val features = FeatureSource
        .http(FeatureControl.northAmerica, System.getenv("FEATURE_CONTROL_SDK_KEY"))
        .preFetching()

    /*
     * You can define a property or flag on init and share it within your application;
     * this is the idiomatic way to use FeatureControl.
     */
    val greetingProperty = features.stringProperty("greeting", default = "hello")
    val myFeatureFlag = features.flag("my-feature", defaultVariant = "off")

    /*
     * Get the latest property value.
     * If the features are not yet ready, the default value is returned.
     */
    println(greetingProperty.get())

    /*
     * Evaluate the flag for a recipient.
     * Different recipients may result in different variants, based on your remote configuration.
     * You must always provide a default in case the flag is not defined.
     */
    when(myFeatureFlag.getVariant("user1")) {
        "off" -> println("Don't do cool thing")
        "on" -> println("Do cool thing")
        "both" -> {
            println("Don't do cool thing")
            println("Do cool thing")
        }
        else -> println("Unknown variant")
    }
}
```

</details>

<details>
<summary>Java</summary>

```java
public class JavaQuickstart {

    public static void main(String[] args) {
        /*
         * Build a FeatureFlags instance from the cloud provider.
         * The pre-fetching wrapper will cache the latest data and periodically refresh it.
         *
         * Fetching occurs in the background, but you can block on the `FeatureSource` for readiness.
         * This is not recommended for production servers, as it may stall the application.
         */
        final var features = JavaFeatureSourceBuilder
                .http(FeatureControl.getNorthAmerica(), System.getenv("FEATURE_CONTROL_SDK_KEY"))
                .preFetching(null, null, null);


        /*
         * You can define a property or flag on init and share it within your application;
         * this is the idiomatic way to use FeatureControl.
         */
        final var greetingProperty = features.getProperty("greeting", "hello", (value) -> value);
        final var myFeatureFlag = features.getFlag("my-feature", "off");

        /*
         * Get the latest property value.
         * If the features are not yet ready, the default value is returned.
         */
        System.out.println(greetingProperty.get());

        /*
         * Evaluate the flag for a recipient.
         * Different recipients may result in different variants, based on your remote configuration.
         * You must always provide a default in case the flag is not defined.
         */

        switch(myFeatureFlag.getVariant("user1")) {
            case "on":
                System.out.println("Do cool thing");
                break;
            case "both":
                System.out.println("Do cool thing");
            case "off":
                System.out.println("Don't do cool thing");
                break;
            default:
                System.out.println("Unknown variant");
        }
    }
}
```

</details>

## Test Support

<details open>
<summary>Kotlin</summary>

```kotlin
class BusinessModule(
    private val random: Random,
    featureSource: FeatureSource,
) {
    private val treatsDoctrine = featureSource.flag("treats-doctrine", defaultVariant = "plenty")

    fun shouldGiveTreats(catId: String): Boolean {
        return when(treatsDoctrine.getVariant(catId)) {
            "copious" -> random.nextInt(10) > 2
            "plenty" -> random.nextInt(10) > 5
            else -> false
        }
    }
}

class FeatureTest {

    private var doctrine = "none"

    private val testObj = BusinessModule(
        random = Random(42),
        featureSource = FeatureSource.memory(
            flags = mapOf("treats-doctrine" to { doctrine })
        )
    )

    private fun calculateDispenseRate(ids: Collection<String>): Float {
        if (ids.isEmpty()) return 0f
        val results = ids.map { testObj.shouldGiveTreats(it) }
        return results.count { it }.toFloat() / results.size
    }

    private val testGroup = 1.rangeTo(1_000).map { "cat$it" }

    @Test
    fun `copious treats`() {
        doctrine = "copious"
        calculateDispenseRate(testGroup) shouldBe (0.683f plusOrMinus 0.05f)
    }

    @Test
    fun `plenty of treats`() {
        doctrine = "plenty"
        calculateDispenseRate(testGroup) shouldBe (0.381f plusOrMinus 0.05f)
    }

    @Test
    fun `no treats`() {
        calculateDispenseRate(testGroup) shouldBe 0f
    }
}
```

</details>

<details>
<summary>Java</summary>

```java
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
            ).uncached()
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
```

</details>