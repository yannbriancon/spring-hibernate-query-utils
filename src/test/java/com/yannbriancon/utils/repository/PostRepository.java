package com.yannbriancon.utils.repository;

import com.yannbriancon.utils.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface PostRepository extends JpaRepository<Post, Long> {
}
