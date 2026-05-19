package Pharmacy.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Indicates that the class declares one or more @Bean methods.
@Configuration
/**
 * Class CorsConfig.
 * Provides functionality and data modeling for CorsConfig.
 */
public class CorsConfig implements WebMvcConfigurer {

    @Override
    /**
     * Creates a new cors mappings.
     *
     * @param registry the registry
     */
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://127.0.0.1:5500", "http://localhost:5500")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}