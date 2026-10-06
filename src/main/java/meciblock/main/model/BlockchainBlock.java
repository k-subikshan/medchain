package meciblock.main.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name="blockchain_block")
@Getter @Setter @NoArgsConstructor
public class BlockchainBlock {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long blockIndex;
    private String recordType;
    private String recordId;
    @Column(length = 64, nullable = false)
    private String dataHash;
    @Column(length = 64, nullable = false)
    private String previousHash;
    @Column(length = 64, nullable = false, unique = true)
    private String blockHash;
    private LocalDateTime timestamp;
}
