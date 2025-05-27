// scripts.js - Basic functionalities for the Reiunio application

// Function to initialize Bootstrap tooltips
document.addEventListener('DOMContentLoaded', function() {
    // Initialize all tooltips
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl)
    });

    // Automatically close alerts after 5 seconds (excluding persistent ones)
    setTimeout(function() {
        var alerts = document.querySelectorAll('.alert.alert-success:not(.persistent), .alert.alert-info:not(.persistent)');
        alerts.forEach(function(alert) {
            if (alert) {
                var bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            }
        });
    }, 5000);

    // Handle filtering for games/sessions/loans
    setupFilterForms();
});

// Configure filter forms
function setupFilterForms() {
    var filterForms = document.querySelectorAll('.filter-form');
    
    filterForms.forEach(function(form) {
        var resetBtn = form.querySelector('.reset-filters');
        if (resetBtn) {
            resetBtn.addEventListener('click', function(e) {
                e.preventDefault();
                var inputs = form.querySelectorAll('input, select');
                inputs.forEach(function(input) {
                    if (input.type === 'checkbox') {
                        input.checked = false;
                    } else {
                        input.value = '';
                    }
                });
                form.submit();
            });
        }
    });
}

// Function to confirm important actions
function confirmAction(message) {
    return confirm(message || 'Are you sure you want to perform this action?');
}

// Function to validate game session creation/edit form
function validateGameSessionForm() {
    var sessionDate = document.getElementById('date').value;
    var startTime = document.getElementById('startTime').value;
    var endTime = document.getElementById('endTime').value;
    
    // Convert to Date objects for comparison
    var currentDate = new Date();
    currentDate.setHours(0, 0, 0, 0); // Reset to midnight
    
    var sessionDateObj = new Date(sessionDate);
    
    // Validate that date is not in the past
    if (sessionDateObj < currentDate) {
        alert('The session date cannot be in the past.');
        return false;
    }
    
    // Validate that end time is after start time
    if (startTime >= endTime) {
        alert('The end time must be after the start time.');
        return false;
    }
    
    return true;
}

// Function to validate loan form
function validateLoanForm() {
    var returnDate = document.getElementById('estimatedReturnDate').value;
    var currentDate = new Date();
    var returnDateObj = new Date(returnDate);
    
    // Validate that return date is in the future
    if (returnDateObj <= currentDate) {
        alert('The estimated return date must be after today.');
        return false;
    }
    
    return true;
}

// Function to show current player count in a game session
function updatePlayerCounter(sessionId, maxPlayers) {
    var counter = document.getElementById('player-counter-' + sessionId);
    var joinButton = document.getElementById('join-button-' + sessionId);
    
    if (counter && joinButton) {
        var currentCount = parseInt(counter.textContent.split('/')[0]);
        if (currentCount >= maxPlayers) {
            joinButton.disabled = true;
            joinButton.title = 'This session is full';
        }
    }
}