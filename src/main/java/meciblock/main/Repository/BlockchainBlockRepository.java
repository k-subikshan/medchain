package meciblock.main.Repository;
import meciblock.main.model.BlockchainBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface BlockchainBlockRepository extends JpaRepository<BlockchainBlock, Long> {
    List<BlockchainBlock> findAllByOrderByBlockIndexDesc();
    Optional<BlockchainBlock> findTopByOrderByBlockIndexDesc();
}
