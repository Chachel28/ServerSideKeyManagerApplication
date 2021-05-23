package es.chachel.keymanager.security

import com.fasterxml.jackson.databind.ObjectMapper
import es.chachel.keymanager.db.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.io.IOException
import java.net.URL
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.collections.ArrayList


class AuthenticationRequest(@get:JvmName("getAuthenticationManager_") private val authenticationManager: AuthenticationManager) :
    UsernamePasswordAuthenticationFilter() {
    @Throws(AuthenticationException::class)
    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        return try {
            val credentials= ObjectMapper().readValue(
                request.inputStream,
                User::class.java
            )
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    credentials.username,
                    credentials.password,
                    ArrayList()
                )
            )
        } catch (e: IOException) {
            throw RuntimeException("Could not request $e")
        }
    }

    override fun successfulAuthentication(
        request: HttpServletRequest?,
        response: HttpServletResponse,
        filterChain: FilterChain?,
        authentication: Authentication
    ) {
        val token = Jwts.builder()
            .setSubject((authentication.principal as org.springframework.security.core.userdetails.User).username)
            .setExpiration(Date(System.currentTimeMillis() + 864000000))
            .signWith(SignatureAlgorithm.HS512, "SecretKeyToGenJWTs".toByteArray())
            .compact()
        response.addHeader("Authorization", "Bearer $token")
        response.addHeader("Access-Control-Expose-Headers", "Authorization")
    }

    //TODO: Añadir un filtro para usuarios validados

    init {
        setFilterProcessesUrl("/api/v1/login")
    }
}