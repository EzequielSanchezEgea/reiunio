// Specific functionalities for the games section

document.addEventListener('DOMContentLoaded', function() {
    // Initialize functionalities for games section
    initGameFilters();
    initAvailabilityControl();
    validateGameForm();
    initPageControls();
    initFormValidations();
});

// Function to initialize game filters
function initGameFilters() {
    const filterForm = document.querySelector('.filter-form');
    if (!filterForm) return;
    
    // Reset all filters
    const resetButton = filterForm.querySelector('.reset-filters');
    if (resetButton) {
        resetButton.addEventListener('click', function(e) {
            e.preventDefault();
            const inputs = filterForm.querySelectorAll('input, select');
            inputs.forEach(input => {
                if (input.type === 'checkbox') {
                    input.checked = false;
                } else if (!input.name.startsWith('page') && !input.name.startsWith('size') && !input.name.startsWith('sort')) {
                    input.value = '';
                }
            });
            
            // Reset page to 0 when clearing filters
            const pageInput = filterForm.querySelector('input[name="page"]');
            if (pageInput) pageInput.value = '0';
            
            filterForm.submit();
        });
    }
    
    // REMOVED: Auto-submit when select changes - now only manual submit with button
    // Visual feedback when filters change (but don't submit automatically)
    const filterInputs = filterForm.querySelectorAll('input:not([type="hidden"]), select');
    filterInputs.forEach(input => {
        input.addEventListener('input', function() {
            updateFilterVisualFeedback();
        });
        
        input.addEventListener('change', function() {
            updateFilterVisualFeedback();
        });
        
        // Add Enter key support for input fields only (not selects)
        if (input.tagName.toLowerCase() === 'input' && input.type !== 'hidden') {
            input.addEventListener('keypress', function(e) {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    filterForm.submit();
                }
            });
        }
    });
    
    // Reset search button appearance after form submission
    filterForm.addEventListener('submit', function() {
        const searchButton = this.querySelector('button[type="submit"]');
        if (searchButton) {
            searchButton.classList.remove('btn-warning');
            searchButton.classList.add('btn-primary');
            searchButton.innerHTML = '<i class="bi bi-search"></i> Search Games';
        }
    });
}

// Function to initialize page size and sort controls (NO AUTO-SUBMIT)
function initPageControls() {
    // Page size form - remove any auto-submit and make manual only
    const pageSizeSelect = document.getElementById('pageSize');
    if (pageSizeSelect) {
        // Remove any existing onchange handlers
        pageSizeSelect.onchange = null;
        pageSizeSelect.removeAttribute('onchange');
        
        // Add visual feedback when changed
        pageSizeSelect.addEventListener('change', function() {
            const form = this.closest('form');
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.classList.add('btn-warning');
                submitBtn.textContent = 'Apply Changes';
            }
        });
    }

    // Sort form - remove any auto-submit and make manual only
    const sortBySelect = document.getElementById('sortBy');
    const sortDirSelect = document.querySelector('select[name="sortDir"]');
    
    if (sortBySelect) {
        sortBySelect.onchange = null;
        sortBySelect.removeAttribute('onchange');
        
        sortBySelect.addEventListener('change', function() {
            const form = this.closest('form');
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.classList.add('btn-warning');
                submitBtn.textContent = 'Apply Changes';
            }
        });
    }
    
    if (sortDirSelect) {
        sortDirSelect.onchange = null;
        sortDirSelect.removeAttribute('onchange');
        
        sortDirSelect.addEventListener('change', function() {
            const form = this.closest('form');
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.classList.add('btn-warning');
                submitBtn.textContent = 'Apply Changes';
            }
        });
    }
}

// Function to initialize form validations
function initFormValidations() {
    // Enhanced player count validation
    const minPlayersInput = document.getElementById('minPlayers');
    const maxPlayersInput = document.getElementById('maxPlayers');
    
    if (minPlayersInput && maxPlayersInput) {
        function validatePlayerCount() {
            const min = parseInt(minPlayersInput.value) || 0;
            const max = parseInt(maxPlayersInput.value) || 999;
            
            if (min > max && min > 0 && max > 0) {
                maxPlayersInput.setCustomValidity('Max players must be greater than or equal to min players');
                return false;
            } else {
                maxPlayersInput.setCustomValidity('');
                return true;
            }
        }
        
        minPlayersInput.addEventListener('input', validatePlayerCount);
        maxPlayersInput.addEventListener('input', validatePlayerCount);
        
        // Validate on form submission
        const filterForm = document.querySelector('.filter-form');
        if (filterForm) {
            filterForm.addEventListener('submit', function(e) {
                if (!validatePlayerCount()) {
                    e.preventDefault();
                    alert('Max players must be greater than or equal to min players');
                    return false;
                }
            });
        }
    }

    // Enhanced duration validation
    const minDurationInput = document.querySelector('input[name="minDuration"]');
    const maxDurationInput = document.querySelector('input[name="maxDuration"]');
    
    if (minDurationInput && maxDurationInput) {
        function validateDuration() {
            const min = parseInt(minDurationInput.value) || 0;
            const max = parseInt(maxDurationInput.value) || 999999;
            
            if (min > max && min > 0 && max > 0) {
                maxDurationInput.setCustomValidity('Max duration must be greater than or equal to min duration');
                return false;
            } else {
                maxDurationInput.setCustomValidity('');
                return true;
            }
        }
        
        minDurationInput.addEventListener('input', validateDuration);
        maxDurationInput.addEventListener('input', validateDuration);
        
        // Validate on form submission
        const filterForm = document.querySelector('.filter-form');
        if (filterForm) {
            filterForm.addEventListener('submit', function(e) {
                if (!validateDuration()) {
                    e.preventDefault();
                    alert('Max duration must be greater than or equal to min duration');
                    return false;
                }
            });
        }
    }
}

// Function to provide visual feedback when filters change
function updateFilterVisualFeedback() {
    const filterForm = document.querySelector('.filter-form');
    const searchButton = filterForm.querySelector('button[type="submit"]');
    
    if (searchButton && !searchButton.classList.contains('btn-warning')) {
        searchButton.classList.remove('btn-primary');
        searchButton.classList.add('btn-warning');
        searchButton.innerHTML = '<i class="bi bi-exclamation-triangle"></i> Search (filters changed)';
    }
    
    updateActiveFilterCount();
}

// Function to count and display active filters
function updateActiveFilterCount() {
    const filterForm = document.querySelector('.filter-form');
    if (!filterForm) return;
    
    const inputs = filterForm.querySelectorAll('input:not([type="hidden"]), select');
    let activeFilters = 0;
    
    inputs.forEach(input => {
        if (input.value && input.value.trim() !== '' && 
            !input.name.startsWith('page') && !input.name.startsWith('size') && !input.name.startsWith('sort')) {
            activeFilters++;
        }
    });
    
    const filterHeader = document.querySelector('.card-header h5');
    if (!filterHeader) return;
    
    const existingBadge = filterHeader.querySelector('.filter-count-badge');
    
    if (activeFilters > 0) {
        if (!existingBadge) {
            const newBadge = document.createElement('span');
            newBadge.className = 'badge bg-primary ms-2 filter-count-badge';
            newBadge.textContent = activeFilters + ' active filter' + (activeFilters > 1 ? 's' : '');
            filterHeader.appendChild(newBadge);
        } else {
            existingBadge.textContent = activeFilters + ' active filter' + (activeFilters > 1 ? 's' : '');
        }
    } else if (existingBadge) {
        existingBadge.remove();
    }
}

// Function for game availability control
function initAvailabilityControl() {
    const availabilityForms = document.querySelectorAll('form[action*="/availability"]');
    
    availabilityForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            const action = form.querySelector('input[name="available"]').value === 'true' ? 
                'mark as available' : 'mark as not available';
            
            if (!confirm(`Are you sure you want to ${action} this game?`)) {
                e.preventDefault();
            }
        });
    });
}

// Function to validate the game form
function validateGameForm() {
    const gameForm = document.querySelector('form[action*="/games/"]');
    if (!gameForm) return;
    
    gameForm.addEventListener('submit', function(e) {
        let valid = true;
        
        // Validate that minimum players is not greater than maximum players
        const minPlayers = parseInt(document.getElementById('minPlayers')?.value || 0);
        const maxPlayers = parseInt(document.getElementById('maxPlayers')?.value || 0);
        
        if (minPlayers > maxPlayers && minPlayers > 0 && maxPlayers > 0) {
            alert('The minimum number of players cannot be greater than the maximum');
            valid = false;
        }
        
        // Validate that duration is positive
        const duration = parseInt(document.getElementById('durationMinutes')?.value || 0);
        if (duration <= 0) {
            alert('Duration must be a positive number');
            valid = false;
        }
        
        // Prevent form submission if there are errors
        if (!valid) {
            e.preventDefault();
        }
    });
}

// Function to show game details in modal
function showGameDetails(gameId) {
    // This could implement a modal that quickly shows game details
    // via an AJAX request without navigating to the details page
    fetch(`/api/games/${gameId}`)
        .then(response => response.json())
        .then(data => {
            // Update modal content with game data
            const modal = document.getElementById('quickViewModal');
            if (modal) {
                modal.querySelector('.modal-title').textContent = data.name;
                modal.querySelector('.modal-body').innerHTML = `
                    <p><strong>Category:</strong> ${data.category}</p>
                    <p><strong>Players:</strong> ${data.minPlayers}-${data.maxPlayers}</p>
                    <p><strong>Duration:</strong> ${data.durationMinutes} minutes</p>
                    <p><strong>State:</strong> ${data.state}</p>
                    <p><strong>Description:</strong> ${data.description || 'Not available'}</p>
                `;
                
                // Show modal
                const bsModal = new bootstrap.Modal(modal);
                bsModal.show();
            }
        })
        .catch(error => console.error('Error loading game details:', error));
}

// Function to visually update availability status
function updateAvailability(gameId, available) {
    const badge = document.querySelector(`#availability-${gameId}`);
    if (badge) {
        badge.textContent = available ? 'Available' : 'Not available';
        badge.className = available ? 
            'badge bg-success availability-change' : 
            'badge bg-danger availability-change';
    }
}

// Function for quick filtering in games table
function quickFilter() {
    const input = document.getElementById('quickSearch');
    const filter = input.value.toUpperCase();
    const table = document.querySelector('.games-table');
    
    if (!table) return;
    
    const rows = table.querySelectorAll('tbody tr');
    
    rows.forEach(row => {
        const nameColumn = row.cells[0];
        const categoryColumn = row.cells[1];
        
        if (nameColumn && categoryColumn) {
            const nameText = nameColumn.textContent || nameColumn.innerText;
            const categoryText = categoryColumn.textContent || categoryColumn.innerText;
            
            if (nameText.toUpperCase().indexOf(filter) > -1 || 
                categoryText.toUpperCase().indexOf(filter) > -1) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        }
    });
}

// Initialize filter count on page load
document.addEventListener('DOMContentLoaded', function() {
    updateActiveFilterCount();
});