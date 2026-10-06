package meciblock.main.Repository;
import meciblock.main.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    Optional<Medicine> findByMedicineId(String medicineId);
}
