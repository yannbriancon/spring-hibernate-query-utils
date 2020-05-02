package com.yannbriancon.utils.repository;

import com.yannbriancon.utils.entity.Message;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @EntityGraph(attributePaths = {"author"})
    List<Message> getAllBy();
}
