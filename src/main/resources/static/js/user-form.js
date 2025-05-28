// User Form Validation - Simple and functional

document.addEventListener('DOMContentLoaded', function() {
    
    // Get form elements
    const usernameInput = document.getElementById('username');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const form = document.getElementById('userForm');
    
    // Get current user ID for edit mode
    const isEditing = form.action.includes('/edit');
    const currentUserId = isEditing ? form.action.split('/')[form.action.split('/').length - 2] : null;
    
    // Debounce function
    function debounce(func, wait) {
        let timeout;
        return function(...args) {
            clearTimeout(timeout);
            timeout = setTimeout(() => func.apply(this, args), wait);
        };
    }
    
    // Username validation
    if (usernameInput && !usernameInput.readOnly) {
        const validateUsername = debounce(async function() {
            const username = usernameInput.value.trim();
            
            if (username.length < 3) return;
            
            try {
                const url = `/api/users/check-username?username=${encodeURIComponent(username)}${currentUserId ? '&excludeId=' + currentUserId : ''}`;
                const response = await fetch(url);
                const data = await response.json();
                
                if (!data.available) {
                    showError(usernameInput, 'Username is already taken');
                } else {
                    showValid(usernameInput);
                }
            } catch (error) {
                console.error('Error checking username:', error);
            }
        }, 500);
        
        usernameInput.addEventListener('input', validateUsername);
    }
    
    // Email validation
    if (emailInput) {
        const validateEmail = debounce(async function() {
            const email = emailInput.value.trim();
            
            if (!email || !email.includes('@')) return;
            
            try {
                const url = `/api/users/check-email?email=${encodeURIComponent(email)}${currentUserId ? '&excludeId=' + currentUserId : ''}`;
                const response = await fetch(url);
                const data = await response.json();
                
                if (!data.available) {
                    showError(emailInput, 'Email is already registered');
                } else {
                    showValid(emailInput);
                }
            } catch (error) {
                console.error('Error checking email:', error);
            }
        }, 500);
        
        emailInput.addEventListener('input', validateEmail);
    }
    
    // Password validation
    if (passwordInput) {
        passwordInput.addEventListener('input', function() {
            const password = this.value;
            
            if (!password) {
                clearValidation(this);
                return;
            }
            
            // Simple validation: 8+ chars, letters, and numbers/symbols
            const hasLength = password.length >= 8;
            const hasLetter = /[a-zA-Z]/.test(password);
            const hasNumberOrSymbol = /[\d!@#$%^&*(),.?":{}|<>]/.test(password);
            
            if (hasLength && hasLetter && hasNumberOrSymbol) {
                showValid(this);
            } else {
                showError(this, 'Password must be 8+ characters with letters and numbers/symbols');
            }
            
            // Check confirm password
            if (confirmPasswordInput && confirmPasswordInput.value) {
                validatePasswordMatch();
            }
        });
    }
    
    // Confirm password validation
    if (confirmPasswordInput) {
        confirmPasswordInput.addEventListener('input', validatePasswordMatch);
    }
    
    function validatePasswordMatch() {
        if (!passwordInput || !confirmPasswordInput) return;
        
        const password = passwordInput.value;
        const confirmPassword = confirmPasswordInput.value;
        
        if (!confirmPassword) {
            clearValidation(confirmPasswordInput);
            return;
        }
        
        if (password === confirmPassword) {
            showValid(confirmPasswordInput);
        } else {
            showError(confirmPasswordInput, 'Passwords do not match');
        }
    }
    
    // Helper functions
    function showError(input, message) {
        input.classList.remove('is-valid');
        input.classList.add('is-invalid');
        
        // Update error message
        let feedback = input.parentNode.querySelector('.invalid-feedback');
        if (!feedback) {
            feedback = input.nextElementSibling;
            if (!feedback || !feedback.classList.contains('invalid-feedback')) {
                feedback = document.createElement('div');
                feedback.className = 'invalid-feedback';
                input.parentNode.appendChild(feedback);
            }
        }
        if (feedback) {
            feedback.textContent = message;
        }
        
        input.setCustomValidity(message);
    }
    
    function showValid(input) {
        input.classList.remove('is-invalid');
        input.classList.add('is-valid');
        input.setCustomValidity('');
    }
    
    function clearValidation(input) {
        input.classList.remove('is-valid', 'is-invalid');
        input.setCustomValidity('');
    }
    
    // Form submission
    form.addEventListener('submit', function(e) {
        // Final validation
        if (confirmPasswordInput && passwordInput) {
            if (passwordInput.value !== confirmPasswordInput.value) {
                e.preventDefault();
                showError(confirmPasswordInput, 'Passwords do not match');
                return false;
            }
        }
        
        // Check for any invalid fields
        const invalidFields = form.querySelectorAll('.is-invalid');
        if (invalidFields.length > 0) {
            e.preventDefault();
            return false;
        }
    });
    
    // Photo upload functionality
    const showUploadBtn = document.querySelector('.show-upload');
    const cancelUploadBtn = document.querySelector('.cancel-upload');
    const uploadForm = document.querySelector('.photo-upload-form');
    const photoActions = document.querySelector('.photo-actions');

    if (showUploadBtn && uploadForm && photoActions) {
        showUploadBtn.addEventListener('click', function() {
            uploadForm.style.display = 'block';
            photoActions.style.display = 'none';
        });

        cancelUploadBtn.addEventListener('click', function() {
            uploadForm.style.display = 'none';
            photoActions.style.display = 'block';
            uploadForm.querySelector('input[type="file"]').value = '';
        });
    }
    
    // Role description
    const roleSelect = document.getElementById('role');
    const roleDescription = document.getElementById('roleDescription');
    
    if (roleSelect && roleDescription) {
        roleSelect.addEventListener('change', function() {
            const descriptions = {
                'ADMIN': '<strong class="text-danger"><i class="bi bi-shield-fill"></i> Administrator</strong><br><small>Full system access, can manage all users, games, and settings</small>',
                'EXTENDED_USER': '<strong class="text-warning"><i class="bi bi-person-gear"></i> Extended User</strong><br><small>Can create game sessions, manage loans, and access extended features</small>',
                'BASIC_USER': '<strong class="text-primary"><i class="bi bi-person"></i> Basic User</strong><br><small>Can view games, join sessions, and access basic features</small>'
            };
            
            roleDescription.innerHTML = descriptions[this.value] || 'Select a role to see permissions';
        });
        
        // Set initial description
        if (roleSelect.value) {
            roleSelect.dispatchEvent(new Event('change'));
        }
    }
    
    // Password visibility toggle
    const togglePasswordBtn = document.getElementById('togglePassword');
    const togglePasswordIcon = document.getElementById('togglePasswordIcon');
    
    if (togglePasswordBtn && passwordInput) {
        togglePasswordBtn.addEventListener('click', function() {
            const type = passwordInput.type === 'password' ? 'text' : 'password';
            passwordInput.type = type;
            if (togglePasswordIcon) {
                togglePasswordIcon.className = type === 'password' ? 'bi bi-eye' : 'bi bi-eye-slash';
            }
        });
    }
});