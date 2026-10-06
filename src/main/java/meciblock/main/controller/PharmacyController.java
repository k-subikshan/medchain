package meciblock.main.controller;

import meciblock.main.Service.MediblockService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@Controller
@RequestMapping("/pharmacy")
public class PharmacyController {
    private final MediblockService service;
    public PharmacyController(MediblockService service) { this.service = service; }
    @GetMapping({"", "/dashboard"}) public String dashboard(Model model) { model.addAttribute("medicineCount", service.batches().size()); model.addAttribute("prescriptionCount", service.prescriptions().size()); model.addAttribute("flaggedCount", 0); return "pharmacy/dashboard"; }
    @GetMapping("/medicine-verification") public String medicine() { return "pharmacy/medicine-verification"; }
    @PostMapping("/medicine-verification") public String verifyMedicine(@RequestParam String packageId, Model model) { model.addAttribute("result", service.verifyMedicine(packageId)); service.batch(packageId).ifPresent(b -> { model.addAttribute("batch", b); service.medicine(b.getMedicineId()).ifPresent(m -> model.addAttribute("medicine", m)); }); return "pharmacy/medicine-verification"; }
    @GetMapping("/prescription-verification") public String prescription() { return "pharmacy/prescription-verification"; }
    @PostMapping("/prescription-verification") public String verifyPrescription(@RequestParam String prescriptionId, Model model) { model.addAttribute("result", service.verifyPrescription(prescriptionId)); service.prescription(prescriptionId).ifPresent(p -> model.addAttribute("prescription", p)); return "pharmacy/prescription-verification"; }
    @PostMapping("/prescription-dispense")
    public String dispense(@RequestParam String prescriptionId, @RequestParam int amount, Authentication authentication, Model model) {
        String result = service.dispense(prescriptionId, amount, authentication.getName());
        model.addAttribute("dispenseResult", result);
        model.addAttribute("result", service.verifyPrescription(prescriptionId));
        service.prescription(prescriptionId).ifPresent(p -> model.addAttribute("prescription", p));
        return "pharmacy/prescription-verification";
    }
    @PostMapping("/prescription-otp")
    public String verifyOtp(@RequestParam String prescriptionId, @RequestParam String otp, Authentication authentication, Model model) {
        String result = service.verifyDispenseOtp(prescriptionId, otp, authentication.getName());
        model.addAttribute("otpResult", result);
        model.addAttribute("result", service.verifyPrescription(prescriptionId));
        service.prescription(prescriptionId).ifPresent(p -> model.addAttribute("prescription", p));
        return "pharmacy/prescription-verification";
    }
    @GetMapping("/qr-verification") public String qr() { return "pharmacy/qr-verification"; }
}
