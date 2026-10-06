package meciblock.main.Service;

import meciblock.main.Repository.*;
import meciblock.main.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.security.SecureRandom;

@Service
public class MediblockService {
    private final MedicineRepository medicines;
    private final BatchRepository batches;
    private final PrescriptionRepository prescriptions;
    private final UserRepository users;
    private final BlockchainService blockchain;
    private final EmailService emailService;

    public MediblockService(MedicineRepository medicines, BatchRepository batches,
                            PrescriptionRepository prescriptions, UserRepository users,
                            BlockchainService blockchain, EmailService emailService) {
        this.medicines = medicines; this.batches = batches; this.prescriptions = prescriptions;
        this.users = users; this.blockchain = blockchain; this.emailService = emailService;
    }

    @Transactional
    public Medicine registerMedicine(Medicine m) {
        if (m.getMedicineId() == null || m.getMedicineId().isBlank()) m.setMedicineId("MED-" + UUID.randomUUID().toString().substring(0,8).toUpperCase());
        String data = String.join("|", n(m.getMedicineId()), n(m.getManufacturerId()), n(m.getName()), n(m.getDosageForm()), n(m.getManufacturingDate()), n(m.getExpiryDate()), n(m.getDetails()));
        m.setRecordHash(blockchain.sha256(data));
        Medicine saved = medicines.save(m); blockchain.addBlock("MEDICINE", saved.getMedicineId(), data); return saved;
    }

    @Transactional
    public Batch createBatch(Batch b) {
        if (b.getBatchId() == null || b.getBatchId().isBlank()) b.setBatchId("B-" + UUID.randomUUID().toString().substring(0,8).toUpperCase());
        String data = String.join("|", n(b.getBatchId()), n(b.getMedicineId()), String.valueOf(b.getPackageCount()), n(b.getManufacturingDate()));
        b.setRecordHash(blockchain.sha256(data));
        Batch saved = batches.save(b); blockchain.addBlock("BATCH", saved.getBatchId(), data); return saved;
    }

    @Transactional
    public Prescription createPrescription(Prescription p, String doctor, String emailVerificationOtp) {
        if (p.getPrescriptionId() == null || p.getPrescriptionId().isBlank()) p.setPrescriptionId("RX-" + UUID.randomUUID().toString().substring(0,8).toUpperCase());
        p.setDoctorUsername(doctor);
        if (p.getDispensedQuantity() == null) p.setDispensedQuantity(0);
        if (p.getOtpEnabled() == null) p.setOtpEnabled(false);
        if (p.getOtpUsed() == null) p.setOtpUsed(false);
        if (p.getOtpPendingAmount() == null) p.setOtpPendingAmount(0);

        User patient = users.findByPatientId(p.getPatientId()).orElseThrow(() -> new IllegalArgumentException("Patient ID not found."));
        String registeredEmail = patient.getEmail() == null ? "" : patient.getEmail().trim();
        String submittedEmail = p.getPatientEmailInput() == null ? "" : p.getPatientEmailInput().trim();
        if (registeredEmail.isBlank()) {
            throw new IllegalArgumentException("Patient email is missing. Ask the patient to add an email in Patient Profile and verify it first.");
        }
        if (submittedEmail.isBlank() || !registeredEmail.equalsIgnoreCase(submittedEmail)) {
            throw new IllegalArgumentException("Patient email does not match the registered email for this patient ID.");
        }
        if (!Boolean.TRUE.equals(patient.getEmailVerified())) {
            if (emailVerificationOtp == null || emailVerificationOtp.isBlank()) {
                sendPatientEmailVerification(patient);
                throw new IllegalArgumentException("EMAIL_OTP_SENT: A verification OTP was sent to the patient's registered email. Enter the OTP and submit again.");
            }
            if (!verifyPatientEmailOtp(patient, emailVerificationOtp)) {
                throw new IllegalArgumentException("Invalid or expired patient email verification OTP.");
            }
        }

        String history = patient.getMedicalHistory() == null
        ? ""
        : patient.getMedicalHistory();

boolean cardiacHistory = containsCardiacHistory(history);
boolean consultationRequested =
        Boolean.TRUE.equals(p.getConsultationRequested());

p.setRequiresValidation(consultationRequested);

if (consultationRequested &&
        (p.getAssignedValidatorUsername() == null ||
         p.getAssignedValidatorUsername().isBlank())) {

    throw new IllegalArgumentException(
            "Select a doctor to validate this prescription."
    );
}

if (consultationRequested) {

    User assigned = users.findByUsername(
            p.getAssignedValidatorUsername()
    ).orElse(null);

    if (assigned == null ||
            assigned.getRoles() == null ||
            !assigned.getRoles().toLowerCase().contains("doctor")) {

        throw new IllegalArgumentException(
                "Select a valid doctor as the validating doctor."
        );
    }

    if (doctor.equalsIgnoreCase(assigned.getUsername())) {

        throw new IllegalArgumentException(
                "The validating doctor must be different from the doctor who created the prescription."
        );
    }
}

if (consultationRequested) {
    p.setStatus("PENDING_VALIDATION");
}
        consultationRequested = Boolean.TRUE.equals(p.getConsultationRequested());
        p.setRequiresValidation(consultationRequested);
        if (consultationRequested && (p.getAssignedValidatorUsername() == null || p.getAssignedValidatorUsername().isBlank())) {
            throw new IllegalArgumentException("Select a doctor to validate this prescription.");
        }
        if (consultationRequested) {
            User assigned = users.findByUsername(p.getAssignedValidatorUsername());
            if (assigned == null || assigned.getRoles() == null || !assigned.getRoles().toLowerCase().contains("doctor")) {
                throw new IllegalArgumentException("Select a valid doctor as the validating doctor.");
            }
            if (doctor.equalsIgnoreCase(assigned.getUsername())) {
                throw new IllegalArgumentException("The validating doctor must be different from the doctor creating the prescription.");
            }
        }
        if (consultationRequested) {
            p.setStatus("PENDING_VALIDATION");
            p.setValidationReason(cardiacHistory
                    ? "Doctor requested second-doctor/specialist consultation because the patient has a cardiac history."
                    : "Doctor requested a second-doctor consultation before dispensing.");
        } else {
            p.setStatus("ACTIVE");
            p.setValidationReason(null);
        }

        String data = prescriptionData(p);
        p.setRecordHash(blockchain.sha256(data));
        Prescription saved = prescriptions.save(p);
        blockchain.addBlock("PRESCRIPTION_CREATED", saved.getPrescriptionId(), data);
        return saved;
    }

    @Transactional
    public String validatePrescription(String id, String validator) {
        if (validator == null || validator.isBlank()) return "UNAUTHENTICATED";
        return prescriptions.findByPrescriptionId(id).map(p -> {
            if (!Boolean.TRUE.equals(p.getRequiresValidation()) || !"PENDING_VALIDATION".equals(p.getStatus())) return "NOT_REQUIRED";
            String issuer = p.getDoctorUsername() == null ? "" : p.getDoctorUsername().trim();
            String assigned = p.getAssignedValidatorUsername() == null ? "" : p.getAssignedValidatorUsername().trim();
            String current = validator.trim();
            if (current.equalsIgnoreCase(issuer)) return "SAME_DOCTOR";
            if (assigned.isBlank()) return "NOT_ASSIGNED";
            if (!current.equalsIgnoreCase(assigned)) return "NOT_ASSIGNED";
            User validatingDoctor = users.findByUsername(current);
            if (validatingDoctor == null || validatingDoctor.getRoles() == null || !validatingDoctor.getRoles().toLowerCase().contains("doctor")) return "INVALID_VALIDATOR";
            p.setValidatorUsername(validatingDoctor.getUsername());
            p.setValidatedAt(LocalDateTime.now().toString());
            p.setStatus("ACTIVE");
            p.setRecordHash(blockchain.sha256(prescriptionData(p)));
            prescriptions.save(p);
            blockchain.addBlock("PRESCRIPTION_VALIDATED", p.getPrescriptionId(), "validator=" + validatingDoctor.getUsername() + "|hash=" + p.getRecordHash());
            return "VALIDATED";
        }).orElse("NOT_FOUND");
    }

    public String verifyMedicine(String packageOrBatchId) {
        return batches.findByBatchId(packageOrBatchId).map(b -> {
            String data = String.join("|", n(b.getBatchId()), n(b.getMedicineId()), String.valueOf(b.getPackageCount()), n(b.getManufacturingDate()));
            return blockchain.sha256(data).equals(b.getRecordHash()) ? "VALID" : "TAMPERED";
        }).orElse("NOT_FOUND");
    }

    @Transactional
    public String verifyPrescription(String id) {
        return prescriptions.findByPrescriptionId(id).map(p -> {
            String currentHash = blockchain.sha256(prescriptionData(p));
            String legacyHash = blockchain.sha256(legacyPrescriptionData(p));
            String integrity = (currentHash.equals(p.getRecordHash()) || legacyHash.equals(p.getRecordHash())) ? "OK" : "TAMPERED";
            if (!"OK".equals(integrity)) return "TAMPERED";
            if (!"ACTIVE".equals(p.getStatus())) return p.getStatus();
            int remaining = Math.max(0, nInt(p.getQuantity()) - nInt(p.getDispensedQuantity()));
            return remaining > 0 ? "VALID" : "DOSE_EXHAUSTED";
        }).orElse("NOT_FOUND");
    }

    @Transactional
    public String dispense(String id, int amount, String pharmacist) {
        if (amount <= 0) return "INVALID_DOSE";
        return prescriptions.findByPrescriptionId(id).map(p -> {
            if (!"ACTIVE".equals(p.getStatus())) return p.getStatus();
            int total = nInt(p.getQuantity());
            int used = nInt(p.getDispensedQuantity());
            int remaining = total - used;
            if (amount > remaining) return "DOSE_EXCEEDED";

            if (Boolean.TRUE.equals(p.getOtpEnabled())) {
                try {
                    String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));
                    p.setOtpHash(blockchain.sha256(otp));
                    p.setOtpExpiresAt(LocalDateTime.now().plusMinutes(5).toString());
                    p.setOtpPendingAmount(amount);
                    p.setOtpUsed(false);
                    prescriptions.save(p);

                    User patient = users.findByPatientId(p.getPatientId()).orElse(null);
                    if (patient == null || patient.getEmail() == null || patient.getEmail().isBlank()) {
                        p.setOtpHash(null); p.setOtpExpiresAt(null); p.setOtpPendingAmount(0);
                        prescriptions.save(p);
                        return "OTP_EMAIL_MISSING";
                    }
                    emailService.sendDispenseOtp(patient.getEmail(), p.getPrescriptionId(), p.getMedicineName(), otp, amount);
                    blockchain.addBlock("OTP_DISPENSE_REQUESTED", p.getPrescriptionId(),
                            "pharmacist=" + n(pharmacist) + "|quantity=" + amount + "|patient=" + n(p.getPatientId()));
                    return "OTP_SENT";
                } catch (Exception ex) {
                    p.setOtpHash(null); p.setOtpExpiresAt(null); p.setOtpPendingAmount(0);
                    prescriptions.save(p);
                    return "OTP_SEND_FAILED: " + ex.getMessage();
                }
            }
            return completeDispense(p, amount, pharmacist);
        }).orElse("NOT_FOUND");
    }

    @Transactional
    public String verifyDispenseOtp(String id, String otp, String pharmacist) {
        if (otp == null || !otp.matches("\\d{6}")) return "INVALID_OTP";
        return prescriptions.findByPrescriptionId(id).map(p -> {
            if (!"ACTIVE".equals(p.getStatus())) return p.getStatus();
            int amount = nInt(p.getOtpPendingAmount());
            if (amount <= 0 || p.getOtpHash() == null) return "NO_PENDING_OTP";
            if (p.getOtpExpiresAt() == null) return "OTP_EXPIRED";
            try {
                if (LocalDateTime.parse(p.getOtpExpiresAt()).isBefore(LocalDateTime.now())) {
                    clearOtp(p);
                    prescriptions.save(p);
                    return "OTP_EXPIRED";
                }
            } catch (Exception e) {
                clearOtp(p); prescriptions.save(p); return "OTP_EXPIRED";
            }
            if (!blockchain.sha256(otp).equals(p.getOtpHash())) return "INVALID_OTP";
            String result = completeDispense(p, amount, pharmacist);
            clearOtp(p);
            p.setOtpUsed(true);
            prescriptions.save(p);
            blockchain.addBlock("OTP_DISPENSE_VERIFIED", p.getPrescriptionId(),
                    "pharmacist=" + n(pharmacist) + "|quantity=" + amount + "|result=" + result);
            return result;
        }).orElse("NOT_FOUND");
    }

    private String completeDispense(Prescription p, int amount, String pharmacist) {
        int total = nInt(p.getQuantity());
        int used = nInt(p.getDispensedQuantity());
        int remaining = total - used;
        if (amount > remaining) return "DOSE_EXCEEDED";
        p.setDispensedQuantity(used + amount);
        p.setRecordHash(blockchain.sha256(prescriptionData(p)));
        prescriptions.save(p);
        blockchain.addBlock("MEDICINE_DISPENSED", p.getPrescriptionId(),
                "quantity=" + amount + "|pharmacist=" + n(pharmacist) + "|remaining=" + (remaining - amount));
        return (remaining - amount) == 0 ? "DISPENSED_AND_EXHAUSTED" : "DISPENSED";
    }

    private void clearOtp(Prescription p) {
        p.setOtpHash(null);
        p.setOtpExpiresAt(null);
        p.setOtpPendingAmount(0);
    }

    @Transactional
    public String sendPatientEmailVerification(String patientId) {
        User patient = users.findByPatientId(patientId).orElse(null);
        if (patient == null) return "PATIENT_NOT_FOUND";
        if (patient.getEmail() == null || patient.getEmail().isBlank()) return "EMAIL_MISSING";
        sendPatientEmailVerification(patient);
        return "OTP_SENT";
    }

    private void sendPatientEmailVerification(User patient) {
        String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        patient.setEmailVerificationHash(blockchain.sha256(otp));
        patient.setEmailVerificationExpiresAt(LocalDateTime.now().plusMinutes(5).toString());
        patient.setEmailVerified(false);
        users.save(patient);
        emailService.sendEmailVerificationOtp(patient.getEmail(), patient.getPatientId(), otp);
    }

    @Transactional
    public boolean verifyPatientEmailOtp(User patient, String otp) {
        if (otp == null || !otp.matches("\\d{6}") || patient.getEmailVerificationHash() == null || patient.getEmailVerificationExpiresAt() == null) return false;
        try {
            if (LocalDateTime.parse(patient.getEmailVerificationExpiresAt()).isBefore(LocalDateTime.now())) return false;
        } catch (Exception e) { return false; }
        if (!blockchain.sha256(otp).equals(patient.getEmailVerificationHash())) return false;
        patient.setEmailVerified(true);
        patient.setEmailVerificationHash(null);
        patient.setEmailVerificationExpiresAt(null);
        users.save(patient);
        blockchain.addBlock("PATIENT_EMAIL_VERIFIED", patient.getPatientId(), "email=" + n(patient.getEmail()));
        return true;
    }

    public boolean patientNeedsValidation(String patientId) {
        return users.findByPatientId(patientId).map(u -> containsCardiacHistory(u.getMedicalHistory())).orElse(false);
    }
    public Optional<User> patient(String patientId) { return users.findByPatientId(patientId); }
    public Optional<User> userByUsername(String username) { return Optional.ofNullable(users.findByUsername(username)); }
    @Transactional public User saveUser(User user) { return users.save(user); }
    public List<Prescription> prescriptionsForPatient(String patientId) { return prescriptions.findAll().stream().filter(p -> patientId != null && patientId.equals(p.getPatientId())).toList(); }
    public String patientHistory(String patientId) { return users.findByPatientId(patientId).map(User::getMedicalHistory).filter(h -> h != null && !h.isBlank()).orElse("No recorded medical history."); }
    public List<Medicine> medicines() { return medicines.findAll(); }
    public List<Batch> batches() { return batches.findAll(); }
    public List<Prescription> prescriptions() { return prescriptions.findAll(); }
    public List<Prescription> pendingValidations() { return prescriptions.findByStatusOrderByIdDesc("PENDING_VALIDATION"); }
    public List<Prescription> pendingValidationsForDoctor(String doctorUsername) {
        return prescriptions.findByStatusOrderByIdDesc("PENDING_VALIDATION").stream()
                .filter(p -> doctorUsername != null && doctorUsername.equals(p.getAssignedValidatorUsername()))
                .toList();
    }
    public List<User> doctors() {
        return users.findAll().stream()
                .filter(u -> u.getRoles() != null && u.getRoles().toLowerCase().contains("doctor"))
                .sorted(java.util.Comparator.comparing(User::getName, java.util.Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }
    public Optional<Medicine> medicine(String id) { return medicines.findByMedicineId(id); }
    public Optional<Batch> batch(String id) { return batches.findByBatchId(id); }
    public Optional<Prescription> prescription(String id) { return prescriptions.findByPrescriptionId(id); }
    public long activePrescriptionCount() { return prescriptions.findAll().stream().filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus())).count(); }
    public long uniquePatientCount() { return prescriptions.findAll().stream().map(Prescription::getPatientId).filter(Objects::nonNull).distinct().count(); }
    private boolean containsCardiacHistory(String h) { if (h == null) return false; String s=h.toLowerCase(); return s.contains("heart attack") || s.contains("myocardial infarction") || s.contains("mi history"); }
    private int nInt(Integer i) { return i == null ? 0 : i; }
    private String legacyPrescriptionData(Prescription p) {
        return String.join("|", n(p.getPrescriptionId()), n(p.getPatientId()), n(p.getPatientName()), n(p.getMedicinePackageId()), n(p.getMedicineName()), String.valueOf(p.getQuantity()), n(p.getDosage()), n(p.getPrescriptionDate()), n(p.getValidUntil()), n(p.getInstructions()), n(p.getDoctorUsername()));
    }
    private String prescriptionData(Prescription p) {
        return String.join("|", n(p.getPrescriptionId()), n(p.getPatientId()), n(p.getPatientName()), n(p.getMedicinePackageId()), n(p.getMedicineName()), String.valueOf(p.getQuantity()), String.valueOf(p.getDispensedQuantity()), n(p.getDosage()), n(p.getPrescriptionDate()), n(p.getValidUntil()), n(p.getInstructions()), n(p.getDoctorUsername()), n(p.getValidatorUsername()), n(p.getAssignedValidatorUsername()), n(p.getStatus()));
    }
    private String n(String s) { return s == null ? "" : s; }
}
