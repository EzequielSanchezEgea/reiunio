// Specific functionalities for the games section

document.addEventListener('DOMContentLoaded', function() {
    // Initialize functionalities for games section
    initGameFilters();
    initAvailabilityControl();
    validateGameForm();
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
                } else {
                    input.value = '';
                }
            });
            filterForm.submit();
        });
    }
    
    // Auto-submit when select changes
    const selectFilters = filterForm.querySelectorAll('select');
    selectFilters.forEach(select => {
        select.addEventListener('change', function() {
            filterForm.submit();
        });
    });
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
        const minPlayers = parseInt(document.getElementById('minPlayers').value);
        const maxPlayers = parseInt(document.getElementById('maxPlayers').value);
        
        if (minPlayers > maxPlayers) {
            alert('The minimum number of players cannot be greater than the maximum');
            valid = false;
        }
        
        // Validate that duration is positive
        const duration = parseInt(document.getElementById('durationMinutes').value);
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