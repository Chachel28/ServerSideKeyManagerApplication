package es.chachel.keymanager.rest.controller

import es.chachel.keymanager.db.KeyPerUser
import es.chachel.keymanager.db.User
import es.chachel.keymanager.dto.AccessTokenRequestDTO
import es.chachel.keymanager.dto.AccessTokenResponseDTO
import es.chachel.keymanager.dto.OperationListDTO
import es.chachel.keymanager.security.WebSecurityConfiguration
import es.chachel.keymanager.service.DBService
import es.chachel.keymanager.service.ReswueService
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class RestController(private val dbService: DBService,
                     private val reswueService: ReswueService,
                     private val bCryptPasswordEncoder: BCryptPasswordEncoder) {

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

    @GetMapping("/getAllKeys")
    fun getAllKeys(): ResponseEntity<List<KeyPerUser>> {
        return ResponseEntity.ok(dbService.getAllKeys())
    }

    @GetMapping("/entrypoint")
    fun getEntryPoint(): ResponseEntity<String> {
        return ResponseEntity.ok(reswueService.getUrlToFindCode())
    }

    @PostMapping("/createReswueToken")
    fun getAccessToken(@RequestBody requestBody: AccessTokenRequestDTO): ResponseEntity<String> {
        val body = reswueService.getAccessToken(requestBody.code)
        dbService.saveToken(body, requestBody.user_id)
        return ResponseEntity.ok(body?.access_token)
    }

    @GetMapping("/expireDate/{id}")
    fun getExpireDate(@PathVariable id: Int): ResponseEntity<String> {
        return ResponseEntity.ok(dbService.getExpireDate(id))
    }

    @GetMapping("/refreshReswueToken/{id}")
    fun refreshReswueToken(@PathVariable id:Int): ResponseEntity<AccessTokenResponseDTO> {
        val refreshToken = dbService.getRefreshToken(id)
        val body = reswueService.refreshTokenReswue(refreshToken)
        dbService.saveToken(body, id)
        return ResponseEntity.ok(body)
    }

    @GetMapping("/operations/{id}")
    fun getOperationList(@PathVariable id: Int): ResponseEntity<OperationListDTO> {
        val token = dbService.getAutToken(id)
        return ResponseEntity.ok(reswueService.getOperationList(token))
    }
}