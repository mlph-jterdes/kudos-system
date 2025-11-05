package com.kudos.repository;

import com.kudos.model.Comment;
import com.kudos.model.Employee;
import com.kudos.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByTargetEmployee(Employee employee);

    List<Comment> findByTargetTeam(Team team);

    // For public history pages (recent comments)
    List<Comment> findTop20ByOrderByCreatedAtDesc();

    // Comments within date range (weekly/monthly views)
    List<Comment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
