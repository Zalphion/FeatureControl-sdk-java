# FeatureControl-sdk-java

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

> [!WARNING]
> Work in progress

Official Java SDK for the Feature Control Platform.

## Requirements

- Java 8+
- slf4j-api: 1.4.3+

## Quickstart

### Gradle

TODO central badge

```kotlin
dependencies {
    implementation("com.zalphion.featurecontrol:sdk-java-okhttp5:<version>")
  
    // Or legacy OkHttp3
    implementation("com.zalphion.featurecontrol:sdk-java-okhttp4:<version>")
  
    // Or when you're already in dependency hell
    implementation("com.zalphion.featurecontrol:sdk-java-8:<version>")
}
```

```java
public class Quickstart {

    public static void main(String[] args) {
        /*
         * Build a FeatureFlags instance from the Feature Control: Canada region.
         * The pre-fetching wrapper will cache the latest data and periodically refresh it.
         */
        final ApplicationSource source = FeatureControl.canada(new OkHttp3HttpFunction())
                .toFeatureSource(System.getenv("FEATURE_CONTROL_SDK_KEY"))
                .preFetching();

        /*
         * You can define a property or flag on init and share it within your application;
         * this is the idiomatic way to use FeatureControl.
         */
        ApplicationProperty<String> greetingProperty = source.stringProperty("greeting", "hello");
        FeatureFlag myFeatureFlag = source.flag("my-feature", "off");

        /*
         * Get the latest property value.
         * If the features are not yet ready, the default value is returned.
         */
        System.out.println(greetingProperty.getValue());

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

## Examples

- [Examples](https://github.com/Zalphion/FeatureControl-sdk-java/tree/main/src/main/java)
- [Testing](https://github.com/Zalphion/FeatureControl-sdk-java/tree/main/src/test/java)