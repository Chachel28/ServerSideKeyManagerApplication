package es.chachel.keymanager.dto

data class UserDTO(
    var user_id: Int = 0,
    var username: String = "",
    var email: String = "",
    var validated: Boolean = false,
    var avatar: String = ""
)

data class AgentInfoDTO(
    val agent_name: String,
    val avatar: String,
    val name: String,
    val trust_last_update: String,
    val trust_status: String,
    val trustmaster_id: String
)