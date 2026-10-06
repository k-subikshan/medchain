package meciblock.main.Repository;
import meciblock.main.model.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface BatchRepository extends JpaRepository<Batch, Long> {
    Optional<Batch> findByBatchId(String batchId);
}
