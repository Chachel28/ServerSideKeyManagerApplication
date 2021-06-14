package es.chachel.keymanager.dto

import es.chachel.keymanager.db.Portal

data class KeyPerUserList(
    val data: List<KeyPerUserItem>
)

data class KeyPerUserItem(
    var key_id: Int = 0,
    var user: UserDTO = UserDTO(),
    var portal: Portal = Portal(),
    var quantity: Int = 0
)

data class KeyUserPortalDTO(
    var portal: Int = 0,
    var user: Int = 0,
    var quantity: Int = 0
)