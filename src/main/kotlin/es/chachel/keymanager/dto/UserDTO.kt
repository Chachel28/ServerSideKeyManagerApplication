package es.chachel.keymanager.dto

data class UserDTO(
    var user_id: Int = 0,
    var username: String = "",
    var email: String = "",
    var validated: Boolean = false
)