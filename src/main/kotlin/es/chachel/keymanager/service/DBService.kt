package es.chachel.keymanager.service

import es.chachel.keymanager.db.*
import es.chachel.keymanager.dto.AccessTokenResponseDTO
import es.chachel.keymanager.dto.UserDTO
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class DBService(
    private val portalRepository: PortalRepository,
    private val userRepository: UserRepository,
    private val keyPerUserRepository: KeyPerUserRepository
) {

    fun getAllKeys(): List<KeyPerUser> {
        return keyPerUserRepository.findAll()
    }

    fun saveToken(body: AccessTokenResponseDTO?, userId: Int) {
        val expireDate = calculateExpireDate()
        userRepository.updateAccessAndRefreshToken(userId, body?.access_token, body?.refresh_token, expireDate)
    }

    private fun calculateExpireDate(): Date? {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 60)
        return calendar.time
    }

    fun getExpireDate(username: String): Date? {
        val user = userRepository.findByUsername(username)
        return user.expireDate
    }

    fun getRefreshToken(id: Int): String {
        return userRepository.findById(id).get().refresh_token
    }

    fun getAutToken(id: Int): String {
        return userRepository.findById(id).get().token
    }

    fun saveUser(user: User): User {
        return userRepository.save(user)
    }

    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }

    fun getUser(username: String): UserDTO {
        val user = userRepository.findByUsername(username)
        return UserDTO(user.user_id, user.username, user.email, user.validated)
    }
}