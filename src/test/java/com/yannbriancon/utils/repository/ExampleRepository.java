package com.yannbriancon.utils.repository;

import com.yannbriancon.utils.entity.Example;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExampleRepository extends JpaRepository<Example, Long> {
    @EntityGraph(attributePaths = {"author"})
    List<Example> getAllBy();
}
