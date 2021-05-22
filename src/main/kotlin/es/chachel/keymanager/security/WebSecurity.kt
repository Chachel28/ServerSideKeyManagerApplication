package es.chachel.keymanager.security

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Configuration
@EnableWebSecurity
class WebSecurityConfiguration(
    @param:Qualifier("userDetailServiceImpl") private val userDetailsService: UserDetailsService,
    val bCryptPasswordEncoder: BCryptPasswordEncoder
) :
    WebSecurityConfigurerAdapter() {
    @Throws(Exception::class)
    override fun configure(httpSecurity: HttpSecurity) {
        httpSecurity.cors().and().csrf().disable()
            .authorizeRequests()
            .antMatchers(HttpMethod.POST, "/api/v1/user").permitAll()
            .antMatchers(HttpMethod.GET, "/api/v1/user").permitAll()
            .anyRequest().authenticated()
            .and().addFilter(AuthenticationRequest(authenticationManager()))
            .addFilter(Authorization(authenticationManager()))
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }

    @Throws(Exception::class)
    public override fun configure(authenticationManagerBuilder: AuthenticationManagerBuilder) {
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder)
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", CorsConfiguration().applyPermitDefaultValues())
        return source
    }
}