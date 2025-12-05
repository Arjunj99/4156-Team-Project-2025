package dev.coms4156.project.calorieservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for Cross-Origin Resource Sharing (CORS).
 */
@Configuration
public class CorsConfig {

  /**
   * Creates a {@link WebMvcConfigurer} bean that configures CORS mappings.
   *
   * @return the WebMvcConfigurer that sets CORS rules
   */
  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
          .allowedOrigins("http://localhost:5173")
          .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*");
      }
    };
  }
}
