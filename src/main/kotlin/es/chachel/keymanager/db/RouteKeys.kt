package es.chachel.keymanager.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

@Entity
@Table(name = "route_keys")
data class RouteKeys(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var route_key_id: Int = 0,
    @ManyToOne
    @JoinColumn(name = "route_id", referencedColumnName = "route_id")
    var route: Route = Route(),
    @ManyToOne
    @JoinColumn(name = "portal_id", referencedColumnName = "portal_id")
    var portal: Portal = Portal(),
    var quantity: Int = 0
)

@Repository
interface RouteKeysRepository : JpaRepository<RouteKeys, Int>{
    fun findByRoute(route: Route) : List<RouteKeys>?
}

