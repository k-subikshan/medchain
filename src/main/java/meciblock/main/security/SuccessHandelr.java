package meciblock.main.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


   @Component
public class SuccessHandelr implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        System.out.println(
            "Authentication successful for user: "
            + authentication.getName()
        );

        for (GrantedAuthority authority :
                authentication.getAuthorities()) {

            System.out.println(
                "Authority: "
                + authority.getAuthority()
            );

            if (authority.getAuthority().equals("ROLE_admin")) {
                response.sendRedirect("/admin/dashboard");
                return;
            }
            else if (authority.getAuthority().equals("ROLE_patient")) {
                response.sendRedirect("/patient/dashboard");
                return;
            }
            else if(authority.getAuthority().equals("ROLE_doctor")) {
                response.sendRedirect("/doctor/dashboard");
                return;
            }
            else if(authority.getAuthority().equals("ROLE_pharmacist")) {
                response.sendRedirect("/pharmacy/dashboard");
                return;
            }
            else if(authority.getAuthority().equals("ROLE_manfacturer")) {
                response.sendRedirect("/manufacturer/dashboard");
                return;
            }
        }

        response.sendRedirect("/login?error=true");
    }
}
    

