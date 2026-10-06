package meciblock.main.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Medicine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String medicineId;
    private String manufacturerId;
    private String name;
    private String dosageForm;
    private String manufacturingDate;
    private String expiryDate;
    @Column(length = 2000)
    private String details;
    private String recordHash;
}
