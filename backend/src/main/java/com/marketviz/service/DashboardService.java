package com.marketviz.service;

import com.marketviz.dto.asset.AssetResponse;
import com.marketviz.dto.dashboard.*;
import com.marketviz.exception.ApiException;
import com.marketviz.exception.ResourceNotFoundException;
import com.marketviz.model.Dashboard;
import com.marketviz.model.User;
import com.marketviz.model.Widget;
import com.marketviz.repository.DashboardRepository;
import com.marketviz.repository.UserRepository;
import com.marketviz.repository.WidgetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD operations for user dashboards and their widgets.
 *
 * <p>Ownership is enforced at the service layer: every mutating operation
 * verifies that the requesting username matches the dashboard owner before
 * proceeding, throwing HTTP 403 if not.
 *
 * <p>At most one dashboard per user may carry the {@code isDefault} flag.
 * When a new default is set, the previous one is cleared atomically within
 * the same transaction.
 */
@Service
public class DashboardService {

    private final DashboardRepository dashboardRepository;
    private final WidgetRepository widgetRepository;
    private final UserRepository userRepository;
    private final AssetService assetService;

    public DashboardService(DashboardRepository dashboardRepository,
                            WidgetRepository widgetRepository,
                            UserRepository userRepository,
                            AssetService assetService) {
        this.dashboardRepository = dashboardRepository;
        this.widgetRepository    = widgetRepository;
        this.userRepository      = userRepository;
        this.assetService        = assetService;
    }

    /** Returns all dashboards owned by the requesting user, ordered by creation date. */
    @Transactional(readOnly = true)
    public List<DashboardResponse> findAll(String username) {
        var user = getUser(username);
        return dashboardRepository.findByUserIdOrderByCreatedAtAsc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    /** Returns a single dashboard by ID, verifying the caller owns it. */
    @Transactional(readOnly = true)
    public DashboardResponse findById(Long id, String username) {
        return toResponse(getOwned(id, username));
    }

    /** Creates a new dashboard; clears any existing default if {@code isDefault} is true. */
    @Transactional
    public DashboardResponse create(DashboardRequest request, String username) {
        var user = getUser(username);
        if (request.isDefault()) clearDefault(user);

        var dashboard = new Dashboard();
        dashboard.setUser(user);
        dashboard.setName(request.name());
        dashboard.setDefault(request.isDefault());
        return toResponse(dashboardRepository.save(dashboard));
    }

    /** Updates name and/or default status of an existing dashboard. */
    @Transactional
    public DashboardResponse update(Long id, DashboardRequest request, String username) {
        var dashboard = getOwned(id, username);
        if (request.isDefault()) clearDefault(dashboard.getUser());
        dashboard.setName(request.name());
        dashboard.setDefault(request.isDefault());
        return toResponse(dashboardRepository.save(dashboard));
    }

    /** Deletes a dashboard and all its widgets (cascaded by the DB constraint). */
    @Transactional
    public void delete(Long id, String username) {
        dashboardRepository.delete(getOwned(id, username));
    }

    /** Adds a new widget to the specified dashboard. */
    @Transactional
    public WidgetResponse addWidget(Long dashboardId, WidgetRequest request, String username) {
        var dashboard = getOwned(dashboardId, username);
        var asset     = assetService.getOrThrow(request.assetId());

        var widget = new Widget();
        widget.setDashboard(dashboard);
        widget.setAsset(asset);
        widget.setChartType(request.chartType());
        widget.setTimeRange(request.timeRange());
        widget.setColor(request.color() != null ? request.color() : "#3b82f6");
        widget.setTitle(request.title() != null ? request.title() : asset.getName());
        widget.setDisplayOrder(request.displayOrder());
        return toWidgetResponse(widgetRepository.save(widget));
    }

    /** Replaces the configuration of an existing widget. */
    @Transactional
    public WidgetResponse updateWidget(Long dashboardId, Long widgetId,
                                       WidgetRequest request, String username) {
        getOwned(dashboardId, username);
        var widget = widgetRepository.findById(widgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Widget not found: " + widgetId));
        var asset = assetService.getOrThrow(request.assetId());

        widget.setAsset(asset);
        widget.setChartType(request.chartType());
        widget.setTimeRange(request.timeRange());
        if (request.color() != null) widget.setColor(request.color());
        if (request.title() != null) widget.setTitle(request.title());
        widget.setDisplayOrder(request.displayOrder());
        return toWidgetResponse(widgetRepository.save(widget));
    }

    /** Removes a widget from a dashboard. */
    @Transactional
    public void deleteWidget(Long dashboardId, Long widgetId, String username) {
        getOwned(dashboardId, username);
        widgetRepository.deleteById(widgetId);
    }

    private Dashboard getOwned(Long id, String username) {
        var dashboard = dashboardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dashboard not found: " + id));
        if (!dashboard.getUser().getUsername().equals(username)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return dashboard;
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private void clearDefault(User user) {
        dashboardRepository.findByUserIdAndIsDefaultTrue(user.getId()).ifPresent(d -> {
            d.setDefault(false);
            dashboardRepository.save(d);
        });
    }

    private DashboardResponse toResponse(Dashboard d) {
        return new DashboardResponse(
                d.getId(), d.getName(), d.isDefault(),
                d.getCreatedAt(), d.getUpdatedAt(),
                d.getWidgets().stream().map(this::toWidgetResponse).toList()
        );
    }

    private WidgetResponse toWidgetResponse(Widget w) {
        var a = w.getAsset();
        return new WidgetResponse(
                w.getId(),
                new AssetResponse(a.getId(), a.getSymbol(), a.getName(), a.getDescription(), a.getCategory()),
                w.getChartType(), w.getTimeRange(),
                w.getColor(), w.getTitle(), w.getDisplayOrder()
        );
    }
}
