package es.chachel.keymanager.service

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import es.chachel.keymanager.dto.*
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
    val restTemplate = RestTemplate()

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

    @Value("\${reswue.grant_type.refresh}")
    private lateinit var grantTypeRefresh: String

    @Value("\${reswue.client_secret}")
    private lateinit var clientSecret: String

    @Value("\${reswue.endpoint.authorize}")
    private lateinit var endpointAuthorize: String

    @Value("\${reswue.endpoint.token}")
    private lateinit var endpointToken: String

    @Value("\${reswue.endpoint.operations}")
    private lateinit var endpointOperations: String

    @Value("\${reswue.endpoint.agent}")
    private lateinit var endpointAgent: String

    fun getUrlToFindCode(): String {
        return entryPoint
            .plus(endpointAuthorize)
            .plus("?client_id=")
            .plus(clientId)
            .plus("&redirect_uri=")
            .plus(redirectUri)
            .plus("&response_type=code&scope=")
            .plus(scope)
    }

    fun getAccessToken(code: String): AccessTokenResponseDTO? {
        val headers = HttpHeaders()
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)

        val body = LinkedMultiValueMap<String, String>(5)
        body.add("grant_type", grantType)
        body.add("client_id", clientId)
        body.add("client_secret", clientSecret)
        body.add("redirect_uri", redirectUri)
        body.add("code", code)

        val request = HttpEntity(body, headers)
        val response: ResponseEntity<AccessTokenResponseDTO> = restTemplate.exchange(
            entryPoint.plus(endpointToken),
            HttpMethod.POST,
            request,
            jacksonTypeRef<AccessTokenResponseDTO>()
        )
        return response.body
    }

    fun refreshTokenReswue(refreshToken: String): AccessTokenResponseDTO? {
        val headers = HttpHeaders()
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)

        val body = LinkedMultiValueMap<String, String>(4)
        body.add("grant_type", grantTypeRefresh)
        body.add("refresh_token", refreshToken)
        body.add("client_id", clientId)
        body.add("client_secret", clientSecret)

        val request = HttpEntity(body, headers)
        val response: ResponseEntity<AccessTokenResponseDTO> = restTemplate.exchange(
            entryPoint.plus(endpointToken),
            HttpMethod.POST,
            request,
            jacksonTypeRef<AccessTokenResponseDTO>()
        )
        return response.body
    }

    fun getOperationList(token: String): OperationListDTO? {
        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer ".plus(token))

        val request = HttpEntity(null, headers)
        val response: ResponseEntity<OperationListDTO> = restTemplate.exchange(
            entryPoint.plus(endpointOperations).plus("?active=true"),
            HttpMethod.GET,
            request,
            jacksonTypeRef<OperationListDTO>()
        )
        if(!response.body?.links?.next.isNullOrEmpty()){
            val finalList:List<Data>? = response.body?.data?.plus(getNextOperationList(response.body?.links?.next!!, token))
            return OperationListDTO(finalList!!, response.body?.links!!, response.body?.meta!!)
        }
        return response.body
    }

    fun getNextOperationList(url:String, token: String): List<Data> {
        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer ".plus(token))

        val request = HttpEntity(null, headers)
        val response: ResponseEntity<OperationListDTO> = restTemplate.exchange(
            url.plus("&active=true"),
            HttpMethod.GET,
            request,
            jacksonTypeRef<OperationListDTO>()
        )
        if(!response.body?.links?.next.isNullOrEmpty()){
            val finalList:List<Data>? = response.body?.data?.plus(getNextOperationList(response.body?.links?.next!!, token))
            return finalList!!
        }
        return response.body?.data!!
    }

    fun getAgentInfo(token: String): AgentInfoDTO? {
        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer ".plus(token))

        val request = HttpEntity(null, headers)
        val response: ResponseEntity<AgentInfoDTO> = restTemplate.exchange(
            entryPoint.plus(endpointAgent),
            HttpMethod.GET,
            request,
            jacksonTypeRef<AgentInfoDTO>()
        )
        return response.body
    }

    fun getPortalList(token: String, operationSlug: String): PortalDTO? {
        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer ".plus(token))

        val request = HttpEntity(null, headers)
        val response: ResponseEntity<PortalDTO> = restTemplate.exchange(
            entryPoint.plus(endpointOperations)
                .plus(operationSlug).plus("/portal"),
            HttpMethod.GET,
            request,
            jacksonTypeRef<PortalDTO>()
        )
        if(!response.body?.links?.next.isNullOrEmpty()){
            val finalList:List<PortalData>? = response.body?.data?.plus(getNextPortalList(response.body?.links?.next!!, token))
            return PortalDTO(finalList!!, response.body?.links!!, response.body?.meta!!)
        }
        return response.body
    }

    fun getNextPortalList(url:String, token: String): List<PortalData> {
        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer ".plus(token))

        val request = HttpEntity(null, headers)
        val response: ResponseEntity<PortalDTO> = restTemplate.exchange(
            url,
            HttpMethod.GET,
            request,
            jacksonTypeRef<PortalDTO>()
        )
        if(!response.body?.links?.next.isNullOrEmpty()){
            val finalList:List<PortalData>? = response.body?.data?.plus(getNextPortalList(response.body?.links?.next!!, token))
            return finalList!!
        }
        return response.body?.data!!
    }
}