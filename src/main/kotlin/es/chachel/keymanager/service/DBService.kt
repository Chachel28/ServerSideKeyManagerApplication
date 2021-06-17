package es.chachel.keymanager.service

import es.chachel.keymanager.db.*
import es.chachel.keymanager.dto.*
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class DBService(
    private val portalRepository: PortalRepository,
    private val userRepository: UserRepository,
    private val keyPerUserRepository: KeyPerUserRepository,
    private val routeRepository: RouteRepository,
    private val routeStopRepository: RouteStopRepository,
    private val routeKeysRepository: RouteKeysRepository
) {

    fun getAllKeys(): List<KeyPerUser> {
        return keyPerUserRepository.findAll()
    }

    fun saveToken(body: AccessTokenResponseDTO?, username: String) {
        val expireDate = calculateExpireDate()
        val userId = userRepository.findByUsername(username)!!.user_id
        userRepository.updateAccessAndRefreshToken(userId, body?.access_token, body?.refresh_token, expireDate)
    }

    private fun calculateExpireDate(): Date? {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 60)
        return calendar.time
    }

    fun getExpireDate(username: String): Date? {
        val user = userRepository.findByUsername(username)!!
        return user.expireDate
    }

    fun getRefreshToken(username: String): String {
        return userRepository.findByUsername(username)!!.refresh_token
    }

    fun getAutToken(id: Int): String? {
        return userRepository.findById(id).get().token
    }

    fun saveUser(user: User): User {
        val repeatedUserName = userRepository.findByUsername(user.username)
        if(repeatedUserName != null){
            return User(user_id = -1)
        }
        val repeatedEmail = userRepository.findByEmail(user.email)
        if(repeatedEmail != null){
            return User(user_id = -2)
        }
        return userRepository.save(user)
    }

    fun getUser(username: String): UserDTO {
        val user = userRepository.findByUsername(username)!!
        return UserDTO(user.user_id, user.username, user.email, user.validated)
    }

    fun checkPortal(guid: String): Boolean {
        val portal = portalRepository.findByGuid(guid)
        if (portal != null) {
            return true
        }
        return false
    }

    fun getIDPortal(guid: String): Int {
        val portal = portalRepository.findByGuid(guid)
        if (portal != null) {
            return portal.portal_id
        }
        return 0
    }

    fun savePortal(portalData: PortalData): Portal {
        val portal = Portal(
            portal_name = portalData.name,
            latitude = portalData.lat.toFloat(),
            longitude = portalData.lng.toFloat(),
            guid = portalData.guid
        )
        return portalRepository.save(portal)
    }

    fun getPortalKeys(id: Int): List<KeyPerUser>? {
        val portal = portalRepository.findById(id)
        if (portal.isEmpty) {
            return null
        }
        val keys = keyPerUserRepository.findByPortal(portal.get())
        if (keys != null) {
            return keys
        }
        return null
    }

    fun addPortalKeys(keyUserPortal: KeyUserPortalDTO): KeyPerUser? {
        val user = userRepository.findById(keyUserPortal.user)
        val portal = portalRepository.findById(keyUserPortal.portal)
        if (!user.isEmpty && !portal.isEmpty) {
            val listOfKeys = keyPerUserRepository.findByPortal(portal.get())
            val exist = listOfKeys?.find { keyPerUser -> keyPerUser.user == user.get() }
            if (exist != null) {
                return editPortalKeys(keyUserPortal, exist.key_id)
            }
            return keyPerUserRepository.save(
                KeyPerUser(
                    user = user.get(),
                    portal = portal.get(),
                    quantity = keyUserPortal.quantity
                )
            )
        }
        return null;
    }

    fun editPortalKeys(keyUserPortal: KeyUserPortalDTO, key_id: Int): KeyPerUser? {
        val keyPerUser = keyPerUserRepository.findById(key_id)
        if (!keyPerUser.isEmpty) {
            val totalQuantity = keyPerUser.get().quantity + keyUserPortal.quantity
            if (totalQuantity > 0) {
                keyPerUserRepository.updateKeys(key_id, totalQuantity)
                return KeyPerUser(keyPerUser.get().key_id, keyPerUser.get().user, keyPerUser.get().portal, totalQuantity)
            } else {
                keyPerUserRepository.deleteById(key_id)
            }
        }
        return null;
    }

    fun getAllRoutes(): List<RouteDTOFlutter> {
        return routeRepository.findAll()
            .map { route ->
                RouteDTOFlutter(
                    route = RouteDTO(
                        route_id = route.route_id,
                        name = route.name,
                        user = UserDTO(
                            route.user.user_id,
                            route.user.username,
                            route.user.email,
                            route.user.validated
                        )
                    ),
                    route_keys = getKeysOfRoute(route.route_id),
                    route_stops = getStopsOfRoute(route.route_id)
                )
            }
    }

    fun getRoutesOfUser(username: String): List<RouteDTOFlutter>? {
        val user = userRepository.findByUsername(username)!!
        val listOfRoutes = routeRepository.findByUser(user)
        val listOfRouteStops = routeStopRepository.findByUser(user)
        return if (listOfRoutes.isNullOrEmpty()) {
            null
        } else {
            if (listOfRouteStops.isNullOrEmpty()) {
                listOfRoutes
                    .map { route ->
                        RouteDTOFlutter(
                            route = RouteDTO(
                                route_id = route.route_id,
                                name = route.name,
                                user = UserDTO(
                                    route.user.user_id,
                                    route.user.username,
                                    route.user.email,
                                    route.user.validated
                                )
                            ),
                            route_stops = getStopsOfRoute(route.route_id),
                            route_keys = getKeysOfRoute(route.route_id)
                        )
                    }
            } else {
                val routes = mutableListOf<Route>()
                listOfRouteStops
                    .forEach { routeStop ->
                        routes.add(routeRepository.findById(routeStop.route.route_id).get())
                    }
                listOfRoutes
                    .plus(routes)
                    .distinct()
                    .map { route ->
                        RouteDTOFlutter(
                            route = RouteDTO(
                                route_id = route.route_id,
                                name = route.name,
                                user = UserDTO(
                                    route.user.user_id,
                                    route.user.username,
                                    route.user.email,
                                    route.user.validated
                                )
                            ),
                            route_stops = getStopsOfRoute(route.route_id),
                            route_keys = getKeysOfRoute(route.route_id)
                        )
                    }
            }
        }
    }

    fun getStopsOfRoute(id: Int): List<RouteStopDTO>? {
        return routeStopRepository
            .findByRoute(routeRepository.findById(id).get())
            ?.map { routeStop ->
                RouteStopDTO(
                    stop_id = routeStop.stop_id,
                    route = RouteDTO(
                        route_id = routeStop.route.route_id,
                        name = routeStop.route.name,
                        user = UserDTO(
                            routeStop.route.user.user_id,
                            routeStop.route.user.username,
                            routeStop.route.user.email,
                            routeStop.route.user.validated
                        )
                    ),
                    order = routeStop.order,
                    user = UserDTO(
                        routeStop.user.user_id,
                        routeStop.user.username,
                        routeStop.user.email,
                        routeStop.user.validated
                    )
                )
            }
    }

    fun getKeysOfRoute(id: Int): List<RouteKeyDTO>? {
        return routeKeysRepository
            .findByRoute(routeRepository.findById(id).get())
            ?.map { routeKeyDTO ->
                RouteKeyDTO(
                    route_key_id = routeKeyDTO.route_key_id,
                    route = RouteDTO(
                        route_id = routeKeyDTO.route.route_id,
                        name = routeKeyDTO.route.name,
                        user = UserDTO(
                            routeKeyDTO.route.user.user_id,
                            routeKeyDTO.route.user.username,
                            routeKeyDTO.route.user.email,
                            routeKeyDTO.route.user.validated
                        )
                    ),
                    portal = routeKeyDTO.portal,
                    quantity = routeKeyDTO.quantity
                )
            }
    }

    fun saveRoute(routeDTO: RouteDTO): RouteDTO {
        val user = userRepository.findByUsername(routeDTO.user.username)!!
        val route = routeRepository.save(Route(name = routeDTO.name, user = user))
        return RouteDTO(
            route_id = route.route_id,
            name = route.name,
            user = UserDTO(
                route.user.user_id,
                route.user.username,
                route.user.email,
                route.user.validated
            )
        )
    }

    fun saveRouteStop(routeStopDTO: RouteStopDTO): RouteStopDTO? {
        val userStop = userRepository.findByUsername(routeStopDTO.user.username)!!
        val route = routeRepository.findById(routeStopDTO.route.route_id)
        if (route.isEmpty) {
            return null
        }
        val stops = routeStopRepository.findByRoute(route.get())
        if (stops.isNullOrEmpty()) {
            return null
        }
        val sortedStops = stops.sortedByDescending { routeStop -> routeStop.order }
        val stop = routeStopRepository.save(
            RouteStop(
                route = route.get(),
                user = userStop,
                order = sortedStops[0].order + 1
            )
        )
        return RouteStopDTO(
            stop_id = stop.stop_id,
            route = routeStopDTO.route,
            user = routeStopDTO.user,
            order = stop.order
        )
    }

    fun saveRouteKeys(routeKeyDTO: RouteKeyDTO): RouteKeyDTO? {
        val route = routeRepository.findById(routeKeyDTO.route.route_id)
        val portal = portalRepository.findById(routeKeyDTO.portal.portal_id)
        if (route.isEmpty || portal.isEmpty) {
            return null
        }
        val key = routeKeysRepository.save(
            RouteKeys(
                route = route.get(),
                portal = portal.get(),
                quantity = routeKeyDTO.quantity
            )
        )
        return RouteKeyDTO(
            route_key_id = key.route_key_id,
            route = routeKeyDTO.route,
            portal = key.portal,
            quantity = key.quantity
        )
    }

    fun getPortalKeysByUser(username: String): List<KeyPerUser>? {
        val user = userRepository.findByUsername(username)!!
        val keys = keyPerUserRepository.findByUser(user)
        if (keys != null) {
            return keys
        }
        return null

    }
}