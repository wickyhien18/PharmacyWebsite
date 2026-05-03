package Pharmacy.Repositories;

import Pharmacy.Entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users,Long> {

    @Query("SELECT u FROM Users u JOIN FETCH u.roles WHERE u.userName = :username")
    Optional<Users> findByUserName(@Param("username") String userName);

    Optional<Users> findByEmail(String email);
    @Query("SELECT COUNT(u) > 0 FROM Users u WHERE u.userName = :userName")
    boolean existsByUserName(@Param("userName") String userName);

    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);

    @Modifying
    @Transactional
    @Query("UPDATE Users u set u.last_activity = :time where u.userName = :userName")
    void updateLastActivity(@Param("userName") String userName, @Param("time") LocalDateTime time);

    @Query("SELECT u from Users u where u.last_activity < :time")
    List<Users> findByLastActivityBefore(@Param("time") LocalDateTime time);
}
