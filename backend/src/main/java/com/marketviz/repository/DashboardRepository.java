package com.marketviz.repository;

import com.marketviz.model.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** JPA repository for {@link Dashboard} entities. */
public interface DashboardRepository extends JpaRepository<Dashboard, Long> {

    List<Dashboard> findByUserIdOrderByCreatedAtAsc(Long userId);

    Optional<Dashboard> findByUserIdAndIsDefaultTrue(Long userId);
}
