package meciblock.main.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import meciblock.main.model.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    Optional<User> findByPatientId(String patientId);
}
