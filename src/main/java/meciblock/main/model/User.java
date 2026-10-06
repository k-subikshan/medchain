package meciblock.main.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter @Setter
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String roles;
    private String email;
    private Boolean emailVerified = false;
    @Column(length = 128)
    private String emailVerificationHash;
    private String emailVerificationExpiresAt;
    private String phone;
    private String name;
    @Column(unique = true)
    private String patientId;
    @Column(length = 2000)
    private String medicalHistory;
    public User orElse(Object object) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'orElse'");
    }
}
