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

    @Query("SELECT u FROM Users u JOIN FETCH u.roles WHERE u.email = :email")
    Optional<Users> findByEmail(@Param("email") String email);
    @Query("SELECT COUNT(u) > 0 FROM Users u WHERE u.userName = :userName")
    boolean existsByUserName(@Param("userName") String userName);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Users u WHERE u.email = :email OR u.phone = :phone")
    boolean existsByEmailOrPhone(@Param("email") String email, @Param("phone") String phone);

    @Modifying
    @Transactional
    @Query("UPDATE Users u set u.lastActivity = :time where u.userName = :userName")
    void updateLastActivity(@Param("userName") String userName, @Param("time") LocalDateTime time);

    @Query("SELECT u from Users u where u.lastActivity < :time")
    List<Users> findByLastActivityBefore(@Param("time") LocalDateTime time);
}
