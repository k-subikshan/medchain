package meciblock.main.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import meciblock.main.Service.BlockchainService;
import meciblock.main.Service.MediblockService;
import meciblock.main.Repository.UserRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final BlockchainService blockchainService;
    private final MediblockService mediblockService;
    private final UserRepository users;
    public AdminController(BlockchainService blockchainService, MediblockService mediblockService, UserRepository users) {
        this.blockchainService = blockchainService; this.mediblockService = mediblockService; this.users = users;
    }
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("userCount", users.count());
        model.addAttribute("medicineCount", mediblockService.medicines().size());
        model.addAttribute("blockCount", blockchainService.all().size());
        model.addAttribute("flaggedCount", 0);
        model.addAttribute("integrity", blockchainService.verifyChain());
        return "admin/dashboard";
    }
    @GetMapping("/blockchain")
    public String getBlockChain(Authentication auth, Model model) { model.addAttribute("username", auth.getName()); model.addAttribute("blocks", blockchainService.all()); model.addAttribute("integrity", blockchainService.verifyChain()); return "admin/blockchain"; }
    @GetMapping("/audit")
    public String getAudit(Authentication auth, Model model) { model.addAttribute("username", auth.getName()); model.addAttribute("blocks", blockchainService.all()); return "admin/audit"; }
    @GetMapping("/users")
    public String getUsers(Authentication auth, Model model) { model.addAttribute("username", auth.getName()); model.addAttribute("users", users.findAll()); return "admin/users"; }
}
