import org.springframework.cloud.gateway.config.RouteLocator;
import org.springframework.cloud.gateway.config.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class GatewayConfig implements WebFluxConfigurer {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r
                        .path("/user/**")
                        .uri("lb://user-service"),
                .route("auth-service", r -> r
                        .path("/auth/**")
                        .uri("lb://auth-service")),
                .route("transaction-service", r -> r
                        .path("/transaction/**")
                        .uri("lb://transaction-service"))
                .build();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
