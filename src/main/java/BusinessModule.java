import com.zalphion.featurecontrol.ApplicationProperty;
import com.zalphion.featurecontrol.FeatureFlag;

import java.util.Arrays;

public class BusinessModule {
    private final FeatureFlag dashboardFlag;
    private final ApplicationProperty<Integer> excitementLevel;

    public BusinessModule(
            FeatureFlag dashboardFlag,
            ApplicationProperty<Integer> excitementLevel
    ) {
        this.dashboardFlag = dashboardFlag;
        this.excitementLevel = excitementLevel;
    }

    public String renderIndex(String principalId) {
        final StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<h1>Welcome to our website</h1>");
        html.append("<p>This is the homepage").append(generateExcitement()).append("</p>");

        if ("on".equals(dashboardFlag.getVariant(principalId))) {
            html.append("<a href=\"/dashboard\">Go to Dashboard</a>");
        }

        html.append("</body></html>");
        return html.toString();
    }

    private String generateExcitement() {
        final int currentLevel = excitementLevel.getValue();
        char[] chars = new char[currentLevel];
        Arrays.fill(chars, '!');
        return new String(chars);
    }
}
