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
        
        // Get selected game information
        const selectedOption = gameIdSelect.options[gameIdSelect.selectedIndex];
        if (selectedOption && selectedOption.value) {
            updateGameInfo(selectedOption);
            loadGameImage(selectedOption.value);
        }
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
        
        // Update game info and image
        const selectedGame = this.options[this.selectedIndex];
        updateGameInfo(selectedGame);
        loadGameImage(gameId);
        
        console.log('Showing library game preview for:', selectedGame.text.split(' (')[0]);
    } else {
        // Show personal game section
        if (gamePreview) gamePreview.style.display = 'none';
        if (personalGameImageSection) personalGameImageSection.style.display = 'block';
        console.log('Showing personal game section');
    }
}

// Function to update game information in preview
function updateGameInfo(selectedOption) {
    const gameInfo = document.getElementById('gameInfo');
    if (!gameInfo || !selectedOption) return;
    
    const gameText = selectedOption.text;
    const gameName = gameText.split(' (')[0];
    const gameDetails = gameText.split(' (')[1]?.replace(')', '') || '';
    
    gameInfo.innerHTML = `
        <h5>${gameName}</h5>
        <p><strong>Details:</strong> ${gameDetails}</p>
        <p><small class="text-success">✓ Available in our library</small></p>
    `;
}

// Function to load game image
function loadGameImage(gameId) {
    const gamePreviewImage = document.getElementById('gamePreviewImage');
    if (!gamePreviewImage) return;
    
    // Try to load the actual game image
    // You can adjust this URL pattern based on your image serving setup
    const gameImageUrl = `/uploads/games/game_${gameId}.jpg`; // Adjust as needed
    
    gamePreviewImage.src = gameImageUrl;
    gamePreviewImage.onerror = function() {
        this.src = '/defaults/game-placeholder.jpg';
    };
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
        const reader = new FileReader();
        reader.onload = function(e) {
            preview.src = e.target.result;
            previewSection.style.display = 'block';
            fileInputSection.style.display = 'none';
        };
        reader.readAsDataURL(file);
    }
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

// Function to initialize date validation
function initDateValidation() {
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');
    const endTimeInput = document.getElementById('endTime');
    
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
    
    // Add event listeners
    startDateInput.addEventListener('change', handleStartDateChange);
    endDateInput.addEventListener('change', toggleEndTimeRequirement);
    
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
            formText.innerHTML = '<strong>Required</strong> for same-day sessions.';
        }
    } else {
        endTimeInput.removeAttribute('required');
        if (formText) {
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
    const startDate = document.getElementById('startDate')?.value;
    const endDate = document.getElementById('endDate')?.value;
    const startTime = document.getElementById('startTime')?.value;
    const endTime = document.getElementById('endTime')?.value;
    const customGameName = document.getElementById('customGameName')?.value;
    
    // Clear previous custom validation
    const fields = ['startDate', 'endDate', 'endTime', 'customGameName'];
    fields.forEach(fieldId => {
        const field = document.getElementById(fieldId);
        if (field) field.setCustomValidity('');
    });
    
    // Validate custom game name
    if (!customGameName || customGameName.trim() === '') {
        const field = document.getElementById('customGameName');
        if (field) field.setCustomValidity('Game name is required.');
        valid = false;
    }
    
    // Validate start date is not in the past (only for new sessions)
    if (!isEditing && startDate) {
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const startDateObj = new Date(startDate);
        
        if (startDateObj < today) {
            const field = document.getElementById('startDate');
            if (field) field.setCustomValidity('The start date cannot be in the past.');
            valid = false;
        }
    }
    
    // Validate end date is not before start date
    if (startDate && endDate && endDate < startDate) {
        const field = document.getElementById('endDate');
        if (field) field.setCustomValidity('End date cannot be before start date.');
        valid = false;
    }
    
    // Validate times for same-day sessions
    if (startDate && endDate && startDate === endDate) {
        if (!endTime) {
            const field = document.getElementById('endTime');
            if (field) field.setCustomValidity('End time is required for same-day sessions.');
            valid = false;
        } else if (startTime && endTime && endTime <= startTime) {
            const field = document.getElementById('endTime');
            if (field) field.setCustomValidity('End time must be after start time for same-day sessions.');
            valid = false;
        }
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