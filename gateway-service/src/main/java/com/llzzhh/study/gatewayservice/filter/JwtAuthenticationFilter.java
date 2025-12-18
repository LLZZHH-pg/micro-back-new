package com.llzzhh.study.gatewayservice.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.llzzhh.study.dto.JwtUserDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey secretKey;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        try {
            this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            logger.info("JWT secret key initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize JWT secret key", e);
            throw new RuntimeException("JWT configuration error", e);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        // 获取Token
        String token = getTokenFromRequest(request);

        if (!StringUtils.hasText(token)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            // 验证并解析Token
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();


            // 从claim中获取JwtUserDTO
            Object userClaim = claims.get("user");
            if (userClaim == null) {
                throw new MalformedJwtException("JWT token missing user claim");
            }
            JwtUserDTO jwtUser = objectMapper.convertValue(userClaim, JwtUserDTO.class);

            if (jwtUser != null) {
                logger.info("Authenticating user: " + jwtUser.getUid() + " with default role");

                // 使用JwtUserDTO作为principal
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(jwtUser, null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                request.setAttribute("jwtUser", jwtUser);

                logger.debug("Authentication set for user: " + jwtUser.getUid());
            }
        } catch (ExpiredJwtException e) {
            logger.error("JWT token expired", e);
            sendError(response, request, "Token已过期", HttpStatus.UNAUTHORIZED.value());
            return;
        } catch (MalformedJwtException | SecurityException e) {
            logger.error("Invalid JWT token", e);
            sendError(response, request, "无效的Token", HttpStatus.FORBIDDEN.value());
            return;
        } catch (Exception e) {
            logger.error("JWT token validation failed", e);
            sendError(response, request, "Token验证失败", HttpStatus.FORBIDDEN.value());
            return;
        }

        chain.doFilter(request, response);
    }
    private void sendError(HttpServletResponse response,HttpServletRequest request, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 返回更详细的错误信息
        String errorBody = String.format(
                "{\"code\":%d,\"msg\":\"%s\",\"path\":\"%s\"}",
                status,
                message,
                request.getRequestURI()
        );

        response.getWriter().write(errorBody);
        response.getWriter().flush();

        // 添加错误日志
        logger.error("Authentication error: " + message +
                " for path: " + request.getRequestURI() +
                " | Method: " + request.getMethod());
    }
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTH_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}