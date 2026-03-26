package com.tramplin.backend.config;

import com.tramplin.backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // Используем твой CorsConfig для настройки CORS
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // 1. Разрешаем предварительные OPTIONS запросы от браузера (для CORS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. Разрешаем всё для регистрации, логина и документации Swagger
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**"
                        ).permitAll()

                        // 3. Разрешаем ПУБЛИЧНЫЙ ПРОСМОТР (только GET) для всех.
                        //    Это включает ленту/карту и детальные страницы вакансий
                        .requestMatchers(HttpMethod.GET, "/api/v1/opportunities/**").permitAll()
                        //    Это включает просмотр публичного профиля компании
                        .requestMatchers(HttpMethod.GET, "/api/v1/employers/{id}").permitAll()

                        // 4. Полный доступ к админ-панели только для роли ADMIN
                        .requestMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN")

                        // 5. ВСЕ ОСТАЛЬНЫЕ ЗАПРОСЫ требуют валидный JWT токен
                        .anyRequest().authenticated()
                )
                // Мы не храним сессии, каждый запрос должен нести токен
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}