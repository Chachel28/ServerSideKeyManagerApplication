package es.chachel.keymanager.dto

data class AccessTokenResponseDTO(
    val access_token: String,
    val expires_in: Int,
    val refresh_token: String,
    val token_type: String
)

data class AccessTokenRequestDTO(
        val code:String,
        val user_id:Int
)