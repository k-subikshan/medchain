package meciblock.main.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="medicine_batch")
@Getter @Setter @NoArgsConstructor
public class Batch {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String batchId;
    private String medicineId;
    private Integer packageCount;
    private String manufacturingDate;
    private String recordHash;
}
