package com.talal.quizcore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    private final String[] protectedRoutes = {"/check/user", "/get/quiz", "/create/user","/update/user","/delete/user","/generate/quiz","/save/quiz","/create/attempt","/update/attempt","/set/completed","/get/quizzes","/user/quizzes","/delete/quiz","/get/user/attempt","/get/user/analytics"};

    @Override
    protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, jakarta.servlet.FilterChain filterChain) throws jakarta.servlet.ServletException, IOException {
        String path = request.getRequestURI();
        boolean isProtected = false;

        for(String route : protectedRoutes) {
            if(path.startsWith(route)) {
                isProtected = true;
            }
        }

        if(isProtected) {
            final String authorizationHeader = request.getHeader("Authorization");

            String userId = null;
            String jwt = null;

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
                userId = jwtUtil.extractUserId(jwt);
            }

            if (userId != null && jwtUtil.validateToken(jwt, userId)) {

            } else {
                System.out.println("Unauthorized");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return;
            }
        }

        filterChain.doFilter(request, response);


    }
}