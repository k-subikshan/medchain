package meciblock.main.Service;

import meciblock.main.Repository.UserRepository;
import meciblock.main.model.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedUsers(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            // Create missing demo accounts without deleting existing application data.
            ensure(users, encoder, "admin", "admin123", "admin", "Admin User", null, null);
            ensure(users, encoder, "doctor", "doctor123", "doctor", "Dr. Demo", null, null);
            ensure(users, encoder, "doctor2", "doctor2123", "doctor", "Dr. Cardiology Reviewer", null, null);
            ensure(users, encoder, "pharmacy", "pharmacy123", "pharmacist", "Demo Pharmacy", null, null);
            ensure(users, encoder, "manufacturer", "manufacturer123", "manfacturer", "Demo Manufacturer", null, null);
            // Demo patient deliberately has a cardiac history so the approval workflow can be demonstrated.
            ensure(users, encoder, "patient", "patient123", "patient", "Arun Kumar", "P-1021", "Previous heart attack (myocardial infarction) - requires specialist prescription validation.");
        };
    }

    private void ensure(UserRepository users, PasswordEncoder encoder, String username, String password,
                        String role, String name, String patientId, String history) {
        User u = users.findByUsername(username);
        if (u == null) {
            u = new User();
            u.setUsername(username);
            u.setRoles(role);
            u.setName(name);
        }
        // Keep the demo accounts usable with the documented credentials.
        // This is intentionally limited to the built-in demo accounts.
        if ("doctor".equals(username) || "doctor2".equals(username) || "pharmacy".equals(username) || "patient".equals(username) || "admin".equals(username) || "manufacturer".equals(username)) {
            u.setPassword(encoder.encode(password));
            u.setRoles(role);
            u.setName(name);
        }
        if (patientId != null) u.setPatientId(patientId);
        if (u.getEmailVerified() == null) u.setEmailVerified(false);
        if (history != null) u.setMedicalHistory(history);
        if ("patient".equals(username) && (u.getEmail() == null || u.getEmail().isBlank())) {
            u.setEmail(System.getenv().getOrDefault("MEDIBLOCK_DEMO_PATIENT_EMAIL", ""));
        }
        users.save(u);
    }
}
