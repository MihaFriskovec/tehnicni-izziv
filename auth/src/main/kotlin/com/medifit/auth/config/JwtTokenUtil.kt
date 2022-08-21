package com.medifit.auth.config

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

const val TOKEN_ISSUER = "medifit"


@Component
class JwtTokenUtil(@Value("\${jwt.secret}") private val secret: String) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))
    private val tokenParser = Jwts.parserBuilder().setSigningKey(key).build()

    fun createToken(userId: String, role: String): String {
        val expiration = Date.from(Instant.now().plusSeconds(3600L))

        val claims = hashMapOf<String, Any>()
        claims["role"] = role

        return try {
            Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(userId)
                .setIssuer(TOKEN_ISSUER)
                .addClaims(claims)
                .setIssuedAt(Date())
                .setExpiration(expiration)
                .signWith(key)
                .compact()
        } catch (e: Exception) {
            logger.error("Error creating JWT token, e=${e.message}")

            throw e
        }
    }

    fun getUserId(token: String): String? {
        val claims = tokenParser.parseClaimsJws(token).body

        return claims.subject
    }

    fun getRole(token: String): String? {
        val claims = tokenParser.parseClaimsJws(token).body

        return claims.get("role", String::class.java)
    }

    fun validateToken(token: String): Boolean {
        try {
            tokenParser.parseClaimsJws(token)

            return true
        } catch (e: SignatureException) {
            logger.warn("Invalid JWT signature - ${e.message}")
        } catch (e: MalformedJwtException) {
            logger.warn("Malformed JWT token - ${e.message}")
        } catch (e: ExpiredJwtException) {
            logger.warn("Expired JWT token - ${e.message}")
        } catch (e: UnsupportedJwtException) {
            logger.warn("Unsupported JWT token - ${e.message}")
        } catch (e: IllegalArgumentException) {
            logger.warn("JWT claims empty - ${e.message}")
        }

        return false
    }


}
