package org.xue.app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtService {
    
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    // 注意：建议在生产环境中配置这个值到环境变量或配置文件
    @Value("${jwt.secret:zIORNkRJx1rE1I8RzyMrQ9ZPwn13RMLAHoXsKVgWB7WlqhUKGtvDe0Sd6rYV}")
    private String secretKey;

    @Value("${jwt.expiration:604800000}")  // 默认7天过期时间（毫秒）
    private long jwtExpiration;

    // 从token中提取用户名
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 从token中获取过期时间
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 从token中提取声明
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 从token提取所有声明信息
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 获取签名密钥
    private Key getSigningKey() {
        // 使用Keys工具类生成安全的密钥
        // 确保密钥长度足够用于HS256算法
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // 检查token是否过期
    private Boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        boolean isExpired = expiration.before(new Date());
        
        // 添加过期时间检查的详细日志
        if (isExpired) {
            log.debug("Token已过期 - 过期时间: {}, 当前时间: {}", 
                expiration, 
                new Date());
        } else {
            LocalDateTime expirationDateTime = expiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
            LocalDateTime now = LocalDateTime.now();
            log.debug("Token有效 - 过期时间: {}, 当前时间: {}, 剩余有效期: {} 天 {} 小时", 
                expirationDateTime,
                now,
                java.time.Duration.between(now, expirationDateTime).toDays(),
                java.time.Duration.between(now, expirationDateTime).toHours() % 24);
        }
        
        return isExpired;
    }

    // 生成token
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    // 生成包含额外信息的token
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return createToken(extraClaims, userDetails.getUsername());
    }

    // 创建token
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        log.info("创建新Token - 用户: {}, Claims: {}, 创建时间: {}, 过期时间: {}, 有效期: {} 天", 
            subject, 
            claims,
            now, 
            expiryDate,
            jwtExpiration / (1000 * 60 * 60 * 24));
            
        String token = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .addClaims(claims)  // 使用addClaims而不是setClaims，避免覆盖
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
                
        log.info("生成的Token: {}", token);
        return token;
    }

    // 验证token是否有效
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        boolean isValid = (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        
        log.debug("验证Token - 用户: {}, Token用户: {}, 是否有效: {}", 
            userDetails.getUsername(), username, isValid);
            
        return isValid;
    }
} 