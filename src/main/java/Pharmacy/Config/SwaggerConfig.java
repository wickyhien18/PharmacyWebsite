package Pharmacy.Config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Indicates that the class declares one or more @Bean methods.
@Configuration
/**
 * Class SwaggerConfig.
 * Provides functionality and data modeling for SwaggerConfig.
 */
public class SwaggerConfig {

    // Indicates that a method produces a bean to be managed by the Spring container.
    @Bean
    /**
     * Custom open api.
     *
     * @return the OpenAPI result
     */
    public OpenAPI customOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Pharmacy API")
                        .version("1.0")
                        .description("API Pharmacy Managemnet"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")));
    }
}