// loans.js - JavaScript for loan form functionality - FIXED

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
	        <div class="text-center mb-3">
	            <img src="${game.imageUrl || '/defaults/game-placeholder.jpg'}" 
	                 alt="Photo of ${escapeHtml(game.name)}"
	                 class="img-fluid rounded"
	                 style="width: 100%; max-width: 200px; height: 150px; object-fit: cover; border: 2px solid #fed001; box-shadow: 0 4px 15px rgba(254, 208, 1, 0.3); border-radius: 8px;"
	                 onerror="this.src='/defaults/game-placeholder.jpg';">
	        </div>
	        <div style="background: rgba(254, 208, 1, 0.05); border: 1px solid rgba(254, 208, 1, 0.2); border-radius: 12px; padding: 1.5rem;">
	            <h5 style="color: #fed001; font-weight: 700; margin-bottom: 1rem; display: flex; align-items: center;">
	                <i class="bi bi-info-circle me-2"></i> Selected Game Information
	            </h5>
	            <h6 style="color: #ffffff; font-weight: 600; margin-bottom: 0.5rem;">${escapeHtml(game.name)}</h6>
	            ${game.description ? `<p style="color: rgba(255, 255, 255, 0.9); font-size: 0.95rem; margin-bottom: 1rem;">${escapeHtml(game.description)}</p>` : ''}
	            <ul style="list-style: none; padding: 0; margin: 0;">
	                <li style="color: #ffffff; display: flex; align-items: center; margin-bottom: 0.8rem;">
	                    <i class="bi bi-tag me-2" style="color: #fed001; min-width: 20px;"></i>
	                    <strong style="margin-right: 0.5rem;">Category:</strong>
	                    <span style="color: rgba(255, 255, 255, 0.9);">${escapeHtml(game.category || 'N/A')}</span>
	                </li>
	                <li style="color: #ffffff; display: flex; align-items: center; margin-bottom: 0.8rem;">
	                    <i class="bi bi-people me-2" style="color: #fed001; min-width: 20px;"></i>
	                    <strong style="margin-right: 0.5rem;">Players:</strong>
	                    <span style="color: rgba(255, 255, 255, 0.9);">${game.minPlayers}-${game.maxPlayers}</span>
	                </li>
	                <li style="color: #ffffff; display: flex; align-items: center; margin-bottom: 0.8rem;">
	                    <i class="bi bi-clock me-2" style="color: #fed001; min-width: 20px;"></i>
	                    <strong style="margin-right: 0.5rem;">Duration:</strong>
	                    <span style="color: rgba(255, 255, 255, 0.9);">${game.durationMinutes} minutes</span>
	                </li>
	                <li style="color: #ffffff; display: flex; align-items: center; margin-bottom: 0.8rem;">
	                    <i class="bi bi-star me-2" style="color: #fed001; min-width: 20px;"></i>
	                    <strong style="margin-right: 0.5rem;">State:</strong>
	                    <span class="badge ${getStateBadgeClass(game.state)}" style="font-weight: 600; border-radius: 6px; padding: 0.4rem 0.8rem;">${game.state}</span>
	                </li>
	            </ul>
	            <p style="margin-bottom: 0; margin-top: 1rem;"><small style="color: #00b894; font-weight: 600;">✓ Available in our library</small></p>
	        </div>
	    `;
	    
	    gamePreview.innerHTML = gameInfoHtml;
	    gamePreview.style.display = 'block';
	}

    // FIXED: Function to display game sessions information with persistent alerts
    function displayGameSessions(data) {
        console.log('Game sessions data:', data); // Debug log
        
        if (!data.upcomingSessions || data.upcomingSessions.length === 0) {
            gameSessionsInfo.innerHTML = `
                <div class="alert alert-success persistent">
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
		                        <small class="me-3" style="color: #ffffff;">
		                            <i class="bi bi-person"></i> Created by: ${escapeHtml(session.creatorName)}
		                        </small>
		                    </div>
		                </div>
		                <div class="text-end">
		                    <span class="badge bg-info text-dark">${escapeHtml(session.formattedDateRange)}</span>
		                    <br>
		                    <small style="color: #ffffff;">${escapeHtml(session.formattedTimeRange)}</small>
		                </div>
		            </div>
		        </div>
		    `;
		});

        // Debug log for conflict detection
        console.log('Has conflicts:', data.hasConflicts);
        console.log('Conflict message:', data.conflictMessage);

        sessionsHtml += `
                    </div>
                    
                    <div class="alert ${data.hasConflicts ? 'alert-warning' : 'alert-success'} mt-3 mb-0 persistent">
                        <i class="bi bi-${data.hasConflicts ? 'exclamation-triangle-fill' : 'check-circle'}"></i>
                        ${data.hasConflicts ? 
                            `<strong>Scheduling Conflict:</strong> There are upcoming sessions for this game that conflict with the loan period. 
                            We recommend returning the game by <strong>${formatDate(data.suggestedReturnDate)}</strong> 
                            to ensure it's available for scheduled sessions.
                            <br>
                            <small class="text-muted mt-1 d-block">${data.conflictMessage || 'The game will be needed for upcoming sessions.'}</small>` :
                            `<strong>✅ All Clear!</strong> No scheduling conflicts detected with the proposed return date. 
                            You can proceed with confidence!`
                        }
                    </div>
                </div>
            </div>
        `;

        gameSessionsInfo.innerHTML = sessionsHtml;
        
        // FIXED: Ensure the element stays visible and doesn't get auto-dismissed
        const alertElements = gameSessionsInfo.querySelectorAll('.alert.persistent');
        alertElements.forEach(alert => {
            // Remove any auto-dismiss functionality
            alert.classList.add('persistent');
            // Remove close button if exists
            const closeBtn = alert.querySelector('.btn-close');
            if (closeBtn) {
                closeBtn.remove();
            }
        });
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
            <div class="alert alert-danger persistent">
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