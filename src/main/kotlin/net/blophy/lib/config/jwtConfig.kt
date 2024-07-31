package net.blophy.lib.config

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtConfig {
    private const val SECRET_KEY = "your-secret-key"
    private const val ISSUER = "your-issuer"
    private const val VALIDITY_IN_MS = 36_000_00 * 10 // 10 hours

    private val algorithm = Algorithm.HMAC512(SECRET_KEY)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(ISSUER)
        .build()

    fun generateToken(username: String): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(ISSUER)
        .withClaim("username", username)
        .withExpiresAt(getExpiration())
        .sign(algorithm)

    private fun getExpiration() = Date(System.currentTimeMillis() + VALIDITY_IN_MS)
}