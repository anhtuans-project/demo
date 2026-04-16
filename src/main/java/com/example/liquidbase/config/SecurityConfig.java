package com.example.liquidbase.config;

import com.example.liquidbase.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Bật @PreAuthorize
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService, ClientRegistrationRepository clientRegistrationRepository) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Cấu hình CORS: cho phép React dev server (port 5173) gọi API
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Tắt CSRF chỉ cho các API endpoint (REST API không dùng CSRF cookie-based)
            // Nếu muốn giữ CSRF, frontend phải gửi token trong header
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/auth/logout") // Cho phép POST logout không cần CSRF token
            )

            .authorizeHttpRequests(authorize -> authorize
                // Public routes
                .requestMatchers("/", "/home", "/css/**", "/js/**", "/assets/**").permitAll()

                // Auth endpoints
                .requestMatchers("/login", "/login/initiate", "/callback").permitAll()
                .requestMatchers("/auth/me").permitAll()      // Frontend tự xử lý 401
                .requestMatchers("/auth/home").permitAll()    // Redirect sau OAuth2 login
                .requestMatchers("/auth/logout").authenticated()

                // Role-based routes
                .requestMatchers("/auth/admin/**", "/admin/**").hasRole("ADMIN")
                .requestMatchers("/auth/user/**", "/user/**").hasAnyRole("USER", "ADMIN")

                // Mọi request khác yêu cầu xác thực
                .anyRequest().authenticated()
            )

            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login") // Sử dụng trang login tùy chỉnh của chúng ta
                .authorizationEndpoint(authorization -> authorization
                    .authorizationRequestResolver(authorizationRequestResolver(clientRegistrationRepository))
                )
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService) // Sử dụng service custom
                )
                // Sau khi login thành công, redirect về /auth/home
                // Controller /auth/home sẽ redirect tiếp về frontend "/"
                .defaultSuccessUrl("/auth/home", true)
                // Failue URL gửi về frontend với error param
                .failureUrl("/?error=oauth2_failed")
            )

            .logout(logout -> logout
                .logoutUrl("/auth/logout") // Khớp với endpoint trong Controller
                .logoutSuccessUrl("/?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
            );

        return http.build();
    }

    private OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {

        DefaultOAuth2AuthorizationRequestResolver authorizationRequestResolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository, "/oauth2/authorization");

        authorizationRequestResolver.setAuthorizationRequestCustomizer(
                authorizationRequestCustomizer -> {
                    // Kiểm tra nếu là login qua Google
                    if (authorizationRequestCustomizer.build().getAttributes().get("registration_id").equals("google")) {
                        Map<String, Object> additionalParameters = new LinkedHashMap<>(
                                authorizationRequestCustomizer.build().getAdditionalParameters());
                        additionalParameters.put("prompt", "select_account");
                        authorizationRequestCustomizer.additionalParameters(additionalParameters);
                    }
                }
        );

        return authorizationRequestResolver;
    }

    /**
     * Cấu hình CORS để React frontend (dev: port 5173, prod: cùng domain) có thể gọi API.
     * withCredentials = true ở frontend để gửi session cookie cùng request.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Cho phép các origin này (thêm production domain nếu cần)
        config.setAllowedOrigins(List.of(
            "http://localhost:3000",   // Cổng hiện tại của bạn
            "http://localhost:5173",   // Vite dev server mặc định
            "http://localhost:8081"    // Production / proxy
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // Quan trọng: để gửi session cookie
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
