package net.lwenstrom.tft.backend.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ProductionAdminPasswordValidatorTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(context -> context.getEnvironment().setActiveProfiles("prod"))
            .withUserConfiguration(ProductionAdminPasswordValidator.class);

    @Test
    void rejectsBlankPasswordInProduction() {
        contextRunner.withPropertyValues("analytics.admin.password=").run(context -> {
            assertThat(context.getStartupFailure()).isNotNull();
            assertThat(context.getStartupFailure())
                    .hasRootCauseMessage("ANALYTICS_ADMIN_PASSWORD must be configured in production");
        });
    }

    @Test
    void acceptsConfiguredPasswordInProduction() {
        contextRunner.withPropertyValues("analytics.admin.password=tft123").run(context -> {
            assertThat(context.getStartupFailure()).isNull();
            assertThat(context.getBeansOfType(ProductionAdminPasswordValidator.class))
                    .hasSize(1);
        });
    }

    @Test
    void doesNotRequirePasswordOutsideProduction() {
        new ApplicationContextRunner()
                .withPropertyValues("analytics.admin.password=")
                .withUserConfiguration(ProductionAdminPasswordValidator.class)
                .run(context -> {
                    assertThat(context.getStartupFailure()).isNull();
                    assertThat(context.getBeansOfType(ProductionAdminPasswordValidator.class))
                            .isEmpty();
                });
    }
}
