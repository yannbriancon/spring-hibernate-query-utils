package com.yannbriancon.utils.repository;

import com.yannbriancon.utils.entity.DomainUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface DomainUserRepository extends JpaRepository<DomainUser, Long> {
}
