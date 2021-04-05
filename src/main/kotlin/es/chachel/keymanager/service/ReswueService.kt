package es.chachel.keymanager.service

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import es.chachel.keymanager.dto.AccessTokenResponseDTO
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.PropertySource
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange

@Service
@PropertySource("/reswue.properties")
class ReswueService {
    val restTemplate: RestTemplate = RestTemplate()

    @Value("\${reswue.entrypoint}")
    private lateinit var entryPoint: String

    @Value("\${reswue.client_id}")
    private lateinit var clientId: String

    @Value("\${reswue.redirect_uri}")
    private lateinit var redirectUri: String

    @Value("\${reswue.scope}")
    private lateinit var scope: String

    @Value("\${reswue.grant_type}")
    private lateinit var grantType: String

    @Value("\${reswue.client_secret}")
    private lateinit var clientSecret: String

    fun getAccessToken(code: String): AccessTokenResponseDTO? {
        val headers = HttpHeaders()
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)

        val map = LinkedMultiValueMap<String, String>(5)
        map.add("grant_type", grantType)
        map.add("client_id", clientId)
        map.add("client_secret", clientSecret)
        map.add("redirect_uri", redirectUri)
        map.add("code", code)

        val httpEntity = HttpEntity(map, headers)
        val response: ResponseEntity<AccessTokenResponseDTO> = restTemplate.exchange(
                entryPoint.plus("/oauth/token"),
                HttpMethod.POST,
                httpEntity,
                jacksonTypeRef<AccessTokenResponseDTO>())
        return response.body
    }

    fun getUrlToFindCode(): String {
        return entryPoint
                .plus("/oauth/authorize?client_id=")
                .plus(clientId)
                .plus("&redirect_uri=")
                .plus(redirectUri)
                .plus("&response_type=code&scope=")
                .plus(scope)
    }
}