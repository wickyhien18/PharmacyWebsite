package Repositories;

import Entities.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository  extends JpaRepository<Roles,Integer> {


}
