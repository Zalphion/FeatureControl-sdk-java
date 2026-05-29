import com.zalphion.featurecontrol.ApplicationProperty;
import com.zalphion.featurecontrol.FeatureFlag;
import org.junit.jupiter.api.Test;
import lombok.val;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BusinessModuleTest {

    private final Set<String> betaUsers = new HashSet<>(Arrays.asList("admin", "tester"));

    private boolean dashboardEnabled = false;
    private int excitementLevel = 3;

    private final BusinessModule module = BusinessModule.builder()
            .dashboardFlag(FeatureFlag.create(recipient ->
                    betaUsers.contains(recipient) ? "on" : dashboardEnabled ? "on" : "off"
            ))
            .excitementLevel(ApplicationProperty.create(() -> excitementLevel))

            .build();

    @Test
    public void dashboardEnabled_forBetaUser() {
        val html = module.renderIndex("admin");
        assertThat(html).contains("Go to Dashboard");
    }

    @Test
    public void dashboardDisabled_forEveryoneElse() {
        val html = module.renderIndex("user1");
        assertThat(html).doesNotContain("Go to Dashboard");
    }


    @Test
    public void dashboardEnabled_forEveryone() {
        dashboardEnabled = true;
        val html = module.renderIndex("user1");
        assertThat(html).contains("Go to Dashboard");
    }

    @Test
    public void excitementLevel_default() {
        val html = module.renderIndex("user1");
        assertThat(html).contains("This is the homepage!");
    }

    @Test
    public void excitementLevel_excessive() {
        excitementLevel = 3;
        val html = module.renderIndex("user1");
        assertThat(html).contains("This is the homepage!!!");
    }
}

