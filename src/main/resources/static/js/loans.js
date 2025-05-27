// Specific functionalities for the loans section - CLEAN VERSION

document.addEventListener('DOMContentLoaded', function() {
    console.log('Loans.js loaded');
    initLoanFilters();
    initReturnConfirmation();
    validateLoanForm();
    initDateValidation();
    initDynamicLoanForm();
});

function initLoanFilters() {
    const filterForm = document.querySelector('.filter-form');
    if (!filterForm) return;
    
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
    
    const statusFilter = filterForm.querySelector('select[name="status"]');
    if (statusFilter) {
        statusFilter.addEventListener('change', function() {
            filterForm.submit();
        });
    }
}

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

function validateLoanForm() {
    const loanForm = document.querySelector('form[action*="/loans/new"]');
    if (!loanForm) return;
    
    loanForm.addEventListener('submit', function(e) {
        let valid = true;
        
        const userId = document.getElementById('userId');
        if (!userId || !userId.value) {
            alert('Please select a user for this loan');
            valid = false;
        }
        
        const gameId = document.getElementById('gameId');
        if (!gameId || !gameId.value) {
            alert('Please select a game for this loan');
            valid = false;
        }
        
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
        
        if (!valid) {
            e.preventDefault();
        }
    });
}

function initDateValidation() {
    const returnDateInput = document.getElementById('estimatedReturnDate');
    if (!returnDateInput) return;
    
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tomorrowISO = tomorrow.toISOString().split('T')[0];
    returnDateInput.setAttribute('min', tomorrowISO);
    
    if (!returnDateInput.value) {
        const oneWeekFromNow = new Date();
        oneWeekFromNow.setDate(oneWeekFromNow.getDate() + 7);
        const oneWeekISO = oneWeekFromNow.toISOString().split('T')[0];
        returnDateInput.value = oneWeekISO;
    }
    
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

function initDynamicLoanForm() {
    const gameSelect = document.getElementById('gameId');
    const userSelect = document.getElementById('userId');
    
    if (gameSelect) {
        gameSelect.addEventListener('change', handleGameSelection);
        
        if (gameSelect.value) {
            console.log('Pre-selected game detected:', gameSelect.value);
            handleGameSelection.call(gameSelect);
        }
    }
    
    if (userSelect) {
        userSelect.addEventListener('change', handleUserSelection);
    }
    
    initBootstrapValidation();
}

function handleGameSelection() {
    const gameId = this.value;
    
    console.log('Game selection changed to:', gameId);
    
    clearDynamicContent();
    
    if (!gameId) {
        resetReturnDateToDefault();
        return;
    }
    
    showLoadingIndicator();
    
    const url = `/loans/api/game-info/${gameId}`;
    console.log('Fetching from URL:', url);
    
    fetch(url, {
        method: 'GET',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        console.log('Response status:', response.status);
        
        if (!response.ok) {
            return response.text().then(text => {
                console.error('Error response:', text);
                throw new Error(`HTTP ${response.status}: ${text}`);
            });
        }
        return response.json();
    })
    .then(data => {
        console.log('Received data:', data);
        hideLoadingIndicator();
        
        if (data && typeof data === 'object') {
            displayGameInformation(data);
            updateReturnDateSuggestion(data);
        } else {
            console.error('Invalid data format received:', data);
            showErrorMessage('Invalid response format received from server.');
        }
    })
    .catch(error => {
        hideLoadingIndicator();
        console.error('Error fetching game information:', error);
        
        if (error.message.includes('404')) {
            showErrorMessage('Game information endpoint not found. Please check if the server is running correctly.');
        } else if (error.message.includes('403')) {
            showErrorMessage('You do not have permission to access this information.');
        } else if (error.message.includes('500')) {
            showErrorMessage('Server error occurred while loading game information.');
        } else {
            showErrorMessage(`Unable to load game information: ${error.message}. You can still proceed with the loan.`);
        }
    });
}

function handleUserSelection() {
    const userId = this.value;
    const userPreview = document.getElementById('userPreview');
    const userInfo = document.getElementById('userInfo');
    
    if (userId && userPreview && userInfo) {
        const selectedUser = this.options[this.selectedIndex];
        const userText = selectedUser.text;
        const userName = userText.split(' (@')[0];
        const username = userText.split(' (@')[1]?.replace(')', '') || '';
        
        userPreview.style.display = 'block';
        userInfo.innerHTML = `
            <h5>${userName}</h5>
            <p><strong>Username:</strong> @${username}</p>
            <p><small class="text-muted">Selected user for this loan</small></p>
        `;
    } else if (userPreview) {
        userPreview.style.display = 'none';
    }
}

function clearDynamicContent() {
    const gameSessionsInfo = document.getElementById('gameSessionsInfo');
    const gamePreview = document.getElementById('gamePreview');
    
    if (gameSessionsInfo) {
        gameSessionsInfo.innerHTML = '';
    }
    
    if (gamePreview) {
        gamePreview.innerHTML = '';
    }
    
    const loading = document.getElementById('loadingIndicator');
    const error = document.getElementById('errorMessage');
    
    if (loading) loading.remove();
    if (error) error.remove();
}

function showLoadingIndicator() {
    const container = document.getElementById('gameSessionsInfo') || document.querySelector('.card-body');
    const loadingDiv = document.createElement('div');
    loadingDiv.id = 'loadingIndicator';
    loadingDiv.className = 'text-center my-3';
    loadingDiv.innerHTML = `
        <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Loading game information...</span>
        </div>
        <p class="text-muted mt-2">Checking for upcoming sessions...</p>
    `;
    
    if (container) {
        container.appendChild(loadingDiv);
    }
}

function hideLoadingIndicator() {
    const loading = document.getElementById('loadingIndicator');
    if (loading) {
        loading.remove();
    }
}

function showErrorMessage(message) {
    const container = document.getElementById('gameSessionsInfo') || document.querySelector('.card-body');
    const errorDiv = document.createElement('div');
    errorDiv.id = 'errorMessage';
    errorDiv.className = 'alert alert-info';
    errorDiv.innerHTML = `
        <i class="bi bi-info-circle"></i>
        <strong>Notice:</strong> ${message}
    `;
    
    if (container) {
        container.appendChild(errorDiv);
    }
}

function resetReturnDateToDefault() {
    const returnDateInput = document.getElementById('estimatedReturnDate');
    if (returnDateInput) {
        const oneWeekFromNow = new Date();
        oneWeekFromNow.setDate(oneWeekFromNow.getDate() + 7);
        const oneWeekISO = oneWeekFromNow.toISOString().split('T')[0];
        returnDateInput.value = oneWeekISO;
        console.log('Reset return date to:', oneWeekISO);
    }
}

function displayGameInformation(data) {
    console.log('Displaying game information:', data);
    
    if (!data.success) {
        showErrorMessage(data.message);
        return;
    }
    
    const game = data.game;
    const upcomingSessions = data.upcomingSessions || [];
    const hasConflicts = data.hasConflicts;
    
    updateGamePreview(game);
    updateSessionsInformation(game, upcomingSessions, hasConflicts, data.suggestedReturnDate);
}

function updateGamePreview(game) {
    const gamePreview = document.getElementById('gamePreview');
    if (!gamePreview || !game) return;
    
    gamePreview.innerHTML = `
        <div class="card bg-light mb-3">
            <div class="card-header">
                <h6 class="mb-0"><i class="bi bi-info-circle"></i> Selected Game Information</h6>
            </div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-8">
                        <div>
                            <h5>${game.name}</h5>
                            ${game.description ? `<p>${game.description}</p>` : ''}
                            <ul class="list-unstyled">
                                <li><i class="bi bi-tag"></i> Category: <span>${game.category || 'Not specified'}</span></li>
                                <li><i class="bi bi-people"></i> Players: <span>${game.minPlayers}-${game.maxPlayers}</span></li>
                                <li><i class="bi bi-clock"></i> Duration: <span>${game.durationMinutes}</span> minutes</li>
                                <li><i class="bi bi-star"></i> State: 
                                    <span class="badge ${getStateBadgeClass(game.state)}">${game.state}</span>
                                </li>
                            </ul>
                        </div>
                    </div>
                    <div class="col-md-4 text-center">
                        <img src="${game.imageUrl || '/defaults/game-placeholder.jpg'}" 
                             alt="Photo of ${game.name}"
                             class="img-fluid rounded shadow-sm"
                             style="height: 120px; width: 120px; object-fit: cover; border: 2px solid #0d6efd;"
                             onerror="this.src='/defaults/game-placeholder.jpg';">
                    </div>
                </div>
            </div>
        </div>
    `;
}

function updateSessionsInformation(game, upcomingSessions, hasConflicts, suggestedReturnDate) {
    const gameSessionsInfo = document.getElementById('gameSessionsInfo');
    if (!gameSessionsInfo || !game) return;
    
    let sessionsHtml = '<div class="mb-4">';
    
    if (upcomingSessions && upcomingSessions.length > 0) {
        sessionsHtml += `
            <div class="card border-info bg-info bg-opacity-10 mb-3">
                <div class="card-header bg-info bg-opacity-25 border-info">
                    <h6 class="mb-0 text-info-emphasis">
                        <i class="bi bi-calendar-event"></i> Upcoming Game Sessions
                    </h6>
                </div>
                <div class="card-body">
                    <p class="mb-2 text-info-emphasis">
                        <strong>Information:</strong> The game "${game.name}" 
                        has ${upcomingSessions.length} upcoming session(s):
                    </p>
                    
                    <div class="list-group list-group-flush">
        `;
        
        upcomingSessions.forEach(session => {
            sessionsHtml += `
                <div class="list-group-item bg-transparent border-info">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <h6 class="mb-1">${session.sessionTitle}</h6>
                            <div class="d-flex align-items-center">
                                <small class="text-muted me-3">
                                    <i class="bi bi-person"></i> Created by: ${session.creatorName}
                                </small>
                            </div>
                        </div>
                        <div class="text-end">
                            <span class="badge bg-info text-dark">${session.formattedDateRange}</span>
                            <br>
                            <small class="text-muted">${session.formattedTimeRange}</small>
                        </div>
                    </div>
                </div>
            `;
        });
        
        sessionsHtml += `
                    </div>
                    
                    ${hasConflicts ? `
                        <div class="alert alert-warning mt-3 mb-0">
                            <i class="bi bi-lightbulb-fill"></i>
                            <strong>💡 Smart Suggestion:</strong> Based on the scheduled sessions, we recommend returning the game by 
                            <strong>${formatDate(suggestedReturnDate)}</strong> 
                            to avoid any scheduling conflicts.
                            <br>
                            <small class="text-muted">You can still proceed with your preferred date if needed!</small>
                        </div>
                    ` : `
                        <div class="alert alert-success mt-3 mb-0">
                            <i class="bi bi-check-circle"></i>
                            <strong>✅ All Good!</strong> Your proposed return date doesn't conflict with any scheduled sessions. 
                            You can proceed with confidence!
                        </div>
                    `}
                </div>
            </div>
        `;
    } else {
        sessionsHtml += `
            <div class="alert alert-success">
                <i class="bi bi-check-circle"></i>
                <strong>Perfect!</strong> No upcoming sessions are scheduled for this game. You have complete flexibility with the return date.
            </div>
        `;
    }
    
    sessionsHtml += '</div>';
    gameSessionsInfo.innerHTML = sessionsHtml;
}

function updateReturnDateSuggestion(data) {
    if (data.success && data.suggestedReturnDate) {
        const returnDateInput = document.getElementById('estimatedReturnDate');
        if (returnDateInput) {
            if (data.hasConflicts) {
                console.log('Updating return date due to conflicts:', data.suggestedReturnDate);
                returnDateInput.value = data.suggestedReturnDate;
            }
        }
    }
}

function getStateBadgeClass(state) {
    switch(state) {
        case 'NEW': return 'bg-success';
        case 'GOOD': return 'bg-info';
        case 'ACCEPTABLE': return 'bg-warning text-dark';
        case 'DAMAGED': return 'bg-danger';
        default: return 'bg-secondary';
    }
}

function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-GB');
}

function initBootstrapValidation() {
    const forms = document.querySelectorAll('.needs-validation');
    
    Array.prototype.slice.call(forms).forEach(function (form) {
        form.addEventListener('submit', function (event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            
            form.classList.add('was-validated');
        }, false);
    });
}

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
        
        statusBadge.classList.add('status-change');
        setTimeout(() => {
            statusBadge.classList.remove('status-change');
        }, 500);
    }
}

function highlightOverdueLoans() {
    const loanRows = document.querySelectorAll('.table-loans tbody tr');
    
    loanRows.forEach(row => {
        const statusCell = row.querySelector('.badge');
        const returnDateCell = row.cells[3];
        
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

document.addEventListener('DOMContentLoaded', function() {
    highlightOverdueLoans();
    
    const quickSearchInput = document.getElementById('loanQuickSearch');
    if (quickSearchInput) {
        quickSearchInput.addEventListener('keyup', function() {
            const filter = this.value.toUpperCase();
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
        });
    }
});