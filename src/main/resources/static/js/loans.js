// Specific functionalities for the loans section

document.addEventListener('DOMContentLoaded', function() {
    // Initialize functionalities for loans section
    initLoanFilters();
    initReturnConfirmation();
    validateLoanForm();
    initDateValidation();
});

// Function to initialize loan filters
function initLoanFilters() {
    const filterForm = document.querySelector('.filter-form');
    if (!filterForm) return;
    
    // Reset all filters
    const resetButton = filterForm.querySelector('.reset-filters');
    if (resetButton) {
        resetButton.addEventListener('click', function(e) {
            e.preventDefault();
            const inputs = filterForm.querySelectorAll('input, select');
            inputs.forEach(input => {
                input.value = '';
            });
            filterForm.submit();
        });
    }
    
    // Auto-submit when status filter changes
    const statusFilter = filterForm.querySelector('select[name="status"]');
    if (statusFilter) {
        statusFilter.addEventListener('change', function() {
            filterForm.submit();
        });
    }
}

// Function for return confirmation
function initReturnConfirmation() {
    const returnButtons = document.querySelectorAll('form[action*="/return"] button[type="submit"]');
    
    returnButtons.forEach(button => {
        const form = button.closest('form');
        if (form) {
            form.addEventListener('submit', function(e) {
                if (!confirm('Are you sure you want to register this return?')) {
                    e.preventDefault();
                }
            });
        }
    });
}

// Function to validate the loan form
function validateLoanForm() {
    const loanForm = document.querySelector('form[action*="/loans/new"]');
    if (!loanForm) return;
    
    loanForm.addEventListener('submit', function(e) {
        let valid = true;
        
        // Validate that user is selected
        const userId = document.getElementById('userId');
        if (!userId || !userId.value) {
            alert('Please select a user for this loan');
            valid = false;
        }
        
        // Validate that game is selected
        const gameId = document.getElementById('gameId');
        if (!gameId || !gameId.value) {
            alert('Please select a game for this loan');
            valid = false;
        }
        
        // Validate return date
        const returnDate = document.getElementById('estimatedReturnDate');
        if (returnDate && returnDate.value) {
            const selectedDate = new Date(returnDate.value);
            const today = new Date();
            today.setHours(0, 0, 0, 0);
            
            if (selectedDate <= today) {
                alert('Return date must be after today');
                valid = false;
            }
        }
        
        // Prevent form submission if there are errors
        if (!valid) {
            e.preventDefault();
        }
    });
}

// Function to handle date validation
function initDateValidation() {
    const returnDateInput = document.getElementById('estimatedReturnDate');
    if (!returnDateInput) return;
    
    // Set minimum date to tomorrow
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tomorrowISO = tomorrow.toISOString().split('T')[0];
    returnDateInput.setAttribute('min', tomorrowISO);
    
    // Set default value to tomorrow if empty
    if (!returnDateInput.value) {
        returnDateInput.value = tomorrowISO;
    }
    
    // Validate on change
    returnDateInput.addEventListener('change', function() {
        const selectedDate = new Date(this.value);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        
        if (selectedDate <= today) {
            this.setCustomValidity('Return date must be after today');
        } else {
            this.setCustomValidity('');
        }
    });
}

// Function to update loan status visually
function updateLoanStatus(loanId, status) {
    const statusBadge = document.querySelector(`#status-${loanId}`);
    if (statusBadge) {
        statusBadge.textContent = status;
        statusBadge.className = 'badge ';
        
        switch(status.toUpperCase()) {
            case 'ACTIVE':
                statusBadge.classList.add('bg-primary');
                break;
            case 'RETURNED':
                statusBadge.classList.add('bg-success');
                break;
            case 'LATE':
                statusBadge.classList.add('bg-danger');
                break;
            default:
                statusBadge.classList.add('bg-secondary');
        }
        
        // Add animation
        statusBadge.classList.add('status-change');
        setTimeout(() => {
            statusBadge.classList.remove('status-change');
        }, 500);
    }
}

// Function to calculate and display overdue days
function calculateOverdueDays(loanDate, estimatedReturn) {
    const today = new Date();
    const returnDate = new Date(estimatedReturn);
    
    if (today > returnDate) {
        const diffTime = Math.abs(today - returnDate);
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        return diffDays;
    }
    
    return 0;
}

// Function to highlight overdue loans
function highlightOverdueLoans() {
    const loanRows = document.querySelectorAll('.table-loans tbody tr');
    
    loanRows.forEach(row => {
        const statusCell = row.querySelector('.badge');
        const returnDateCell = row.cells[3]; // Expected return date column
        
        if (statusCell && statusCell.textContent.toUpperCase() === 'ACTIVE' && returnDateCell) {
            const returnDateText = returnDateCell.textContent.trim();
            const returnDate = new Date(returnDateText.split('/').reverse().join('-'));
            const today = new Date();
            
            if (today > returnDate) {
                row.classList.add('table-warning');
                returnDateCell.classList.add('text-danger', 'fw-bold');
            }
        }
    });
}

// Function for quick search in loans table
function quickSearchLoans() {
    const input = document.getElementById('loanQuickSearch');
    if (!input) return;
    
    const filter = input.value.toUpperCase();
    const table = document.querySelector('.table-loans');
    
    if (!table) return;
    
    const rows = table.querySelectorAll('tbody tr');
    
    rows.forEach(row => {
        const gameColumn = row.cells[0];
        const userColumn = row.cells[1];
        
        if (gameColumn && userColumn) {
            const gameText = gameColumn.textContent || gameColumn.innerText;
            const userText = userColumn.textContent || userColumn.innerText;
            
            if (gameText.toUpperCase().indexOf(filter) > -1 || 
                userText.toUpperCase().indexOf(filter) > -1) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        }
    });
}

// Function to show loan details in modal (for future enhancement)
function showLoanDetails(loanId) {
    // This could implement a modal that quickly shows loan details
    // via an AJAX request without navigating to the details page
    fetch(`/api/loans/${loanId}`)
        .then(response => response.json())
        .then(data => {
            // Update modal content with loan data
            const modal = document.getElementById('loanQuickViewModal');
            if (modal) {
                modal.querySelector('.modal-title').textContent = `Loan #${data.id}`;
                modal.querySelector('.modal-body').innerHTML = `
                    <div class="row">
                        <div class="col-6">
                            <p><strong>Game:</strong> ${data.game.name}</p>
                            <p><strong>User:</strong> ${data.user.firstName} ${data.user.lastName}</p>
                        </div>
                        <div class="col-6">
                            <p><strong>Status:</strong> 
                                <span class="badge bg-${data.status === 'ACTIVE' ? 'primary' : 
                                                      data.status === 'RETURNED' ? 'success' : 'danger'}">${data.status}</span>
                            </p>
                            <p><strong>Expected Return:</strong> ${data.estimatedReturnDate}</p>
                        </div>
                    </div>
                `;
                
                // Show modal
                const bsModal = new bootstrap.Modal(modal);
                bsModal.show();
            }
        })
        .catch(error => console.error('Error loading loan details:', error));
}

// Initialize additional features when page loads
document.addEventListener('DOMContentLoaded', function() {
    // Highlight overdue loans on page load
    highlightOverdueLoans();
    
    // Add quick search if input exists
    const quickSearchInput = document.getElementById('loanQuickSearch');
    if (quickSearchInput) {
        quickSearchInput.addEventListener('keyup', quickSearchLoans);
    }
});