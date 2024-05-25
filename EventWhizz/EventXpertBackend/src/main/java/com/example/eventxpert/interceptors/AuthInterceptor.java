package com.example.eventxpert.interceptors;
import com.example.eventxpert.annotations.UnprotectedEndpoint;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            System.out.println("Pre Handle method is Calling");

            if (handler instanceof HandlerMethod) {
                UnprotectedEndpoint unprotectedEndpoint = ((HandlerMethod)handler).getMethod().getAnnotation((UnprotectedEndpoint.class));

                if (unprotectedEndpoint != null) {
                    return true;
                }
            }

            String authHeader = request.getHeader("Authorization");
            System.out.println("authHeader " + authHeader);

            if (authHeader == null) {
                request.setAttribute("jwtValidation", false);
                request.setAttribute("role", "");
                request.setAttribute("id", -1);
                return true;
            }

            String[] authHeaderParts = authHeader.split(" ");

            if (authHeaderParts.length != 2 || !authHeaderParts[0].equals("Bearer")) {
                request.setAttribute("jwtValidation", false);
                request.setAttribute("role", "");
                request.setAttribute("id", -1);
                return true;
            }

            Claims jwtBody = Jwts.parser()
                    .setSigningKey("secretkeyforeventxpertproject12345678901234567890!!!!!!!!!!!!!!!!!!!!!!!!!!")
                    .parseClaimsJws(authHeaderParts[1]).getBody();

            String role = jwtBody.getSubject();
            int id = Integer.parseInt(jwtBody.getIssuer());

            request.setAttribute("role", role);
            request.setAttribute("id", id);
            request.setAttribute("jwtValidation", true);
            return true;
        }
        catch (Exception e) {
            System.out.println(e);
            request.setAttribute("role", "");
            request.setAttribute("id", -1);
            request.setAttribute("jwtValidation", false);
            return true;
        }
    }
}

