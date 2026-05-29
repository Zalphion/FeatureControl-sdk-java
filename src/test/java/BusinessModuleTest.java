import com.zalphion.featurecontrol.ApplicationProperty;
import com.zalphion.featurecontrol.FeatureFlag;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BusinessModuleTest {

    private final Set<String> betaUsers = new HashSet<>(Arrays.asList("admin", "tester"));

    private boolean dashboardEnabled = false;
    private int excitementLevel = 3;

    private final BusinessModule module = new BusinessModule(
            FeatureFlag.create(recipient ->
                    betaUsers.contains(recipient) ? "on" : dashboardEnabled ? "on" : "off"
            ),
            ApplicationProperty.create(() -> excitementLevel)
    );

    @Test
    public void dashboardEnabled_forBetaUser() {
        final String html = module.renderIndex("admin");
        assertThat(html).contains("Go to Dashboard");
    }

    @Test
    public void dashboardDisabled_forEveryoneElse() {
        final String html = module.renderIndex("user1");
        assertThat(html).doesNotContain("Go to Dashboard");
    }


    @Test
    public void dashboardEnabled_forEveryone() {
        dashboardEnabled = true;
        final String html = module.renderIndex("user1");
        assertThat(html).contains("Go to Dashboard");
    }

    @Test
    public void excitementLevel_default() {
        final String html = module.renderIndex("user1");
        assertThat(html).contains("This is the homepage!");
    }

    @Test
    public void excitementLevel_excessive() {
        excitementLevel = 3;
        final String html = module.renderIndex("user1");
        assertThat(html).contains("This is the homepage!!!");
    }
}

