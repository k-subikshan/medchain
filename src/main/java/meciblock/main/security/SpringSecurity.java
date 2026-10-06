package meciblock.main.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import meciblock.main.Service.UserService;

@Configuration
@EnableWebSecurity
public class SpringSecurity {

    private final SuccessHandelr successHandler;
    public SpringSecurity(
            SuccessHandelr successHandler,
            UserService userService) {

        this.successHandler = successHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {

        return http
            .formLogin(httpform -> {
                httpform
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .failureUrl("/login?error=true")
                    .successHandler(successHandler)
                    .permitAll();
            })

            .authorizeHttpRequests(auth -> {
                auth
                    .requestMatchers("/register", "/login", "/error").permitAll()
                    .requestMatchers("/admin/**").hasRole("admin")
                    .requestMatchers("/doctor/**").hasRole("doctor")
                    .requestMatchers("/patient/**").hasRole("patient")
                    .requestMatchers("/pharmacy/**").hasRole("pharmacist")
                    .requestMatchers("/manufacturer/**", "/manfacturer/**").hasRole("manfacturer")
                    .requestMatchers("/css/**").permitAll()
                    .requestMatchers("/js/**").permitAll()
                    .requestMatchers("/images/**").permitAll()
                    .anyRequest().authenticated();
            })
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .build();
    }
}