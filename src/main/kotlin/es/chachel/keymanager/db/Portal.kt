package es.chachel.keymanager.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

@Entity
@Table(name = "portal")
data class Portal(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var portal_id: Int = 0,
        var portal_name: String = "",
        var latitude: Float = 0f,
        var longitude: Float = 0f,
        var city: String = "",
        var province: String = "",
        var guid: String = ""
)

@Repository
interface PortalRepository:JpaRepository<Portal, Int> {
        fun findByGuid(guid: String): Portal?
}
