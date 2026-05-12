package com.marketviz.repository;

import com.marketviz.model.Widget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** JPA repository for {@link Widget} entities. */
public interface WidgetRepository extends JpaRepository<Widget, Long> {

    List<Widget> findByDashboardIdOrderByDisplayOrderAsc(Long dashboardId);
}
