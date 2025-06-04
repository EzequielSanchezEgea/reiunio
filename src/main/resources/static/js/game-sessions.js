// game-sessions.js - Specific functionalities for game sessions

document.addEventListener('DOMContentLoaded', function() {
    // Initialize functionalities for game sessions
    initGameSessionForm();
    initGamePreview();
    initCustomImageUpload();
    initDateValidation();
});

// Function to initialize game session form validation
function initGameSessionForm() {
    const forms = document.querySelectorAll('.needs-validation');
    
    Array.prototype.slice.call(forms).forEach(function (form) {
        form.addEventListener('submit', function (event) {
            if (!form.checkValidity() || !validateGameSessionForm()) {
                event.preventDefault();
                event.stopPropagation();
            }
            
            form.classList.add('was-validated');
        }, false);
    });
}

// Function to initialize game preview functionality
function initGamePreview() {
    const gameIdSelect = document.getElementById('gameId');
    if (!gameIdSelect) return;
    
    // Setup initial state
    setupInitialGameState();
    
    // Handle game selection changes
    gameIdSelect.addEventListener('change', handleGameSelectionChange);
}

// Function to setup initial game state (for editing)
function setupInitialGameState() {
    const gameIdSelect = document.getElementById('gameId');
    const personalGameImageSection = document.getElementById('personalGameImageSection');
    const gamePreview = document.getElementById('gamePreview');
    
    if (!gameIdSelect) return;
    
    const hasSelectedGame = gameIdSelect.value && gameIdSelect.value !== '';
    console.log('Has selected game:', hasSelectedGame);
    
    if (hasSelectedGame) {
        // Show library game preview
        if (personalGameImageSection) personalGameImageSection.style.display = 'none';
        if (gamePreview) gamePreview.style.display = 'block';
        
        // Get selected game information via AJAX
        loadGameInfoFromServer(gameIdSelect.value);
    } else {
        // Show personal game section
        if (gamePreview) gamePreview.style.display = 'none';
        if (personalGameImageSection) personalGameImageSection.style.display = 'block';
        console.log('Showing personal game section');
    }
}

// Function to handle game selection changes
function handleGameSelectionChange() {
    const gameId = this.value;
    const gamePreview = document.getElementById('gamePreview');
    const personalGameImageSection = document.getElementById('personalGameImageSection');
    
    console.log('Game selection changed. New gameId:', gameId);
    
    if (gameId) {
        // Show library game preview
        if (gamePreview) gamePreview.style.display = 'block';
        if (personalGameImageSection) personalGameImageSection.style.display = 'none';
        
        // Load game info from server
        loadGameInfoFromServer(gameId);
        
        console.log('Loading library game info for ID:', gameId);
    } else {
        // Show personal game section
        if (gamePreview) gamePreview.style.display = 'none';
        if (personalGameImageSection) personalGameImageSection.style.display = 'block';
        console.log('Showing personal game section');
    }
}

// Function to load game information from server via AJAX
function loadGameInfoFromServer(gameId) {
    fetch(`/game-sessions/api/game-info/${gameId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success && data.game) {
                updateGameInfoDisplay(data.game);
                updateGameImage(data.game);
            } else {
                console.error('Error loading game info:', data.message);
                showGameInfoError();
            }
        })
        .catch(error => {
            console.error('Error fetching game info:', error);
            showGameInfoError();
        });
}

// Function to update game information display
function updateGameInfoDisplay(game) {
    const gameInfo = document.getElementById('gameInfo');
    if (!gameInfo) return;
    
    const playersInfo = `${game.minPlayers}-${game.maxPlayers} players`;
    const durationInfo = `${game.durationMinutes} min`;
    const categoryInfo = game.category || 'No category';
    
    gameInfo.innerHTML = `
        <h5>${game.name}</h5>
        <p><strong>Category:</strong> ${categoryInfo}</p>
        <p><strong>Players:</strong> ${playersInfo} | <strong>Duration:</strong> ${durationInfo}</p>
        <p><small class="text-success">✓ Available in our library</small></p>
    `;
}

// Function to update game image
function updateGameImage(game) {
    const gamePreviewImage = document.getElementById('gamePreviewImage');
    if (!gamePreviewImage) return;
    
    // Use the game's imageUrl which handles both custom images and placeholders
    gamePreviewImage.src = game.imageUrl;
    gamePreviewImage.alt = `Image of ${game.name}`;
    
    // Add error handler in case the image fails to load
    gamePreviewImage.onerror = function() {
        this.src = '/defaults/game-placeholder.jpg';
        this.alt = `Placeholder image for ${game.name}`;
    };
}

// Function to show error when game info cannot be loaded
function showGameInfoError() {
    const gameInfo = document.getElementById('gameInfo');
    const gamePreviewImage = document.getElementById('gamePreviewImage');
    
    if (gameInfo) {
        gameInfo.innerHTML = `
            <h5>Game Information</h5>
            <p class="text-danger">Error loading game information</p>
        `;
    }
    
    if (gamePreviewImage) {
        gamePreviewImage.src = '/defaults/game-placeholder.jpg';
        gamePreviewImage.alt = 'Error loading game image';
    }
}

// Function to initialize custom image upload functionality
function initCustomImageUpload() {
    const customGameImage = document.getElementById('customGameImage');
    const removeImageBtn = document.getElementById('removeImageBtn');
    
    if (customGameImage) {
        customGameImage.addEventListener('change', handleImagePreview);
    }
    
    if (removeImageBtn) {
        removeImageBtn.addEventListener('click', removeImagePreview);
    }
}

// Function to handle image preview
function handleImagePreview(e) {
    const file = e.target.files[0];
    const preview = document.getElementById('imagePreview');
    const previewSection = document.getElementById('imagePreviewSection');
    const fileInputSection = document.getElementById('fileInputSection');
    
    if (file && preview && previewSection && fileInputSection) {
        // Validate file before showing preview
        if (!validateImageFile(file)) {
            e.target.value = '';
            return;
        }
        
        const reader = new FileReader();
        reader.onload = function(e) {
            preview.src = e.target.result;
            previewSection.style.display = 'block';
            fileInputSection.style.display = 'none';
        };
        reader.readAsDataURL(file);
    }
}

// Function to validate image file
function validateImageFile(file) {
    // Check file size (2MB limit)
    const maxSize = 2 * 1024 * 1024; // 2MB in bytes
    if (file.size > maxSize) {
        alert('File size exceeds 2MB limit. Please choose a smaller image.');
        return false;
    }
    
    // Check file type
    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
        alert('Please select a valid image file (JPG, PNG, GIF, or WebP).');
        return false;
    }
    
    return true;
}

// Function to remove image preview
function removeImagePreview() {
    const preview = document.getElementById('imagePreview');
    const previewSection = document.getElementById('imagePreviewSection');
    const fileInputSection = document.getElementById('fileInputSection');
    const fileInput = document.getElementById('customGameImage');
    
    if (preview) preview.src = '';
    if (fileInput) fileInput.value = '';
    if (previewSection) previewSection.style.display = 'none';
    if (fileInputSection) fileInputSection.style.display = 'block';
}

// Helper function to clear custom validity
function clearCustomValidity(element) {
    if (element) {
        element.setCustomValidity('');
    }
}

// Helper function to clear validation classes
function clearValidationClasses(element) {
    if (element) {
        element.classList.remove('is-valid', 'is-invalid');
    }
}

// Function to initialize date validation
function initDateValidation() {
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');
    const startTimeInput = document.getElementById('startTime');
    const endTimeInput = document.getElementById('endTime');
    const customGameNameInput = document.getElementById('customGameName');
    
    if (!startDateInput || !endDateInput) return;
    
    const today = new Date().toISOString().split('T')[0];
    const isEditing = document.querySelector('input[type="hidden"][name="_method"]') !== null ||
                     window.location.pathname.includes('/edit');
    
    console.log('Form initialized. IsEditing:', isEditing);
    
    // Only set min date for new sessions, not when editing
    if (!isEditing) {
        startDateInput.setAttribute('min', today);
        endDateInput.setAttribute('min', today);
    }
    
    // Add event listeners with proper validation clearing
    if (startDateInput) {
        startDateInput.addEventListener('change', function() {
            clearCustomValidity(this);
            clearValidationClasses(this);
            handleStartDateChange();
            validateDatesAndTimes();
        });
        
        startDateInput.addEventListener('input', function() {
            clearCustomValidity(this);
            clearValidationClasses(this);
        });
    }
    
    if (endDateInput) {
        endDateInput.addEventListener('change', function() {
            clearCustomValidity(this);
            clearValidationClasses(this);
            toggleEndTimeRequirement();
            validateDatesAndTimes();
        });
        
        endDateInput.addEventListener('input', function() {
            clearCustomValidity(this);
            clearValidationClasses(this);
        });
    }
    
    if (startTimeInput) {
        startTimeInput.addEventListener('change', function() {
            clearCustomValidity(this);
            clearValidationClasses(this);
            validateDatesAndTimes();
            toggleEndTimeRequirement(); // Update help text based on time conflict
        });
        
        startTimeInput.addEventListener('input', function() {
            clearCustomValidity(this);
            clearValidationClasses(this);
        });
    }
    
    if (endTimeInput) {
        endTimeInput.addEventListener('change', function() {
            clearCustomValidity(this);
            clearValidationClasses(this);
            validateDatesAndTimes();
            toggleEndTimeRequirement(); // Update help text based on time conflict
        });
        
        endTimeInput.addEventListener('input', function() {
            clearCustomValidity(this);
            clearValidationClasses(this);
        });
    }
    
    if (customGameNameInput) {
        customGameNameInput.addEventListener('input', function() {
            clearCustomValidity(this);
            clearValidationClasses(this);
        });
    }
    
    // Initial check for end time requirement
    toggleEndTimeRequirement();
}

// Function to handle start date changes
function handleStartDateChange() {
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');
    
    if (!startDateInput || !endDateInput) return;
    
    endDateInput.setAttribute('min', startDateInput.value);
    if (endDateInput.value && endDateInput.value < startDateInput.value) {
        endDateInput.value = startDateInput.value;
    }
    toggleEndTimeRequirement();
}

// Function to toggle end time requirement
function toggleEndTimeRequirement() {
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');
    const endTimeInput = document.getElementById('endTime');
    
    if (!startDateInput || !endDateInput || !endTimeInput) return;
    
    const startDate = startDateInput.value;
    const endDate = endDateInput.value;
    const isSameDay = startDate && endDate && startDate === endDate;
    
    const formText = endTimeInput.parentElement.querySelector('.form-text');
    
    if (isSameDay) {
        endTimeInput.setAttribute('required', 'required');
        if (formText) {
            // Check if there's a time conflict to show specific message
            const startTime = document.getElementById('startTime').value;
            const endTime = endTimeInput.value;
            
            if (startTime && endTime && endTime <= startTime) {
                formText.innerHTML = '<strong class="text-danger">End time must be later than start time (' + startTime + ')</strong>';
            } else {
                formText.innerHTML = '<strong>Required</strong> for same-day sessions.';
            }
        }
    } else {
        endTimeInput.removeAttribute('required');
        if (formText) {
            formText.innerHTML = 'Optional for multi-day sessions. Required for same-day sessions.';
        }
    }
}

// NEW: Comprehensive date and time validation function
function validateDatesAndTimes() {
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');
    const startTimeInput = document.getElementById('startTime');
    const endTimeInput = document.getElementById('endTime');
    
    if (!startDateInput || !endDateInput || !startTimeInput) return;
    
    const startDate = startDateInput.value;
    const endDate = endDateInput.value;
    const startTime = startTimeInput.value;
    const endTime = endTimeInput ? endTimeInput.value : null;
    
    // Clear all previous validation states
    [startDateInput, endDateInput, startTimeInput, endTimeInput].forEach(input => {
        if (input) {
            clearCustomValidity(input);
            clearValidationClasses(input);
        }
    });
    
    // Only validate if we have the basic required values
    if (!startDate || !endDate || !startTime) return;
    
    const isEditing = document.querySelector('input[type="hidden"][name="_method"]') !== null ||
                     window.location.pathname.includes('/edit');
    
    let hasErrors = false;
    
    // Validate start date is not in the past (only for new sessions)
    if (!isEditing) {
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const startDateObj = new Date(startDate);
        
        if (startDateObj < today) {
            startDateInput.setCustomValidity('The start date cannot be in the past.');
            startDateInput.classList.add('is-invalid');
            hasErrors = true;
        }
    }
    
    // Validate end date is not before start date
    if (endDate < startDate) {
        endDateInput.setCustomValidity('End date cannot be before start date.');
        endDateInput.classList.add('is-invalid');
        hasErrors = true;
    }
    
    // Validate times for same-day sessions
    if (startDate === endDate) {
        if (!endTime) {
            if (endTimeInput) {
                endTimeInput.setCustomValidity('End time is required for same-day sessions.');
                endTimeInput.classList.add('is-invalid');
                hasErrors = true;
            }
        } else if (endTime <= startTime) {
            if (endTimeInput) {
                const formattedStartTime = formatTimeForDisplay(startTime);
                const formattedEndTime = formatTimeForDisplay(endTime);
                endTimeInput.setCustomValidity(`End time (${formattedEndTime}) must be after start time (${formattedStartTime})`);
                endTimeInput.classList.add('is-invalid');
                hasErrors = true;
                
                // Update the help text dynamically
                updateEndTimeHelpText(startTime, endTime);
            }
        } else {
            // Valid same-day times, reset help text
            resetEndTimeHelpText();
        }
    } else {
        // Multi-day session, reset help text
        resetEndTimeHelpText();
    }
    
    // If no errors, mark valid inputs as valid
    if (!hasErrors) {
        [startDateInput, endDateInput, startTimeInput, endTimeInput].forEach(input => {
            if (input && input.value) {
                input.classList.add('is-valid');
            }
        });
    }
    
    return !hasErrors;
}

// Helper function to format time for user-friendly display
function formatTimeForDisplay(timeString) {
    if (!timeString) return '';
    
    try {
        const [hours, minutes] = timeString.split(':');
        const hour = parseInt(hours);
        const minute = minutes || '00';
        
        // Convert to 12-hour format
        if (hour === 0) {
            return `12:${minute} AM`;
        } else if (hour < 12) {
            return `${hour}:${minute} AM`;
        } else if (hour === 12) {
            return `12:${minute} PM`;
        } else {
            return `${hour - 12}:${minute} PM`;
        }
    } catch (e) {
        // Fallback to original format if parsing fails
        return timeString;
    }
}

// Helper function to update end time help text with specific time conflict info
function updateEndTimeHelpText(startTime, endTime) {
    const endTimeInput = document.getElementById('endTime');
    if (!endTimeInput) return;
    
    const formText = endTimeInput.parentElement.querySelector('.form-text');
    if (formText) {
        const formattedStartTime = formatTimeForDisplay(startTime);
        const formattedEndTime = formatTimeForDisplay(endTime);
        
        formText.innerHTML = `<strong class="text-danger"><i class="bi bi-exclamation-triangle"></i> 
            End time (${formattedEndTime}) must be later than start time (${formattedStartTime})</strong>`;
    }
}

// Helper function to reset end time help text to default
function resetEndTimeHelpText() {
    const endTimeInput = document.getElementById('endTime');
    if (!endTimeInput) return;
    
    const formText = endTimeInput.parentElement.querySelector('.form-text');
    if (formText) {
        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;
        const isSameDay = startDate && endDate && startDate === endDate;
        
        if (isSameDay) {
            formText.innerHTML = '<strong>Required</strong> for same-day sessions.';
        } else {
            formText.innerHTML = 'Optional for multi-day sessions. Required for same-day sessions.';
        }
    }
}

// Function for custom form validation
function validateGameSessionForm() {
    let valid = true;
    const isEditing = document.querySelector('input[type="hidden"][name="_method"]') !== null ||
                     window.location.pathname.includes('/edit');
    
    // Get values
    const customGameNameInput = document.getElementById('customGameName');
    const customGameName = customGameNameInput ? customGameNameInput.value : '';
    
    // Clear all custom validation first
    const fields = ['startDate', 'endDate', 'startTime', 'endTime', 'customGameName'];
    fields.forEach(fieldId => {
        const field = document.getElementById(fieldId);
        if (field) {
            clearCustomValidity(field);
            clearValidationClasses(field);
        }
    });
    
    // Validate custom game name
    if (!customGameName || customGameName.trim() === '') {
        if (customGameNameInput) {
            customGameNameInput.setCustomValidity('Game name is required.');
            customGameNameInput.classList.add('is-invalid');
            valid = false;
        }
    }
    
    // Validate dates and times
    if (!validateDatesAndTimes()) {
        valid = false;
    }
    
    return valid;
}

// Function to handle session deletion (for list pages)
function initSessionDeletion() {
    const deleteButtons = document.querySelectorAll('.delete-session-btn');
    const deleteModal = document.getElementById('deleteSessionModal');
    const deleteForm = document.getElementById('deleteSessionForm');
    const sessionTitleSpan = document.getElementById('sessionTitleToDelete');
    const libraryGameInfo = document.getElementById('libraryGameInfo');
    const gameNameSpan = document.getElementById('gameNameToRelease');
    
    if (!deleteButtons.length || !deleteModal) return;
    
    deleteButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            const sessionId = this.getAttribute('data-session-id');
            const sessionTitle = this.getAttribute('data-session-title');
            const isLibraryGame = this.getAttribute('data-is-library-game') === 'true';
            const gameName = this.getAttribute('data-game-name');
            
            // Update modal content
            if (sessionTitleSpan) sessionTitleSpan.textContent = sessionTitle;
            if (deleteForm) deleteForm.action = '/game-sessions/' + sessionId + '/delete';
            
            // Show/hide library game info
            if (libraryGameInfo && gameNameSpan) {
                if (isLibraryGame && gameName) {
                    gameNameSpan.textContent = gameName;
                    libraryGameInfo.style.display = 'block';
                } else {
                    libraryGameInfo.style.display = 'none';
                }
            }
            
            // Show modal
            const modalInstance = new bootstrap.Modal(deleteModal);
            modalInstance.show();
        });
    });
}

// Function to handle session filters (for list pages)
function initSessionFilters() {
    const filterButtons = document.querySelectorAll('.btn[href*="filter="]');
    
    filterButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            // Add loading state
            button.classList.add('loading');
            button.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>' + button.textContent;
        });
    });
}

// Initialize session-specific features based on page
function initSessionFeatures() {
    // Check if we're on a session list page
    if (window.location.pathname.includes('/game-sessions') && 
        !window.location.pathname.includes('/new') && 
        !window.location.pathname.includes('/edit')) {
        initSessionDeletion();
        initSessionFilters();
    }
}

// Initialize all session features when page loads
document.addEventListener('DOMContentLoaded', function() {
    initSessionFeatures();
});

// Handle delete modal for sessions
document.addEventListener('DOMContentLoaded', function() {
    const deleteModal = document.getElementById('deleteSessionModal');
    const deleteForm = document.getElementById('deleteSessionForm');
    const sessionTitleSpan = document.getElementById('sessionTitleToDelete');
    const libraryGameInfo = document.getElementById('libraryGameInfo');
    const gameNameSpan = document.getElementById('gameNameToRelease');
    const deleteButtons = document.querySelectorAll('.delete-session-btn');
    
    // Event handling for delete buttons
    deleteButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            const sessionId = this.getAttribute('data-session-id');
            const sessionTitle = this.getAttribute('data-session-title');
            const isLibraryGame = this.getAttribute('data-is-library-game') === 'true';
            const gameName = this.getAttribute('data-game-name');
            
            // Update modal content
            if (sessionTitleSpan) sessionTitleSpan.textContent = sessionTitle;
            if (deleteForm) deleteForm.action = '/game-sessions/' + sessionId + '/delete';
            
            // Show/hide library game info
            if (isLibraryGame && gameName && gameNameSpan && libraryGameInfo) {
                gameNameSpan.textContent = gameName;
                libraryGameInfo.style.display = 'block';
            } else if (libraryGameInfo) {
                libraryGameInfo.style.display = 'none';
            }
            
            // Show modal
            const modalInstance = new bootstrap.Modal(deleteModal);
            modalInstance.show();
        });
    });
});