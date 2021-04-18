package es.chachel.keymanager.security.implementation

import es.chachel.keymanager.db.UserRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import java.util.*


@Component
class UserDetailServiceImpl(private val usersRepository: UserRepository) : UserDetailsService {

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val user:es.chachel.keymanager.db.User = usersRepository.findByUsername(username)
        return User(user.username, user.password, Collections.emptyList())
    }

}