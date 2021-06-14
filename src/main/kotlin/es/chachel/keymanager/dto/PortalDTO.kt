package es.chachel.keymanager.dto

data class PortalDTO(
    val data: List<PortalData>,
    val links: PortalLinks,
    val meta: PortalMeta
)

data class PortalData(
    val codename: Any?,
    val created_at: String,
    val description: String,
    val guid: String,
    val id: Int,
    val keys_farmed: Int,
    val lat: Double,
    val layers: List<Int>,
    val lng: Double,
    val name: String,
    val updated_at: String
)

data class PortalLinks(
    val first: String,
    val last: String,
    val next: String?,
    val prev: String?
)

data class PortalMeta(
    val current_page: Int,
    val from: Int,
    val last_page: Int,
    val path: String,
    val per_page: Int,
    val to: Int,
    val total: Int
)

data class PortalListDTO(
    val data: List<PortalListItem>,
    val links: PortalLinks,
    val meta: PortalMeta
)

data class PortalListItem(
    var portal_id: Int = 0,
    var portal_name: String = "",
    var latitude: Float = 0f,
    var longitude: Float = 0f,
    var city: String = "",
    var province: String = "",
    var guid: String = "",
    var keys:Int = 0
)