package org.xue.gateway.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * JWT令牌处理服务（WebFlux版本）
 */
@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 从token中提取用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从token中提取用户ID
     */
    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    /**
     * 从token中提取过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 从token中提取指定声明
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从token中提取所有声明
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            System.out.println("JWT token已过期: " + e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            System.out.println("不支持的JWT token: " + e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            System.out.println("JWT token格式错误: " + e.getMessage());
            throw e;
        } catch (SecurityException e) {
            System.out.println("JWT token签名验证失败: " + e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            System.out.println("JWT token参数为空: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 检查token是否过期
     */
    private Boolean isTokenExpired(String token) {
        try {
            Date expirationDate = extractExpiration(token);
            return expirationDate.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * 验证token有效性
     */
    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            System.out.println("Token验证失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 从token中获取用户信息
     */
    public String getUsernameFromToken(String token) {
        try {
            return extractUsername(token);
        } catch (Exception e) {
            System.out.println("从token提取用户名失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 从token中获取用户ID
     */
    public String getUserIdFromToken(String token) {
        try {
            return extractUserId(token);
        } catch (Exception e) {
            System.out.println("从token提取用户ID失败: " + e.getMessage());
            return null;
        }
    }
} 