package net.lwenstrom.tft.backend.analytics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
class ProductionAdminPasswordValidator {
    ProductionAdminPasswordValidator(@Value("${analytics.admin.password:}") String configuredPassword) {
        if (configuredPassword.isBlank()) {
            throw new IllegalStateException("ANALYTICS_ADMIN_PASSWORD must be configured in production");
        }
    }
}
