package br.com.fiap.autointel.config;

import br.com.fiap.autointel.security.JwtAuthenticationFilter;
import br.com.fiap.autointel.security.JwtService;
import br.com.fiap.autointel.security.RestAccessDeniedHandler;
import br.com.fiap.autointel.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Regras de acesso da API (stateless, autenticação via JWT Bearer).
 *
 * <table>
 *     <tr><th>Endpoint</th><th>Acesso</th></tr>
 *     <tr><td>POST /api/v1/auth/login, POST /api/v1/usuarios (cadastro)</td><td>Público</td></tr>
 *     <tr><td>GET /api/v1/atributos/**</td><td>Público (catálogo)</td></tr>
 *     <tr><td>Swagger, /v3/api-docs, /actuator/health</td><td>Público</td></tr>
 *     <tr><td>GET /api/v1/veiculos/**</td><td>ADMIN, ANALISTA</td></tr>
 *     <tr><td>POST/PUT/DELETE /api/v1/veiculos/**, /api/v1/atributos/**; GET/PATCH /api/v1/usuarios/**</td><td>ADMIN</td></tr>
 *     <tr><td>/api/v1/consultas/**</td><td>ADMIN, ANALISTA (analista só vê as próprias)</td></tr>
 *     <tr><td>Demais</td><td>Autenticado</td></tr>
 * </table>
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] ROTAS_PUBLICAS = {
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**",
            "/actuator/health", "/actuator/health/**", "/h2-console/**", "/error"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService,
                                                   RestAuthenticationEntryPoint entryPoint,
                                                   RestAccessDeniedHandler accessDeniedHandler) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(h -> h.frameOptions(f -> f.sameOrigin())) // necessário para o console do H2
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(ROTAS_PUBLICAS).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/usuarios").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/atributos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/veiculos/**").hasAnyRole("ADMIN", "ANALISTA")
                        .requestMatchers("/api/v1/veiculos/**", "/api/v1/atributos/**", "/api/v1/usuarios/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/v1/consultas/**").hasAnyRole("ADMIN", "ANALISTA")
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }
}
