package es.chachel.keymanager.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

@Entity
@Table(name = "route_stop")
data class RouteStop(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var stop_id: Int = 0,
    @ManyToOne
    @JoinColumn(name = "route_id", referencedColumnName = "route_id")
    var route: Route = Route(),
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    var user: User = User(),
    var order: Int = 0
)

@Repository
interface RouteStopRepository : JpaRepository<RouteStop, Int>{
    fun findByRoute(route: Route) : List<RouteStop>?

    fun findByUser(user: User) : List<RouteStop>?
}

