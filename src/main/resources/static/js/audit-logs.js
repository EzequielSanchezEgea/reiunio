// Enhanced Audit Logs JavaScript Functionality

document.addEventListener('DOMContentLoaded', function() {
    
    // Initialize date filter functionality
    initializeDateFilters();
    
    // Reset filters functionality
    initializeResetButton();
    
    // Page size form submission
    initializePageSizeForm();
    
    // Filter form validation
    initializeFilterValidation();
    
    // Real-time statistics update
    updateStatistics();
    
    // Initialize enhanced table interactions
    initializeTableInteractions();
});

/**
 * Initialize date filter functionality with quick presets
 */
function initializeDateFilters() {
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');
    
    // Add validation to ensure start date is before end date
    if (startDateInput && endDateInput) {
        startDateInput.addEventListener('change', validateDateRange);
        endDateInput.addEventListener('change', validateDateRange);
    }
    
    // Initialize quick filter buttons
    initializeQuickDateButtons();
}

/**
 * Initialize quick date filter buttons
 */
function initializeQuickDateButtons() {
    const filterTodayBtn = document.getElementById('filterToday');
    const filterWeekBtn = document.getElementById('filterWeek');
    const filterMonthBtn = document.getElementById('filterMonth');
    const clearDatesBtn = document.getElementById('clearDates');
    
    if (filterTodayBtn) {
        filterTodayBtn.addEventListener('click', () => setDateFilter('today'));
    }
    
    if (filterWeekBtn) {
        filterWeekBtn.addEventListener('click', () => setDateFilter('week'));
    }
    
    if (filterMonthBtn) {
        filterMonthBtn.addEventListener('click', () => setDateFilter('month'));
    }
    
    if (clearDatesBtn) {
        clearDatesBtn.addEventListener('click', clearDateFilters);
    }
}

/**
 * Set date filter based on predefined periods
 * @param {string} period - 'today', 'week', or 'month'
 */
function setDateFilter(period) {
    const now = new Date();
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');
    
    if (!startDateInput || !endDateInput) return;
    
    // Set end date to current date and time
    endDateInput.value = formatDateTime(now);
    
    let startDate = new Date();
    
    switch(period) {
        case 'today':
            startDate.setHours(0, 0, 0, 0);
            break;
        case 'week':
            startDate.setDate(now.getDate() - 7);
            break;
        case 'month':
            startDate.setDate(now.getDate() - 30);
            break;
    }
    
    startDateInput.value = formatDateTime(startDate);
    
    // Add visual feedback
    highlightQuickFilterButton(period);
}

/**
 * Clear date filters
 */
function clearDateFilters() {
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');
    
    if (startDateInput) startDateInput.value = '';
    if (endDateInput) endDateInput.value = '';
    
    // Remove any date range warnings
    hideDateRangeWarning();
    
    // Reset quick filter button highlights
    document.querySelectorAll('.btn[id^="filter"]').forEach(btn => {
        btn.classList.remove('btn-primary');
        btn.classList.add('audit-btn-outline');
    });
}

/**
 * Highlight the active quick filter button
 * @param {string} period - The active period
 */
function highlightQuickFilterButton(period) {
    // Reset all buttons
    document.querySelectorAll('.btn[id^="filter"]').forEach(btn => {
        btn.classList.remove('btn-primary');
        btn.classList.add('audit-btn-outline');
    });
    
    // Highlight active button
    const activeBtn = document.getElementById(`filter${period.charAt(0).toUpperCase() + period.slice(1)}`);
    if (activeBtn) {
        activeBtn.classList.remove('audit-btn-outline');
        activeBtn.classList.add('btn-primary');
    }
}

/**
 * Format date for datetime-local input
 * @param {Date} date - Date to format
 * @return {string} - Formatted date string
 */
function formatDateTime(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    
    return `${year}-${month}-${day}T${hours}:${minutes}`;
}

/**
 * Validate that start date is before end date
 */
function validateDateRange() {
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');
    
    if (startDateInput && endDateInput && startDateInput.value && endDateInput.value) {
        const startDate = new Date(startDateInput.value);
        const endDate = new Date(endDateInput.value);
        
        if (startDate > endDate) {
            // Show warning
            showDateRangeWarning();
            // Auto-adjust end date
            endDateInput.value = startDateInput.value;
        } else {
            hideDateRangeWarning();
        }
    }
}

/**
 * Show date range warning
 */
function showDateRangeWarning() {
    let warning = document.getElementById('dateRangeWarning');
    if (!warning) {
        warning = document.createElement('div');
        warning.id = 'dateRangeWarning';
        warning.className = 'alert alert-warning alert-dismissible fade show mt-2';
        warning.innerHTML = `
            <i class="bi bi-exclamation-triangle"></i>
            <strong>Date Range Warning:</strong> Start date cannot be after end date. End date has been adjusted automatically.
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `;
        
        const endDateInput = document.getElementById('endDate');
        endDateInput.parentNode.appendChild(warning);
    }
}

/**
 * Hide date range warning
 */
function hideDateRangeWarning() {
    const warning = document.getElementById('dateRangeWarning');
    if (warning) {
        warning.remove();
    }
}

/**
 * Initialize reset filters button
 */
function initializeResetButton() {
    const resetButton = document.querySelector('.reset-filters');
    if (resetButton) {
        resetButton.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Add loading state
            resetButton.innerHTML = '<i class="bi bi-arrow-repeat"></i> Clearing...';
            resetButton.disabled = true;
            
            const form = document.querySelector('.filter-form');
            const inputs = form.querySelectorAll('input:not([type="hidden"]), select');
            
            inputs.forEach(input => {
                if (input.type === 'checkbox') {
                    input.checked = false;
                } else {
                    input.value = '';
                }
            });
            
            // Clear date filters
            clearDateFilters();
            
            // Submit form
            setTimeout(() => {
                form.submit();
            }, 300);
        });
    }
}

/**
 * Initialize page size form auto-submission
 */
function initializePageSizeForm() {
    const pageSizeSelect = document.getElementById('pageSize');
    if (pageSizeSelect) {
        pageSizeSelect.addEventListener('change', function() {
            // Add visual feedback
            const form = this.form;
            
            // Show loading state
            this.style.opacity = '0.7';
            this.disabled = true;
            
            // Submit after short delay for better UX
            setTimeout(() => {
                form.submit();
            }, 200);
        });
    }
}

/**
 * Initialize filter form validation
 */
function initializeFilterValidation() {
    const filterForm = document.querySelector('.filter-form');
    if (filterForm) {
        filterForm.addEventListener('submit', function(e) {
            const submitButton = this.querySelector('button[type="submit"]');
            
            // Add loading state to submit button
            if (submitButton) {
                const originalText = submitButton.innerHTML;
                submitButton.innerHTML = '<i class="bi bi-hourglass-split"></i> Filtering...';
                submitButton.disabled = true;
                
                // Re-enable after form submission
                setTimeout(() => {
                    submitButton.innerHTML = originalText;
                    submitButton.disabled = false;
                }, 2000);
            }
        });
    }
}

/**
 * Update statistics in real-time
 */
function updateStatistics() {
    // Count visible rows
    const visibleRows = document.querySelectorAll('.audit-table-row').length;
    const totalRecords = document.querySelector('.audit-stat-number');
    
    if (totalRecords && visibleRows > 0) {
        // Add animation to statistics
        animateCounterUpdate(totalRecords);
    }
}

/**
 * Animate counter update
 */
function animateCounterUpdate(element) {
    const finalValue = parseInt(element.textContent);
    const duration = 1000; // 1 second
    const steps = 20;
    const increment = finalValue / steps;
    let currentValue = 0;
    
    const timer = setInterval(() => {
        currentValue += increment;
        if (currentValue >= finalValue) {
            element.textContent = finalValue;
            clearInterval(timer);
        } else {
            element.textContent = Math.floor(currentValue);
        }
    }, duration / steps);
}

/**
 * Quick filter functionality
 */
function applyQuickFilter(actionType, entity) {
    const url = new URL(window.location.href);
    url.searchParams.set('page', '0');
    
    if (actionType) {
        url.searchParams.set('actionType', actionType);
    }
    
    if (entity) {
        url.searchParams.set('entity', entity);
    }
    
    window.location.href = url.toString();
}

/**
 * Export audit logs functionality
 */
function exportAuditLogs(format = 'csv') {
    const currentUrl = new URL(window.location.href);
    currentUrl.searchParams.set('export', format);
    
    // Create temporary link and click it
    const link = document.createElement('a');
    link.href = currentUrl.toString();
    link.download = `audit-logs-${new Date().toISOString().split('T')[0]}.${format}`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

/**
 * Refresh audit logs data
 */
function refreshAuditLogs() {
    const refreshButton = document.querySelector('.refresh-logs');
    if (refreshButton) {
        refreshButton.innerHTML = '<i class="bi bi-arrow-clockwise"></i> Refreshing...';
        refreshButton.disabled = true;
        
        // Reload page
        setTimeout(() => {
            window.location.reload();
        }, 500);
    }
}

/**
 * Toggle row details
 */
function toggleRowDetails(logId) {
    const detailsRow = document.getElementById(`details-${logId}`);
    if (detailsRow) {
        if (detailsRow.style.display === 'none' || !detailsRow.style.display) {
            detailsRow.style.display = 'table-row';
            // Animate in
            detailsRow.style.opacity = '0';
            setTimeout(() => {
                detailsRow.style.opacity = '1';
            }, 10);
        } else {
            detailsRow.style.display = 'none';
        }
    }
}

/**
 * Copy log details to clipboard
 */
function copyLogDetails(logId) {
    const row = document.querySelector(`[data-log-id="${logId}"]`);
    if (row) {
        const details = {
            dateTime: row.querySelector('.audit-date').textContent + ' ' + row.querySelector('.audit-time').textContent,
            user: row.querySelector('.fw-bold').textContent,
            action: row.querySelector('.audit-action-badge').textContent,
            entity: row.querySelector('.audit-entity-icon').nextElementSibling.textContent,
            description: row.querySelector('.audit-description').textContent
        };
        
        const text = Object.entries(details)
            .map(([key, value]) => `${key}: ${value}`)
            .join('\n');
        
        navigator.clipboard.writeText(text).then(() => {
            showCopySuccess();
        }).catch(() => {
            showCopyError();
        });
    }
}

/**
 * Show copy success message
 */
function showCopySuccess() {
    const toast = document.createElement('div');
    toast.className = 'alert alert-success position-fixed';
    toast.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 250px;';
    toast.innerHTML = '<i class="bi bi-check-circle"></i> Log details copied to clipboard!';
    
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.remove();
    }, 3000);
}

/**
 * Show copy error message
 */
function showCopyError() {
    const toast = document.createElement('div');
    toast.className = 'alert alert-danger position-fixed';
    toast.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 250px;';
    toast.innerHTML = '<i class="bi bi-exclamation-triangle"></i> Failed to copy to clipboard!';
    
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.remove();
    }, 3000);
}

/**
 * Enhanced table interactions
 */
function initializeTableInteractions() {
    const tableRows = document.querySelectorAll('.audit-table-row');
    
    tableRows.forEach(row => {
        // Add click to expand functionality
        row.addEventListener('click', function(e) {
            // Don't trigger if clicking on buttons or links
            if (e.target.tagName === 'BUTTON' || e.target.tagName === 'A' || e.target.closest('button')) {
                return;
            }
            
            const logId = this.dataset.logId;
            if (logId) {
                toggleRowDetails(logId);
            }
        });
        
        // Add right-click context menu
        row.addEventListener('contextmenu', function(e) {
            e.preventDefault();
            const logId = this.dataset.logId;
            if (logId) {
                showContextMenu(e, logId);
            }
        });
        
        // Add hover effects
        row.addEventListener('mouseenter', function() {
            this.style.backgroundColor = 'rgba(0, 123, 255, 0.05)';
        });
        
        row.addEventListener('mouseleave', function() {
            this.style.backgroundColor = '';
        });
    });
}

/**
 * Show context menu for audit log row
 */
function showContextMenu(event, logId) {
    // Remove existing context menu
    const existingMenu = document.getElementById('audit-context-menu');
    if (existingMenu) {
        existingMenu.remove();
    }
    
    const menu = document.createElement('div');
    menu.id = 'audit-context-menu';
    menu.className = 'dropdown-menu show position-fixed';
    menu.style.cssText = `top: ${event.clientY}px; left: ${event.clientX}px; z-index: 9999;`;
    
    menu.innerHTML = `
        <button class="dropdown-item" onclick="copyLogDetails('${logId}')">
            <i class="bi bi-clipboard"></i> Copy Details
        </button>
        <button class="dropdown-item" onclick="toggleRowDetails('${logId}')">
            <i class="bi bi-eye"></i> Toggle Details
        </button>
        <hr class="dropdown-divider">
        <button class="dropdown-item text-muted" disabled>
            <i class="bi bi-info-circle"></i> Log ID: ${logId}
        </button>
    `;
    
    document.body.appendChild(menu);
    
    // Remove menu when clicking elsewhere
    setTimeout(() => {
        document.addEventListener('click', function removeMenu() {
            menu.remove();
            document.removeEventListener('click', removeMenu);
        });
    }, 100);
}

/**
 * Initialize date persistence on page load
 */
function initializeDatePersistence() {
    // Check URL parameters for existing date filters
    const urlParams = new URLSearchParams(window.location.search);
    const startDate = urlParams.get('startDate');
    const endDate = urlParams.get('endDate');
    
    if (startDate || endDate) {
        // Determine which quick filter might be active
        if (startDate && endDate) {
            const start = new Date(startDate);
            const end = new Date(endDate);
            const now = new Date();
            
            // Check if it matches today
            const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate());
            if (start.getTime() === todayStart.getTime() && 
                Math.abs(end.getTime() - now.getTime()) < 60000) { // Within 1 minute
                highlightQuickFilterButton('today');
            }
            // Check if it matches last 7 days
            else if (Math.abs(start.getTime() - (now.getTime() - 7 * 24 * 60 * 60 * 1000)) < 60000) {
                highlightQuickFilterButton('week');
            }
            // Check if it matches last 30 days
            else if (Math.abs(start.getTime() - (now.getTime() - 30 * 24 * 60 * 60 * 1000)) < 60000) {
                highlightQuickFilterButton('month');
            }
        }
    }
}

// Initialize date persistence after DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeDatePersistence();
});

// Export functions for global access
window.auditLogs = {
    applyQuickFilter,
    exportAuditLogs,
    refreshAuditLogs,
    toggleRowDetails,
    copyLogDetails,
    setDateFilter,
    clearDateFilters
};