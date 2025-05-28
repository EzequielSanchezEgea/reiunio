// loans.js - JavaScript for loan form functionality

document.addEventListener('DOMContentLoaded', function() {
    const gameSelect = document.getElementById('gameId');
    const userSelect = document.getElementById('userId');
    const returnDateInput = document.getElementById('estimatedReturnDate');
    const gamePreview = document.getElementById('gamePreview');
    const userPreview = document.getElementById('userPreview');
    const gameSessionsInfo = document.getElementById('gameSessionsInfo');

    // Initialize with pre-selected values if any
    if (gameSelect && gameSelect.value) {
        loadGameInfo(gameSelect.value);
    }
    
    if (userSelect && userSelect.value) {
        loadUserInfo(userSelect.value);
    }

    // Game selection change handler
    if (gameSelect) {
        gameSelect.addEventListener('change', function() {
            const gameId = this.value;
            if (gameId) {
                loadGameInfo(gameId);
            } else {
                hideGameInfo();
            }
        });
    }

    // User selection change handler - Enhanced for photo loading
    if (userSelect) {
        userSelect.addEventListener('change', function() {
            const userId = this.value;
            if (userId) {
                loadUserInfoEnhanced(userId);
            } else {
                hideUserInfo();
            }
        });
    }

    // Enhanced function to load user information with photo
    function loadUserInfoEnhanced(userId) {
        if (!userId) return;

        // Find user in the select options
        const selectedOption = userSelect.querySelector(`option[value="${userId}"]`);
        if (selectedOption) {
            const userText = selectedOption.textContent;
            const photoUrl = selectedOption.getAttribute('data-photo-url');
            const email = selectedOption.getAttribute('data-email');
            const role = selectedOption.getAttribute('data-role');
            
            // Parse the text: "First Last (@username)"
            const match = userText.match(/^(.+?)\s+\(@(.+)\)$/);
            if (match) {
                const fullName = match[1];
                const username = match[2];
                displayUserInfoEnhanced(fullName, username, email, role, photoUrl);
            }
        }
    }

    // Enhanced function to display user information with photo
    function displayUserInfoEnhanced(fullName, username, email, role, photoUrl) {
        const userInfoHtml = `
            <div>
                <h5>${escapeHtml(fullName)}</h5>
                <ul class="list-unstyled">
                    <li><i class="bi bi-person-circle"></i> Username: @${escapeHtml(username)}</li>
                    <li><i class="bi bi-envelope"></i> Email: ${escapeHtml(email || 'N/A')}</li>
                    <li><i class="bi bi-shield"></i> Role: 
                        <span class="badge ${getRoleBadgeClass(role)}">${escapeHtml(role || 'USER')}</span>
                    </li>
                </ul>
            </div>
        `;
        
        const userInfoContainer = document.getElementById('userInfo');
        if (userInfoContainer) {
            userInfoContainer.innerHTML = userInfoHtml;
        }
        
        // Update user image with actual photo
        const userImage = document.getElementById('userPreviewImage');
        if (userImage) {
            if (photoUrl && photoUrl !== '/defaults/user-placeholder.jpg') {
                userImage.src = photoUrl;
                userImage.alt = `Profile photo of ${fullName}`;
            } else {
                userImage.src = '/defaults/user-placeholder.jpg';
                userImage.alt = `Default photo for ${fullName}`;
            }
        }
        
        userPreview.style.display = 'block';
    }

    // Function to load game information via AJAX
    function loadGameInfo(gameId) {
        if (!gameId) return;

        // Show loading state
        showGameLoading();

        fetch(`/loans/api/game-info/${gameId}`)
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    displayGameInfo(data);
                    displayGameSessions(data);
                    
                    // Update return date if there's a suggested date
                    if (data.suggestedReturnDate && returnDateInput) {
                        returnDateInput.value = data.suggestedReturnDate;
                    }
                } else {
                    showGameError(data.message);
                }
            })
            .catch(error => {
                console.error('Error loading game info:', error);
                showGameError('Error loading game information. Please try again.');
            });
    }

    // Function to display game information
    function displayGameInfo(data) {
        const game = data.game;
        
        const gameInfoHtml = `
            <div class="card bg-light mb-3">
                <div class="card-header">
                    <h6 class="mb-0"><i class="bi bi-info-circle"></i> Selected Game Information</h6>
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-8">
                            <div>
                                <h5>${escapeHtml(game.name)}</h5>
                                ${game.description ? `<p>${escapeHtml(game.description)}</p>` : ''}
                                <ul class="list-unstyled">
                                    <li><i class="bi bi-tag"></i> Category: ${escapeHtml(game.category || 'N/A')}</li>
                                    <li><i class="bi bi-people"></i> Players: ${game.minPlayers}-${game.maxPlayers}</li>
                                    <li><i class="bi bi-clock"></i> Duration: ${game.durationMinutes} minutes</li>
                                    <li><i class="bi bi-star"></i> State: 
                                        <span class="badge ${getStateBadgeClass(game.state)}">${game.state}</span>
                                    </li>
                                </ul>
                            </div>
                        </div>
                        <div class="col-md-4 text-center">
                            <img src="${game.imageUrl || '/defaults/game-placeholder.jpg'}" 
                                 alt="Photo of ${escapeHtml(game.name)}"
                                 class="img-fluid rounded shadow-sm"
                                 style="height: 120px; width: 120px; object-fit: cover; border: 2px solid #0d6efd;"
                                 onerror="this.src='/defaults/game-placeholder.jpg';">
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        gamePreview.innerHTML = gameInfoHtml;
        gamePreview.style.display = 'block';
    }

    // Function to display game sessions information
    function displayGameSessions(data) {
        if (!data.upcomingSessions || data.upcomingSessions.length === 0) {
            gameSessionsInfo.innerHTML = `
                <div class="alert alert-success">
                    <i class="bi bi-check-circle"></i>
                    <strong>Perfect!</strong> No upcoming sessions are scheduled for this game. 
                    You have complete flexibility with the return date.
                </div>
            `;
            return;
        }

        let sessionsHtml = `
            <div class="card border-info bg-info bg-opacity-10 mb-3">
                <div class="card-header bg-info bg-opacity-25 border-info">
                    <h6 class="mb-0 text-info-emphasis">
                        <i class="bi bi-calendar-event"></i> Upcoming Game Sessions
                    </h6>
                </div>
                <div class="card-body">
                    <p class="mb-2 text-info-emphasis">
                        <strong>Information:</strong> The game "${escapeHtml(data.game.name)}" 
                        has ${data.upcomingSessions.length} upcoming session(s):
                    </p>
                    
                    <div class="list-group list-group-flush">
        `;

        data.upcomingSessions.forEach(session => {
            sessionsHtml += `
                <div class="list-group-item bg-transparent border-info">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <h6 class="mb-1">${escapeHtml(session.sessionTitle)}</h6>
                            <div class="d-flex align-items-center">
                                <small class="text-muted me-3">
                                    <i class="bi bi-person"></i> Created by: ${escapeHtml(session.creatorName)}
                                </small>
                            </div>
                        </div>
                        <div class="text-end">
                            <span class="badge bg-info text-dark">${escapeHtml(session.formattedDateRange)}</span>
                            <br>
                            <small class="text-muted">${escapeHtml(session.formattedTimeRange)}</small>
                        </div>
                    </div>
                </div>
            `;
        });

        sessionsHtml += `
                    </div>
                    
                    <div class="alert ${data.hasConflicts ? 'alert-warning' : 'alert-success'} mt-3 mb-0">
                        <i class="bi bi-${data.hasConflicts ? 'lightbulb-fill' : 'check-circle'}"></i>
                        ${data.hasConflicts ? 
                            `<strong>💡 Smart Suggestion:</strong> Based on the scheduled sessions, we recommend returning the game by 
                            <strong>${formatDate(data.suggestedReturnDate)}</strong> 
                            to avoid any scheduling conflicts.
                            <br>
                            <small class="text-muted">You can still proceed with your preferred date if needed!</small>` :
                            `<strong>✅ All Good!</strong> Your proposed return date doesn't conflict with any scheduled sessions. 
                            You can proceed with confidence!`
                        }
                    </div>
                </div>
            </div>
        `;

        gameSessionsInfo.innerHTML = sessionsHtml;
    }

    // Helper functions
    function showGameLoading() {
        gamePreview.innerHTML = `
            <div class="card bg-light mb-3">
                <div class="card-body text-center">
                    <div class="spinner-border text-primary" role="status">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                    <p class="mt-2 text-muted">Loading game information...</p>
                </div>
            </div>
        `;
        gamePreview.style.display = 'block';
    }

    function showGameError(message) {
        gamePreview.innerHTML = `
            <div class="alert alert-danger">
                <i class="bi bi-exclamation-triangle"></i>
                ${escapeHtml(message)}
            </div>
        `;
        gamePreview.style.display = 'block';
    }

    function hideGameInfo() {
        if (gamePreview) {
            gamePreview.style.display = 'none';
            gamePreview.innerHTML = '';
        }
        if (gameSessionsInfo) {
            gameSessionsInfo.innerHTML = '';
        }
    }

    function hideUserInfo() {
        if (userPreview) {
            userPreview.style.display = 'none';
        }
    }

    function escapeHtml(text) {
        if (!text) return '';
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return text.replace(/[&<>"']/g, function(m) { return map[m]; });
    }

    function getStateBadgeClass(state) {
        const stateClasses = {
            'NEW': 'bg-success',
            'GOOD': 'bg-info',
            'ACCEPTABLE': 'bg-warning text-dark',
            'DAMAGED': 'bg-danger'
        };
        return stateClasses[state] || 'bg-secondary';
    }

    function getRoleBadgeClass(role) {
        const roleClasses = {
            'ADMIN': 'bg-danger',
            'EXTENDED_USER': 'bg-warning text-dark',
            'BASIC_USER': 'bg-info'
        };
        return roleClasses[role] || 'bg-secondary';
    }

    function formatDate(dateString) {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleDateString('en-GB'); // DD/MM/YYYY format
    }

    // Form validation
    const form = document.querySelector('.needs-validation');
    if (form) {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    }

    // Date validation
    if (returnDateInput) {
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
});