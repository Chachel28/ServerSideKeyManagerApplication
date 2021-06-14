package es.chachel.keymanager.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
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
interface KeyPerUserRepository: JpaRepository<KeyPerUser, Int>{
        fun findByPortal(portal: Portal): List<KeyPerUser>?

        fun findByUser(user: User): List<KeyPerUser>?

        @Transactional
        @Modifying
        @Query(nativeQuery = true, value = "update key_per_user k set k.quantity = :quantity where k.key_id = :key_id")
        fun updateKeys(@Param("key_id") keyId: Int,@Param("quantity") totalQuantity: Int)
}
