package meciblock.main.controller;

import meciblock.main.Service.MediblockService;
import meciblock.main.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@Controller
@RequestMapping("/patient")
public class PatientController {
    private final MediblockService service;
    public PatientController(MediblockService service) { this.service = service; }

    private User currentPatient(Authentication authentication) {
        return service.userByUsername(authentication.getName()).orElseThrow(() -> new IllegalStateException("Patient account not found."));
    }
    private java.util.List<meciblock.main.model.Prescription> records(Authentication authentication) {
        User u = currentPatient(authentication);
        if (u.getPatientId() == null || u.getPatientId().isBlank()) return java.util.List.of();
        return service.prescriptionsForPatient(u.getPatientId());
    }
    @GetMapping("/dashboard") public String dashboard(Authentication authentication, Model model){ var p=records(authentication); model.addAttribute("prescriptions",p); model.addAttribute("activeCount",p.stream().filter(x->"ACTIVE".equalsIgnoreCase(x.getStatus())).count()); model.addAttribute("medicineCount",p.size()); model.addAttribute("patient", currentPatient(authentication)); return "patient/dashboard"; }
    @GetMapping("/prescriptions") public String prescriptions(Authentication authentication, Model model){ model.addAttribute("prescriptions", records(authentication)); return "patient/prescriptions"; }
    @GetMapping("/medicine-history") public String history(Authentication authentication, Model model){ model.addAttribute("prescriptions", records(authentication)); return "patient/medicine-history"; }

    @GetMapping("/profile") public String profile(Authentication authentication, Model model){ model.addAttribute("patient", currentPatient(authentication)); return "patient/profile"; }
    @PostMapping("/profile/email") public String saveEmail(Authentication authentication, @RequestParam String email, Model model){
        User u=currentPatient(authentication);
        u.setEmail(email == null ? null : email.trim());
        u.setEmailVerified(false);
        u.setEmailVerificationHash(null);
        u.setEmailVerificationExpiresAt(null);
        service.saveUser(u);
        String result=service.sendPatientEmailVerification(u.getPatientId());
        model.addAttribute("patient", u); model.addAttribute("profileResult", result);
        return "patient/profile";
    }
    @PostMapping("/profile/verify-email") public String verifyEmail(Authentication authentication, @RequestParam String otp, Model model){
        User u=currentPatient(authentication);
        boolean ok=service.verifyPatientEmailOtp(u, otp);
        model.addAttribute("patient", u); model.addAttribute("profileResult", ok ? "EMAIL_VERIFIED" : "INVALID_OR_EXPIRED_OTP");
        return "patient/profile";
    }
}
