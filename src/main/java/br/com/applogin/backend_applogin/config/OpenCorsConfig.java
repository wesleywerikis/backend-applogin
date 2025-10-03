package br.com.applogin.backend_applogin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class OpenCorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:5500}")
    private String allowed;

    public CorsFilter corsFilter() {
        CorsConfiguration cfg = new CorsConfiguration();
        for (String o : allowed.split(","))
            cfg.addAllowedOrigin(o.trim());
        cfg.addAllowedHeader("*");
        cfg.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return new CorsFilter(src);
    }
}
