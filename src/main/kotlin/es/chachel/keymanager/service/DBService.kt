package es.chachel.keymanager.service

import es.chachel.keymanager.db.KeyPerUser
import es.chachel.keymanager.db.KeyPerUserRepository
import es.chachel.keymanager.db.PortalRepository
import es.chachel.keymanager.db.UserRepository
import es.chachel.keymanager.dto.AccessTokenResponseDTO
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class DBService(private val portalRepository: PortalRepository,
                private val userRepository: UserRepository,
                private val keyPerUserRepository: KeyPerUserRepository) {

    fun getAllKeys():List<KeyPerUser>{
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
}