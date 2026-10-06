package meciblock.main.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Prescription {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String prescriptionId;
    private String patientId;
    private String patientName;
    @Transient
    private String patientEmailInput;
    private String medicinePackageId;
    private String medicineName;
    private Integer quantity;
    private Integer dispensedQuantity = 0;
    private String dosage;
    private String prescriptionDate;
    private String validUntil;
    @Column(length = 2000)
    private String instructions;
    private String doctorUsername;
    private String validatorUsername;
    /** Doctor selected by the issuing doctor to review this prescription. */
    private String assignedValidatorUsername;
    private String validatedAt;
    private Boolean requiresValidation = false;
    private Boolean consultationRequested = false;
    /** Patient OTP is required before the pharmacy can complete dispensing. */
    private Boolean otpEnabled = false;
    @Column(length = 128)
    private String otpHash;
    private String otpExpiresAt;
    private Integer otpPendingAmount = 0;
    private Boolean otpUsed = false;
    @Column(length = 1000)
    private String validationReason;
    private String status;
    private String recordHash;
}
