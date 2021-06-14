package es.chachel.keymanager.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

@Entity
@Table(name = "route")
data class Route(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var route_id: Int = 0,
    var name: String = "",
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    var user: User = User()
)

@Repository
interface RouteRepository : JpaRepository<Route, Int>{
    fun findByUser(user: User) : List<Route>?
}

