package meciblock.main.Repository;
import meciblock.main.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    Optional<Prescription> findByPrescriptionId(String prescriptionId);
    java.util.List<Prescription> findByStatusOrderByIdDesc(String status);
}
