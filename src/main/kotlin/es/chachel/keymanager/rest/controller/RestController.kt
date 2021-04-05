package es.chachel.keymanager.rest.controller

import es.chachel.keymanager.db.KeyPerUser
import es.chachel.keymanager.dto.AccessTokenRequestDTO
import es.chachel.keymanager.service.DBService
import es.chachel.keymanager.service.ReswueService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class RestController(private val dbService: DBService,
                     private val reswueService: ReswueService) {

    @GetMapping("/getAllKeys")
    fun getAllKeys(): ResponseEntity<List<KeyPerUser>> {
        return ResponseEntity.ok(dbService.getAllKeys())
    }

    @GetMapping("/entrypoint")
    fun getEntryPoint(): ResponseEntity<String> {
        return ResponseEntity.ok(reswueService.getUrlToFindCode())
    }

    @PostMapping("/getAccessToken")
    fun getAccessToken(@RequestBody requestBody: AccessTokenRequestDTO): ResponseEntity<String> {
        val body = reswueService.getAccessToken(requestBody.code)
        dbService.saveToken(body, requestBody.user_id)
        return ResponseEntity.ok(body?.access_token)
    }
}