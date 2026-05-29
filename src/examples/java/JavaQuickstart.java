import com.zalphion.featurecontrol.ApplicationProperty;
import com.zalphion.featurecontrol.FeatureControl;
import com.zalphion.featurecontrol.FeatureFlag;
import com.zalphion.featurecontrol.source.ApplicationSource;

public class JavaQuickstart {

    public static void main(String[] args) {
        /*
         * Build a FeatureFlags instance from the Feature Control: Canada region.
         * The pre-fetching wrapper will cache the latest data and periodically refresh it.
         */
        final ApplicationSource source = FeatureControl.canada()
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
