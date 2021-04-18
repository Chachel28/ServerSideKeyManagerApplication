package es.chachel.keymanager.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "user")
data class User(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var user_id: Int = 0,
        var username: String = "",
        var password: String = "",
        var email: String = "",
        var token: String = "",
        var refresh_token: String = "",
        var expireDate: Date = Date()
)

@Repository
interface UserRepository : JpaRepository<User, Int> {
    @Transactional
    @Modifying
    @Query("update User u set u.token=:accessToken, u.refresh_token=:refreshToken, u.expireDate=:expireDate where u.user_id=:userId")
    fun updateAccessAndRefreshToken(userId: Int, accessToken: String?, refreshToken: String?, expireDate: Date?)

    fun findByUsername(username: String):User
}