package es.chachel.keymanager.rest.controller

import es.chachel.keymanager.db.KeyPerUser
import es.chachel.keymanager.db.User
import es.chachel.keymanager.dto.*
import es.chachel.keymanager.service.DBService
import es.chachel.keymanager.service.ReswueService
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.*

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

    @GetMapping("/user/{username}")
    fun getUser(@PathVariable username: String): ResponseEntity<UserDTO> {
        val user = dbService.getUser(username)
        val token = dbService.getAutToken(user.user_id)
        if (!token.isNullOrEmpty()) {
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
        return ResponseEntity.ok("token")
    }

    @GetMapping("/isReswueOutdated/{username}")
    fun isReswueOutdated(@PathVariable username: String): ResponseEntity<Boolean> {
        val expireDate = dbService.getExpireDate(username)
        if (expireDate != null) {
            return if (Date.from(Instant.now()).after(expireDate)) {
                refreshReswueToken(username)
                ResponseEntity.ok(true)
            } else {
                ResponseEntity.ok(false)
            }
        }
        return ResponseEntity.notFound().build()
    }

    @GetMapping("/refreshReswueToken/{username}")
    fun refreshReswueToken(@PathVariable username: String): ResponseEntity<String> {
        val refreshToken = dbService.getRefreshToken(username)
        val body = reswueService.refreshTokenReswue(refreshToken)
        dbService.saveToken(body, username)
        return ResponseEntity.ok("token")
    }

    @GetMapping("/operations/{id}")
    fun getOperationList(@PathVariable id: Int): ResponseEntity<OperationListDTO> {
        val token = dbService.getAutToken(id)
        if (token.isNullOrEmpty()) {
            return ResponseEntity.notFound().build()
        }
        return ResponseEntity.ok(reswueService.getOperationList(token))
    }

    @GetMapping("/operations/{id}/{operationSlug}")
    fun getPortalList(@PathVariable id: Int, @PathVariable operationSlug: String): ResponseEntity<PortalListDTO> {
        val token = dbService.getAutToken(id)
        if (token.isNullOrEmpty()) {
            return ResponseEntity.notFound().build()
        }

        val portalList = reswueService.getPortalList(token, operationSlug)
        portalList?.data?.forEach { portalData ->
            if (!dbService.checkPortal(portalData.guid)) {
                dbService.savePortal(portalData)
            }
        }
        val finalList = PortalListDTO(
            ArrayList(portalList?.data?.map { portalData ->
                PortalListItem(
                    portal_id = dbService.getIDPortal(portalData.guid),
                    portal_name = portalData.name,
                    latitude = portalData.lat.toFloat(),
                    longitude = portalData.lng.toFloat(),
                    guid = portalData.guid,
                    keys = dbService.getPortalKeys(dbService.getIDPortal(portalData.guid))?.sumOf { it.quantity }!!
                )
            }),
            portalList?.links!!,
            portalList.meta
        )

        return ResponseEntity.ok(finalList)
    }

    @GetMapping("/portal/keys/{id}")
    fun getPortalKeys(@PathVariable id: Int): ResponseEntity<KeyPerUserList> {
        val portalList = dbService.getPortalKeys(id)
        if (portalList != null) {
            return ResponseEntity.ok(KeyPerUserList(portalList.map {
                KeyPerUserItem(
                    it.key_id,
                    UserDTO(
                        it.user.user_id,
                        it.user.username,
                        it.user.email,
                        it.user.validated
                    ),
                    it.portal,
                    it.quantity
                )
            }))
        }
        return ResponseEntity.notFound().build()
    }

    @GetMapping("/portal/keys/user/{username}")
    fun getPortalKeysByUser(@PathVariable username: String): ResponseEntity<KeyPerUserList> {
        val portalList = dbService.getPortalKeysByUser(username)
        if (portalList != null) {
            return ResponseEntity.ok(KeyPerUserList(portalList.map {
                KeyPerUserItem(
                    it.key_id,
                    UserDTO(
                        it.user.user_id,
                        it.user.username,
                        it.user.email,
                        it.user.validated
                    ),
                    it.portal,
                    it.quantity
                )
            }))
        }
        return ResponseEntity.notFound().build()
    }

    @PostMapping("/portal/keys")
    fun addPortalKeys(@RequestBody keyUserPortal: KeyUserPortalDTO): ResponseEntity<KeyPerUserItem> {
        val response = dbService.addPortalKeys(keyUserPortal)
        if (response != null) {
            return ResponseEntity.ok(
                KeyPerUserItem(
                    response.key_id,
                    UserDTO(
                        response.user.user_id,
                        response.user.username,
                        response.user.email,
                        response.user.validated
                    ),
                    response.portal,
                    response.quantity
                )
            )
        }
        return ResponseEntity.notFound().build()
    }

    @PutMapping("portal/keys/{key_id}")
    fun editPortalKeys(@RequestBody keyUserPortal: KeyUserPortalDTO, @PathVariable key_id: Int): ResponseEntity<KeyPerUserItem> {
        val response = dbService.editPortalKeys(keyUserPortal, key_id)
        if (response != null) {
            return ResponseEntity.ok(
                KeyPerUserItem(
                    response.key_id,
                    UserDTO(
                        response.user.user_id,
                        response.user.username,
                        response.user.email,
                        response.user.validated
                    ),
                    response.portal,
                    response.quantity
                )
            )
        }
        return ResponseEntity.notFound().build()
    }

    @GetMapping("/route")
    fun getAllRoutes(): ResponseEntity<RouteListFlutterDTO> {
        return ResponseEntity.ok(
            RouteListFlutterDTO(
                dbService.getAllRoutes()
            )
        )
    }

    @GetMapping("/route/{username}")
    fun getAllRoutesOfUser(@PathVariable username: String): ResponseEntity<RouteListFlutterDTO> {
        val routes = dbService.getRoutesOfUser(username)
        if (routes.isNullOrEmpty()) {
            return ResponseEntity.notFound().build()
        }
        return ResponseEntity.ok(
            RouteListFlutterDTO(
                routes
            )
        )
    }

    @PostMapping("/route")
    fun saveRoute(@RequestBody routeDTO: RouteDTO): ResponseEntity<RouteDTO> {
        return ResponseEntity.ok(dbService.saveRoute(routeDTO))
    }

    @PostMapping("/route/stop")
    fun saveRouteStop(@RequestBody routeStopDTO: RouteStopDTO): ResponseEntity<RouteStopDTO> {
        return ResponseEntity.ok(dbService.saveRouteStop(routeStopDTO))
    }

    @PostMapping("/route/key")
    fun saveRouteKey(@RequestBody routeKeyDTO: RouteKeyDTO): ResponseEntity<RouteKeyDTO> {
        return ResponseEntity.ok(dbService.saveRouteKeys(
            routeKeyDTO))
    }
}