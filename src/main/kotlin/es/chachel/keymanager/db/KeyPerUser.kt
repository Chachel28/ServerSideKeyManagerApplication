package es.chachel.keymanager.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

@Entity
@Table(name = "key_per_user")
data class KeyPerUser(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var key_id: Int = 0,
        @ManyToOne
        @JoinColumn(name = "user_id", referencedColumnName = "user_id")
        var user: User = User(),
        @ManyToOne
        @JoinColumn(name = "portal_id", referencedColumnName = "portal_id")
        var portal: Portal = Portal(),
        var quantity: Int = 0
)

@Repository
interface KeyPerUserRepository: JpaRepository<KeyPerUser, Int>
