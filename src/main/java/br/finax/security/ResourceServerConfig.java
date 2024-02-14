package br.finax.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/login/**").permitAll()
                .antMatchers("/user/change-forgeted-password").permitAll()
                .antMatchers("/user/get-by-email").permitAll()
                .antMatchers("/user/{id}").permitAll()
                .antMatchers("/user-configs/save").permitAll()
                .anyRequest().authenticated();

//        http.requiresChannel().anyRequest().requiresSecure();
    }
}
