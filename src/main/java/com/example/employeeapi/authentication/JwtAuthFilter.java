package com.example.employeeapi.authentication;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Bloque l'accès à /employees sans jeton valide dans l'en-tête "Authorization: Bearer ...". */
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Pattern BEARER_PATTERN = Pattern.compile("^bearer\\s+(\\S+).*", Pattern.CASE_INSENSITIVE);

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !(path.equals("/employees") || path.startsWith("/employees/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        var matcher = BEARER_PATTERN.matcher(header == null ? "" : header.trim());
        if (!matcher.matches()) {
            unauthorized(response,
                    "Invalid Authorization header. Expected format is : 'Authorization: Bearer <token>'");
            return;
        }

        AuthUser user;
        try {
            user = jwtService.verifyToken(matcher.group(1));
        } catch (ExpiredJwtException e) {
            unauthorized(response, "Token expired");
            return;
        } catch (JwtException | IllegalArgumentException e) {
            unauthorized(response, "Invalid Token");
            return;
        }

        var authentication = new UsernamePasswordAuthenticationToken(user, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), Map.of("error", message));
    }
}
