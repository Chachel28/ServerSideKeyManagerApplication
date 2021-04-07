package es.chachel.keymanager.dto

data class OperationListDTO(
    val data:List<Data>,
    val links:Links,
    val meta:Meta
)

data class Data(
    val active: Int,
    val created_at: String,
    val id: Int,
    val joined_at: String,
    val owner: Owner,
    val role: String,
    val slug: String,
    val type: String,
    val updated_at: String
)

data class Links(
    val first: String,
    val last: String,
    val next: String?,
    val prev: String?
)

data class Meta(
    val current_page: Int,
    val from: Int,
    val last_page: Int,
    val path: String,
    val per_page: Int,
    val to: Int,
    val total: Int
)

data class Owner(
    val agent_name: String,
    val trustmaster_id: String
)