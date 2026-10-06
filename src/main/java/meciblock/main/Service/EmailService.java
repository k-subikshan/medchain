package meciblock.main.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final String from;

    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username:}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendEmailVerificationOtp(String to, String patientId, String otp) {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("The patient email address is missing.");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        if (from != null && !from.isBlank()) message.setFrom(from);
        message.setTo(to);
        message.setSubject("MedChain patient email verification");
        message.setText(
                "MedChain Patient Email Verification\n\n" +
                "Patient ID: " + patientId + "\n\n" +
                "Your verification code is: " + otp + "\n\n" +
                "This code expires in 5 minutes. Use it to verify the patient's email before a prescription is created.");
        mailSender.send(message);
    }

    public void sendDispenseOtp(String to, String prescriptionId, String medicineName, String otp, int amount) {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("The patient does not have an email address registered.");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        if (from != null && !from.isBlank()) message.setFrom(from);
        message.setTo(to);
        message.setSubject("MedChain dispensing OTP - " + prescriptionId);
        message.setText(
                "MedChain Medicine Dispensing Verification\n\n" +
                "Prescription: " + prescriptionId + "\n" +
                "Medicine: " + medicineName + "\n" +
                "Requested quantity: " + amount + "\n\n" +
                "Your one-time verification code is: " + otp + "\n\n" +
                "This OTP expires in 5 minutes and can be used only once. " +
                "Give this code to the pharmacist only if you authorized this dispensing request.");
        mailSender.send(message);
    }
}
