package meciblock.main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import meciblock.main.Service.MediblockService;
import meciblock.main.model.Prescription;

@Controller
@RequestMapping("/doctor")
public class DoctorController {
    private final MediblockService service;
    public DoctorController(MediblockService service) { this.service = service; }
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("prescriptions", service.prescriptions());
        model.addAttribute("prescriptionCount", service.prescriptions().size());
        model.addAttribute("activeCount", service.activePrescriptionCount());
        model.addAttribute("patientCount", service.uniquePatientCount());
        return "doctor/dashboard";
    }
    @GetMapping("/patients")
    public String patients(Model model) { model.addAttribute("prescriptions", service.prescriptions()); return "doctor/patients"; }
    @GetMapping("/create-prescription")
    public String getCreatePrescription(Authentication authentication, Model model) {
        model.addAttribute("doctors", service.doctors().stream()
                .filter(d -> !d.getUsername().equals(authentication.getName())).toList());
        return "doctor/create-prescription";
    }
    @GetMapping("/prescriptions") public String getPrescriptions(Model model) { model.addAttribute("prescriptions", service.prescriptions()); return "doctor/prescriptions"; }
    @GetMapping("/pending-validations") public String pendingValidations(Authentication authentication, Model model) {
        var pending = service.pendingValidationsForDoctor(authentication.getName());
        var histories = new java.util.HashMap<String, String>();
        pending.forEach(p -> histories.put(p.getPatientId(), service.patientHistory(p.getPatientId())));
        model.addAttribute("prescriptions", pending);
        model.addAttribute("histories", histories);
        model.addAttribute("currentDoctor", authentication.getName());
        return "doctor/pending-validations";
    }
    @PostMapping("/create-prescription")
    public String createPrescription(@ModelAttribute Prescription prescription, @RequestParam(required = false) String emailVerificationOtp, Authentication authentication, Model model) {
        try {
            Prescription saved = service.createPrescription(prescription, authentication.getName(), emailVerificationOtp);
            if ("PENDING_VALIDATION".equals(saved.getStatus())) {
                model.addAttribute("success", "Prescription " + saved.getPrescriptionId() + " created and assigned to " + saved.getAssignedValidatorUsername() + " for validation.");
            } else {
                model.addAttribute("success", "Prescription " + saved.getPrescriptionId() + " secured and written to blockchain.");
            }
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
        }
        model.addAttribute("doctors", service.doctors().stream()
                .filter(d -> !d.getUsername().equals(authentication.getName())).toList());
        return "doctor/create-prescription";
    }

    @PostMapping("/prescriptions/{id}/validate")
    public String validate(@PathVariable String id, Authentication authentication, RedirectAttributes redirectAttributes) {
        String currentDoctor = authentication.getName();
        String result = service.validatePrescription(id, currentDoctor);
        String message;
        switch (result) {
            case "VALIDATED" -> message = "Prescription " + id + " validated successfully. It is now ACTIVE.";
            case "SAME_DOCTOR" -> message = "Validation rejected: the issuing doctor cannot validate their own prescription.";
            case "NOT_ASSIGNED" -> message = "Validation rejected: this prescription is assigned to a different doctor.";
            case "NOT_REQUIRED" -> message = "This prescription does not require second-doctor validation.";
            case "INVALID_VALIDATOR" -> message = "Validation rejected: the logged-in account is not an authorized doctor.";
            case "NOT_FOUND" -> message = "Prescription not found.";
            default -> message = "Validation failed: " + result;
        }
        redirectAttributes.addFlashAttribute("validationResult", message);
        return "redirect:/doctor/pending-validations";
    }
}
