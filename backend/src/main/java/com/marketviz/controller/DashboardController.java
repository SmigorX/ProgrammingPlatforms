package com.marketviz.controller;

import com.marketviz.dto.dashboard.*;
import com.marketviz.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD endpoints for dashboards and their widgets.
 * All operations are automatically scoped to the currently authenticated user.
 */
@RestController
@RequestMapping("/api/dashboards")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dashboards", description = "Manage user dashboards and widgets")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @Operation(summary = "List all dashboards for the current user")
    public List<DashboardResponse> list(@AuthenticationPrincipal UserDetails user) {
        return dashboardService.findAll(user.getUsername());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a dashboard by ID")
    public DashboardResponse get(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails user) {
        return dashboardService.findById(id, user.getUsername());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new dashboard")
    public DashboardResponse create(@Valid @RequestBody DashboardRequest request,
                                    @AuthenticationPrincipal UserDetails user) {
        return dashboardService.create(request, user.getUsername());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a dashboard's name or default status")
    public DashboardResponse update(@PathVariable Long id,
                                    @Valid @RequestBody DashboardRequest request,
                                    @AuthenticationPrincipal UserDetails user) {
        return dashboardService.update(id, request, user.getUsername());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a dashboard and all its widgets")
    public void delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        dashboardService.delete(id, user.getUsername());
    }

    @PostMapping("/{id}/widgets")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a widget to a dashboard")
    public WidgetResponse addWidget(@PathVariable Long id,
                                    @Valid @RequestBody WidgetRequest request,
                                    @AuthenticationPrincipal UserDetails user) {
        return dashboardService.addWidget(id, request, user.getUsername());
    }

    @PutMapping("/{id}/widgets/{widgetId}")
    @Operation(summary = "Update an existing widget")
    public WidgetResponse updateWidget(@PathVariable Long id,
                                       @PathVariable Long widgetId,
                                       @Valid @RequestBody WidgetRequest request,
                                       @AuthenticationPrincipal UserDetails user) {
        return dashboardService.updateWidget(id, widgetId, request, user.getUsername());
    }

    @DeleteMapping("/{id}/widgets/{widgetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a widget from a dashboard")
    public void deleteWidget(@PathVariable Long id,
                             @PathVariable Long widgetId,
                             @AuthenticationPrincipal UserDetails user) {
        dashboardService.deleteWidget(id, widgetId, user.getUsername());
    }
}
