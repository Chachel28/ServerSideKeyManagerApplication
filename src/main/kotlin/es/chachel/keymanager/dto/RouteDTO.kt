package es.chachel.keymanager.dto

import es.chachel.keymanager.db.Portal

data class RouteDTOFlutter(
    var route: RouteDTO,
    var route_stops: List<RouteStopDTO>?,
    var route_keys: List<RouteKeyDTO>?
)

data class RouteListFlutterDTO(
    var routes: List<RouteDTOFlutter>
)

data class RouteDTO(
    var route_id: Int = 0,
    var name: String = "",
    var user: UserDTO = UserDTO()
)

data class RouteStopDTO(
    var stop_id: Int = 0,
    var route: RouteDTO = RouteDTO(),
    var user: UserDTO = UserDTO(),
    var order: Int = 0
)

data class RouteKeyDTO(
    var route_key_id: Int = 0,
    var route: RouteDTO = RouteDTO(),
    var portal: Portal = Portal(),
    var quantity: Int = 0
)