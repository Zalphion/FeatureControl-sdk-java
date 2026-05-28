package com.zalphion.featurecontrol;

import lombok.Builder;
import lombok.NonNull;
import lombok.val;

import java.util.Arrays;

@Builder
public class BusinessModule {

    private final @NonNull FeatureFlag dashboardFlag;
    private final @NonNull ApplicationProperty<Integer> excitementLevel;

    public @NonNull String renderIndex(String principalId) {
        val html = new StringBuilder();
        html.append("<html><body>");
        html.append("<h1>Welcome to our website</h1>");
        html.append("<p>This is the homepage").append(generateExcitement()).append("</p>");

        if ("on".equals(dashboardFlag.getVariant(principalId))) {
            html.append("<a href=\"/dashboard\">Go to Dashboard</a>");
        }

        html.append("</body></html>");
        return html.toString();
    }

    private @NonNull String generateExcitement() {
        final int currentLevel = excitementLevel.getValue();
        char[] chars = new char[currentLevel];
        Arrays.fill(chars, '!');
        return new String(chars);
    }
}
