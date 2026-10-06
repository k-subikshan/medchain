package meciblock.main.controller;

import meciblock.main.Service.MediblockService;
import meciblock.main.Service.BlockchainService;
import meciblock.main.model.Batch;
import meciblock.main.model.Medicine;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/manufacturer")
public class ManufacturerController {
    private final MediblockService service; private final BlockchainService blockchain;
    public ManufacturerController(MediblockService service, BlockchainService blockchain) { this.service = service; this.blockchain = blockchain; }
    @GetMapping({"", "/dashboard"}) public String dashboard(Model model) {
        model.addAttribute("medicines", service.medicines()); model.addAttribute("batches", service.batches()); model.addAttribute("blockCount", blockchain.all().size()); model.addAttribute("packageCount", service.batches().stream().mapToInt(b -> b.getPackageCount() == null ? 0 : b.getPackageCount()).sum()); return "manufacturer/dashboard";
    }
    @GetMapping("/medicine-registration") public String medicineForm() { return "manufacturer/medicine-registration"; }
    @PostMapping("/medicine-registration") public String registerMedicine(Medicine medicine, Model model) { Medicine saved = service.registerMedicine(medicine); model.addAttribute("success", "Medicine " + saved.getMedicineId() + " registered and anchored to blockchain."); return "manufacturer/medicine-registration"; }
    @GetMapping("/batch-management") public String batchForm(Model model) { model.addAttribute("medicines", service.medicines()); model.addAttribute("batches", service.batches()); return "manufacturer/batch-management"; }
    @PostMapping("/batch-management") public String createBatch(Batch batch, Model model) { Batch saved = service.createBatch(batch); model.addAttribute("success", "Batch " + saved.getBatchId() + " created and anchored to blockchain."); model.addAttribute("medicines", service.medicines()); model.addAttribute("batches", service.batches()); return "manufacturer/batch-management"; }
}
