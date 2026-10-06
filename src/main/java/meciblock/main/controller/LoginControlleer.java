package meciblock.main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@Controller
public class LoginControlleer {
    @GetMapping("/login")
    public String getMethodName() {
        return "login";
    }
    @GetMapping("/logout")
    public String getMethodName(@RequestParam String param) {
        return "redirect:/login";
    }
    
    
    
}
