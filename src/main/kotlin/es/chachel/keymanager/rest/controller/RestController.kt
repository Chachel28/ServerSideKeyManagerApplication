package es.chachel.keymanager.rest.controller

import es.chachel.keymanager.db.KeyPerUser
import es.chachel.keymanager.db.User
import es.chachel.keymanager.dto.*
import es.chachel.keymanager.security.WebSecurityConfiguration
import es.chachel.keymanager.service.DBService
import es.chachel.keymanager.service.ReswueService
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.*
import java.util.logging.Logger

@RestController
@RequestMapping("/api/v1")
class RestController(
    private val dbService: DBService,
    private val reswueService: ReswueService,
    private val bCryptPasswordEncoder: BCryptPasswordEncoder
) {

    @PostMapping("/user")
    fun saveUser(@RequestBody user: User): ResponseEntity<User> {
        user.password = bCryptPasswordEncoder.encode(user.password)
        val newUser = dbService.saveUser(user)
        return ResponseEntity.ok(newUser)
    }

    @GetMapping("/user")
    fun getAllUsers(): ResponseEntity<List<User>> {
        return ResponseEntity.ok(dbService.getAllUsers());
    }

    @GetMapping("/user/{username}")
    fun getUser(@PathVariable username: String): ResponseEntity<UserDTO> {
        val user = dbService.getUser(username)
        val token = dbService.getAutToken(user.user_id)
        if(!token.isNullOrEmpty()){
            val agentInfo = reswueService.getAgentInfo(token)
            user.avatar = agentInfo?.avatar!!
        }
        return ResponseEntity.ok(user)
    }

    @GetMapping("/getAllKeys")
    fun getAllKeys(): ResponseEntity<List<KeyPerUser>> {
        return ResponseEntity.ok(dbService.getAllKeys())
    }

    @GetMapping("/reswueURL")
    fun getEntryPoint(): ResponseEntity<String> {
        return ResponseEntity.ok(reswueService.getUrlToFindCode())
    }

    @PostMapping("/createReswueToken")
    fun getAccessToken(@RequestBody requestBody: AccessTokenRequestDTO): ResponseEntity<String> {
        val body = reswueService.getAccessToken(requestBody.code)
        dbService.saveToken(body, requestBody.user_name)
        return ResponseEntity.ok(body?.access_token)
    }

    @GetMapping("/isReswueOutdated/{username}")
    fun isReswueOutdated(@PathVariable username: String): ResponseEntity<Boolean> {
        val expireDate = dbService.getExpireDate(username)
        if (expireDate != null) {
            return if (Date.from(Instant.now()).after(expireDate)) {
                ResponseEntity.ok(true)
            } else {
                ResponseEntity.ok(false)
            }
        }
        return ResponseEntity.notFound().build()
    }

    @GetMapping("/refreshReswueToken/{username}")
    fun refreshReswueToken(@PathVariable username: String): ResponseEntity<AccessTokenResponseDTO> {
        val refreshToken = dbService.getRefreshToken(username)
        val body = reswueService.refreshTokenReswue(refreshToken)
        dbService.saveToken(body, username)
        return ResponseEntity.ok(body)
    }

    @GetMapping("/operations/{id}")
    fun getOperationList(@PathVariable id: Int): ResponseEntity<OperationListDTO> {
        val token = dbService.getAutToken(id)
        return ResponseEntity.ok(reswueService.getOperationList(token!!))
    }
}