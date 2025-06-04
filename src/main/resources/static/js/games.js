// Specific functionalities for the games section

document.addEventListener('DOMContentLoaded', function() {
    // Initialize functionalities for games section
    initGameFilters();
    initAvailabilityControl();
    validateGameForm();
    initPageControls();
    initFormValidations();
    
    // Initialize game form specific functionalities
    initGameFormPhotoUpload();
    initGameFormValidation();
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

// ========================================
// GAME FORM SPECIFIC FUNCTIONS
// ========================================

// Function to initialize game form photo upload functionality
function initGameFormPhotoUpload() {
    const showUploadBtn = document.querySelector('.show-upload');
    const cancelUploadBtn = document.querySelector('.cancel-upload');
    const uploadForm = document.querySelector('.photo-upload-form');
    const photoActions = document.querySelector('.photo-actions');
    const currentImage = document.getElementById('currentGameImage');

    if (showUploadBtn && uploadForm && photoActions) {
        showUploadBtn.addEventListener('click', function() {
            uploadForm.style.display = 'block';
            photoActions.style.display = 'none';
        });

        if (cancelUploadBtn) {
            cancelUploadBtn.addEventListener('click', function() {
                uploadForm.style.display = 'none';
                photoActions.style.display = 'block';
                uploadForm.querySelector('input[type="file"]').value = '';
            });
        }
    }

    // Handle form submission for photo upload (editing mode)
    const uploadFormElement = document.querySelector('.photo-upload-form');
    if (uploadFormElement) {
        uploadFormElement.addEventListener('submit', function(e) {
            const fileInput = this.querySelector('input[type="file"]');
            const submitBtn = this.querySelector('button[type="submit"]');
            
            if (fileInput.files.length === 0) {
                e.preventDefault();
                alert('Please select a file to upload.');
                return false;
            }
            
            // Validate file
            const file = fileInput.files[0];
            if (!validateGamePhoto(file)) {
                e.preventDefault();
                return false;
            }
            
            // Show loading state
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Uploading...';
        });
    }

    // Photo preview functionality for new games
    const gamePhotoInput = document.getElementById('gamePhoto');
    const imagePreview = document.getElementById('imagePreview');
    const imagePreviewContainer = document.getElementById('imagePreviewContainer');
    const placeholderContainer = document.getElementById('placeholderContainer');
    const removeImageBtn = document.getElementById('removeImageBtn');

    if (gamePhotoInput) {
        gamePhotoInput.addEventListener('change', function(e) {
            const file = e.target.files[0];
            
            if (file) {
                // Validate file
                if (!validateGamePhoto(file)) {
                    this.value = '';
                    return;
                }
                
                const reader = new FileReader();
                reader.onload = function(e) {
                    imagePreview.src = e.target.result;
                    imagePreviewContainer.style.display = 'block';
                    if (placeholderContainer) placeholderContainer.style.display = 'none';
                    removeImageBtn.style.display = 'block';
                };
                reader.readAsDataURL(file);
            }
        });

        if (removeImageBtn) {
            removeImageBtn.addEventListener('click', function() {
                gamePhotoInput.value = '';
                imagePreview.src = '';
                imagePreviewContainer.style.display = 'none';
                if (placeholderContainer) placeholderContainer.style.display = 'block';
                removeImageBtn.style.display = 'none';
            });
        }
    }
}

// Function to validate game photo
function validateGamePhoto(file) {
    // Validate file size (2MB = 2 * 1024 * 1024 bytes)
    if (file.size > 2 * 1024 * 1024) {
        alert('File size exceeds 2MB limit. Please choose a smaller image.');
        return false;
    }
    
    // Validate file type
    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
        alert('Please select a valid image file (JPG, PNG, GIF, or WebP).');
        return false;
    }
    
    return true;
}

// Function to initialize game form validation
function initGameFormValidation() {
    // Bootstrap form validation
    const forms = document.querySelectorAll('.needs-validation');
    
    Array.prototype.slice.call(forms).forEach(function (form) {
        form.addEventListener('submit', function (event) {
            // Custom validation
            var minPlayers = parseInt(document.getElementById('minPlayers')?.value || 0);
            var maxPlayers = parseInt(document.getElementById('maxPlayers')?.value || 0);
            
            if (maxPlayers < minPlayers && minPlayers > 0 && maxPlayers > 0) {
                document.getElementById('maxPlayers').setCustomValidity('Maximum players must be greater than or equal to minimum players');
            } else {
                document.getElementById('maxPlayers').setCustomValidity('');
            }
            
            // Validate duration
            var duration = parseInt(document.getElementById('durationMinutes')?.value || 0);
            if (duration <= 0) {
                document.getElementById('durationMinutes').setCustomValidity('Duration must be a positive number');
            } else {
                document.getElementById('durationMinutes').setCustomValidity('');
            }
            
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            
            form.classList.add('was-validated');
        }, false);
    });
}

// Function to handle dynamic form updates
function updateFormVisuals() {
    // Update any visual elements based on form changes
    const gameForm = document.getElementById('gameForm');
    if (!gameForm) return;
    
    // Add visual feedback for form changes
    const inputs = gameForm.querySelectorAll('input, select, textarea');
    inputs.forEach(input => {
        input.addEventListener('input', function() {
            // Add visual feedback that form has been modified
            if (this.classList.contains('is-invalid')) {
                this.classList.remove('is-invalid');
            }
        });
        
        input.addEventListener('change', function() {
            // Add visual feedback that form has been modified
            if (this.classList.contains('is-invalid')) {
                this.classList.remove('is-invalid');
            }
        });
    });
}

// Function to enhance switch behavior
function initSwitchEnhancements() {
    const availabilitySwitch = document.getElementById('available');
    if (availabilitySwitch) {
        availabilitySwitch.addEventListener('change', function() {
            // Add visual feedback
            const container = this.closest('.games-switch');
            if (container) {
                container.classList.add('games-switch-changed');
                setTimeout(() => {
                    container.classList.remove('games-switch-changed');
                }, 300);
            }
        });
    }
}

// Function to handle form reset
function initFormReset() {
    const resetButtons = document.querySelectorAll('button[type="reset"], .btn-reset');
    resetButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            if (!confirm('Are you sure you want to reset all form data? This action cannot be undone.')) {
                e.preventDefault();
                return false;
            }
            
            // Reset all custom validations
            const form = this.closest('form');
            if (form) {
                const inputs = form.querySelectorAll('input, select, textarea');
                inputs.forEach(input => {
                    input.setCustomValidity('');
                    input.classList.remove('is-valid', 'is-invalid');
                });
                form.classList.remove('was-validated');
            }
            
            // Reset photo preview
            const imagePreview = document.getElementById('imagePreview');
            const imagePreviewContainer = document.getElementById('imagePreviewContainer');
            const placeholderContainer = document.getElementById('placeholderContainer');
            const removeImageBtn = document.getElementById('removeImageBtn');
            
            if (imagePreview) imagePreview.src = '';
            if (imagePreviewContainer) imagePreviewContainer.style.display = 'none';
            if (placeholderContainer) placeholderContainer.style.display = 'block';
            if (removeImageBtn) removeImageBtn.style.display = 'none';
        });
    });
}

// Function to add keyboard shortcuts for form
function initKeyboardShortcuts() {
    document.addEventListener('keydown', function(e) {
        // Ctrl/Cmd + S to save form
        if ((e.ctrlKey || e.metaKey) && e.key === 's') {
            e.preventDefault();
            const gameForm = document.getElementById('gameForm');
            if (gameForm) {
                gameForm.dispatchEvent(new Event('submit', { cancelable: true, bubbles: true }));
            }
        }
        
        // Escape to cancel/go back
        if (e.key === 'Escape') {
            const cancelButton = document.querySelector('a[href="/games"]');
            if (cancelButton && confirm('Are you sure you want to cancel? Any unsaved changes will be lost.')) {
                window.location.href = '/games';
            }
        }
    });
}

// Function to track form changes for unsaved changes warning
function initUnsavedChangesWarning() {
    let formChanged = false;
    const gameForm = document.getElementById('gameForm');
    
    if (gameForm) {
        // Track form changes
        const inputs = gameForm.querySelectorAll('input, select, textarea');
        inputs.forEach(input => {
            input.addEventListener('input', () => formChanged = true);
            input.addEventListener('change', () => formChanged = true);
        });
        
        // Warn before leaving page
        window.addEventListener('beforeunload', function(e) {
            if (formChanged) {
                e.preventDefault();
                e.returnValue = 'You have unsaved changes. Are you sure you want to leave?';
                return e.returnValue;
            }
        });
        
        // Don't warn when form is submitted
        gameForm.addEventListener('submit', () => formChanged = false);
        
        // Don't warn when navigating to cancel
        const cancelButton = document.querySelector('a[href="/games"]');
        if (cancelButton) {
            cancelButton.addEventListener('click', function(e) {
                if (formChanged) {
                    if (!confirm('You have unsaved changes. Are you sure you want to cancel?')) {
                        e.preventDefault();
                        return false;
                    }
                }
                formChanged = false;
            });
        }
    }
}

// Function to initialize auto-save draft functionality
function initAutoSaveDraft() {
    const gameForm = document.getElementById('gameForm');
    if (!gameForm) return;
    
    // Only for new games (not editing)
    const isEditing = gameForm.querySelector('input[name="id"]') !== null;
    if (isEditing) return;
    
    let autoSaveTimeout;
    const AUTOSAVE_DELAY = 30000; // 30 seconds
    
    function saveDraft() {
        const formData = new FormData(gameForm);
        const draftData = {};
        
        for (let [key, value] of formData.entries()) {
            if (key !== 'gamePhoto') { // Don't save file data
                draftData[key] = value;
            }
        }
        
        try {
            localStorage.setItem('gameDraft', JSON.stringify(draftData));
            console.log('Draft saved automatically');
        } catch (e) {
            console.warn('Could not save draft:', e);
        }
    }
    
    function loadDraft() {
        try {
            const draftData = localStorage.getItem('gameDraft');
            if (draftData) {
                const data = JSON.parse(draftData);
                for (let [key, value] of Object.entries(data)) {
                    const input = gameForm.querySelector(`[name="${key}"]`);
                    if (input) {
                        if (input.type === 'checkbox') {
                            input.checked = value === 'on';
                        } else {
                            input.value = value;
                        }
                    }
                }
                console.log('Draft loaded');
            }
        } catch (e) {
            console.warn('Could not load draft:', e);
        }
    }
    
    function clearDraft() {
        try {
            localStorage.removeItem('gameDraft');
            console.log('Draft cleared');
        } catch (e) {
            console.warn('Could not clear draft:', e);
        }
    }
    
    // Load draft on page load
    loadDraft();
    
    // Auto-save on input changes
    const inputs = gameForm.querySelectorAll('input, select, textarea');
    inputs.forEach(input => {
        input.addEventListener('input', function() {
            clearTimeout(autoSaveTimeout);
            autoSaveTimeout = setTimeout(saveDraft, AUTOSAVE_DELAY);
        });
    });
    
    // Clear draft on successful submission
    gameForm.addEventListener('submit', clearDraft);
}

// Initialize all form-specific functionality
document.addEventListener('DOMContentLoaded', function() {
    // Only initialize form functions if we're on a form page
    if (document.getElementById('gameForm')) {
        updateFormVisuals();
        initSwitchEnhancements();
        initFormReset();
        initKeyboardShortcuts();
        initUnsavedChangesWarning();
        initAutoSaveDraft();
    }
});