// User Form Validation JavaScript - Improved Version

document.addEventListener('DOMContentLoaded', function() {
    // Form elements
    const form = document.getElementById('userForm');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const togglePasswordBtn = document.getElementById('togglePassword');
    const togglePasswordIcon = document.getElementById('togglePasswordIcon');
    const roleSelect = document.getElementById('role');
    const roleDescription = document.getElementById('roleDescription');
    const usernameInput = document.getElementById('username');
    const emailInput = document.getElementById('email');
    const firstNameInput = document.getElementById('firstName');
    const lastNameInput = document.getElementById('lastName');
    
    // Check if we're editing (presence of hidden ID field or readonly username)
    const isEditing = document.querySelector('input[name="id"]') !== null || 
                     (usernameInput && usernameInput.hasAttribute('readonly'));
    
    console.log('Form initialized. Editing mode:', isEditing);
    
    // Photo upload functionality
    initPhotoUpload();
    
    // Password visibility toggle
    if (togglePasswordBtn && passwordInput) {
        togglePasswordBtn.addEventListener('click', function() {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            if (togglePasswordIcon) {
                togglePasswordIcon.className = type === 'password' ? 'bi bi-eye' : 'bi bi-eye-slash';
            }
        });
    }
    
    // Password validation with relaxed requirements
    if (passwordInput) {
        const passwordStrength = document.getElementById('passwordStrength');
        const passwordFeedback = document.getElementById('passwordFeedback');
        const passwordRequirements = document.getElementById('passwordRequirements');
        
        passwordInput.addEventListener('input', function() {
            const password = this.value;
            
            if (!password) {
                if (passwordRequirements) passwordRequirements.style.display = 'none';
                if (passwordStrength) passwordStrength.className = 'password-strength';
                if (passwordFeedback) passwordFeedback.textContent = '';
                clearCustomValidity(this);
                return;
            }
            
            if (passwordRequirements) passwordRequirements.style.display = 'block';
            
            // Relaxed requirements
            const requirements = {
                length: password.length >= 8,
                letter: /[a-zA-Z]/.test(password),
                numberOrSymbol: /\d/.test(password) || /[!@#$%^&*(),.?\":{}|<>]/.test(password)
            };
            
            // Update requirement indicators
            updateRequirement('req-length', requirements.length);
            updateRequirement('req-letter', requirements.letter);
            updateRequirement('req-number', requirements.numberOrSymbol);
            
            // Calculate strength
            const score = Object.values(requirements).filter(Boolean).length;
            updatePasswordStrength(score, passwordStrength, passwordFeedback);
            
            // Validate password
            validatePasswordInput(password, this);
            
            // Validate confirm password if it exists
            if (confirmPasswordInput && confirmPasswordInput.value) {
                validatePasswordMatch();
            }
        });
        
        // Clear validation state when field is focused
        passwordInput.addEventListener('focus', function() {
            clearValidationClasses(this);
        });
    }
    
    // Confirm password validation
    if (confirmPasswordInput) {
        confirmPasswordInput.addEventListener('input', validatePasswordMatch);
        confirmPasswordInput.addEventListener('focus', function() {
            clearValidationClasses(this);
        });
    }
    
    // Role description
    if (roleSelect && roleDescription) {
        roleSelect.addEventListener('change', function() {
            updateRoleDescription(this.value);
        });
        
        // Set initial description if editing
        if (roleSelect.value) {
            updateRoleDescription(roleSelect.value);
        }
    }
    
    // Username validation - simplified and non-blocking
    if (usernameInput && !isEditing) {
        let usernameTimeout;
        
        usernameInput.addEventListener('input', function() {
            const username = this.value.trim();
            
            // Clear previous validation classes
            clearValidationClasses(this);
            
            if (!username) {
                clearCustomValidity(this);
                return;
            }
            
            // Basic pattern validation
            if (!validateUsernamePattern(username)) {
                return; // Pattern validation will handle the error
            }
            
            // Clear timeout for previous request
            clearTimeout(usernameTimeout);
            
            // Set timeout for API check (debounce)
            usernameTimeout = setTimeout(() => {
                checkUsernameAvailability(username, this);
            }, 500);
        });
        
        usernameInput.addEventListener('focus', function() {
            clearValidationClasses(this);
        });
        
        usernameInput.addEventListener('blur', function() {
            const username = this.value.trim();
            if (username && !validateUsernamePattern(username)) {
                this.classList.add('is-invalid');
            }
        });
    }
    
    // Email validation - simplified and non-blocking
    if (emailInput) {
        let emailTimeout;
        
        emailInput.addEventListener('input', function() {
            const email = this.value.trim();
            
            // Clear previous validation classes
            clearValidationClasses(this);
            
            if (!email) {
                clearCustomValidity(this);
                return;
            }
            
            // Basic email pattern validation
            if (!validateEmailPattern(email)) {
                return; // Pattern validation will handle the error
            }
            
            // Clear timeout for previous request
            clearTimeout(emailTimeout);
            
            // Set timeout for API check (debounce)
            emailTimeout = setTimeout(() => {
                checkEmailAvailability(email, this);
            }, 500);
        });
        
        emailInput.addEventListener('focus', function() {
            clearValidationClasses(this);
        });
        
        emailInput.addEventListener('blur', function() {
            const email = this.value.trim();
            if (email && !validateEmailPattern(email)) {
                this.classList.add('is-invalid');
            }
        });
    }
    
    // Name validation - non-blocking
    if (firstNameInput) {
        firstNameInput.addEventListener('input', function() {
            clearValidationClasses(this);
            validateNameField(this, 'First name');
        });
        
        firstNameInput.addEventListener('focus', function() {
            clearValidationClasses(this);
        });
    }
    
    if (lastNameInput) {
        lastNameInput.addEventListener('input', function() {
            clearValidationClasses(this);
            validateNameField(this, 'Last name');
        });
        
        lastNameInput.addEventListener('focus', function() {
            clearValidationClasses(this);
        });
    }
    
    // Form submission validation
    if (form) {
        form.addEventListener('submit', function(event) {
            console.log('Form submission started');
            
            // Clear all custom validity first
            const inputs = this.querySelectorAll('input, select');
            inputs.forEach(input => clearCustomValidity(input));
            
            let isValid = true;
            
            // Custom validation for password match
            if (confirmPasswordInput && passwordInput) {
                if (passwordInput.value !== confirmPasswordInput.value) {
                    confirmPasswordInput.setCustomValidity('Passwords do not match');
                    isValid = false;
                }
            }
            
            // Validate required password for new users
            if (!isEditing && passwordInput && !passwordInput.value.trim()) {
                passwordInput.setCustomValidity('Password is required for new users');
                isValid = false;
            }
            
            // Check HTML5 validation
            if (!this.checkValidity()) {
                isValid = false;
            }
            
            if (!isValid) {
                event.preventDefault();
                event.stopPropagation();
                console.log('Form validation failed');
            } else {
                console.log('Form validation passed');
            }
            
            this.classList.add('was-validated');
        });
    }
    
    // Initialize role description on page load
    if (roleSelect && roleSelect.value) {
        updateRoleDescription(roleSelect.value);
    }
    
    // Helper functions
    function initPhotoUpload() {
        const showUploadBtn = document.querySelector('.show-upload');
        const cancelUploadBtn = document.querySelector('.cancel-upload');
        const uploadForm = document.querySelector('.photo-upload-form');
        const photoActions = document.querySelector('.photo-actions');

        if (showUploadBtn && uploadForm && photoActions) {
            showUploadBtn.addEventListener('click', function() {
                uploadForm.style.display = 'block';
                photoActions.style.display = 'none';
            });

            if (cancelUploadBtn) {
                cancelUploadBtn.addEventListener('click', function() {
                    uploadForm.style.display = 'none';
                    photoActions.style.display = 'block';
                    const fileInput = uploadForm.querySelector('input[type="file"]');
                    if (fileInput) fileInput.value = '';
                });
            }
        }

        // Handle form submission with file upload
        const uploadFormElement = document.querySelector('.photo-upload-form');
        if (uploadFormElement) {
            uploadFormElement.addEventListener('submit', function(e) {
                const fileInput = this.querySelector('input[type="file"]');
                const submitBtn = this.querySelector('button[type="submit"]');
                
                if (fileInput && fileInput.files.length === 0) {
                    e.preventDefault();
                    alert('Please select a file to upload.');
                    return false;
                }
                
                // Show loading state
                if (submitBtn) {
                    submitBtn.disabled = true;
                    submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Uploading...';
                }
            });
        }
    }
    
    function clearValidationClasses(element) {
        element.classList.remove('is-valid', 'is-invalid');
    }
    
    function clearCustomValidity(element) {
        element.setCustomValidity('');
    }
    
    function validateUsernamePattern(username) {
        const pattern = /^[a-zA-Z0-9_.-]+$/;
        
        if (username.length < 3 || username.length > 50) {
            return false;
        }
        
        if (!pattern.test(username)) {
            return false;
        }
        
        return true;
    }
    
    function validateEmailPattern(email) {
        const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailPattern.test(email);
    }
    
    function validateNameField(element, fieldName) {
        const name = element.value.trim();
        if (!name) return true; // Optional field
        
        const namePattern = fieldName === 'Last name' ? /^[a-zA-ZÀ-ÿ\s]*$/ : /^[a-zA-ZÀ-ÿ\s]+$/;
        
        if (!namePattern.test(name)) {
            element.setCustomValidity(`${fieldName} can only contain letters and spaces`);
            return false;
        } else {
            element.setCustomValidity('');
            return true;
        }
    }
    
    function validatePasswordInput(password, element) {
        clearCustomValidity(element);
        
        if (password.length < 8) {
            element.setCustomValidity('Password must be at least 8 characters long');
            return false;
        }
        
        // Check for letters
        if (!/[a-zA-Z]/.test(password)) {
            element.setCustomValidity('Password must contain at least one letter');
            return false;
        }
        
        // Check for numbers or symbols
        if (!/\d/.test(password) && !/[!@#$%^&*(),.?\":{}|<>]/.test(password)) {
            element.setCustomValidity('Password must contain at least one number or symbol');
            return false;
        }
        
        return true;
    }
    
    function checkUsernameAvailability(username, element) {
        const userId = isEditing ? document.querySelector('input[name="id"]')?.value : null;
        const url = `/users/api/users/check-username?username=${encodeURIComponent(username)}${userId ? `&excludeId=${userId}` : ''}`;
        
        fetch(url)
            .then(response => response.json())
            .then(data => {
                if (data.available) {
                    element.classList.add('is-valid');
                    element.classList.remove('is-invalid');
                    clearCustomValidity(element);
                } else {
                    element.classList.add('is-invalid');
                    element.classList.remove('is-valid');
                    element.setCustomValidity(data.message || 'Username is not available');
                }
            })
            .catch(error => {
                console.warn('Error checking username availability:', error);
                // Don't show error to user, just clear validation
                clearValidationClasses(element);
            });
    }
    
    function checkEmailAvailability(email, element) {
        const userId = isEditing ? document.querySelector('input[name="id"]')?.value : null;
        const url = `/users/api/users/check-email?email=${encodeURIComponent(email)}${userId ? `&excludeId=${userId}` : ''}`;
        
        fetch(url)
            .then(response => response.json())
            .then(data => {
                if (data.available) {
                    element.classList.add('is-valid');
                    element.classList.remove('is-invalid');
                    clearCustomValidity(element);
                } else {
                    element.classList.add('is-invalid');
                    element.classList.remove('is-valid');
                    element.setCustomValidity(data.message || 'Email is not available');
                }
            })
            .catch(error => {
                console.warn('Error checking email availability:', error);
                // Don't show error to user, just clear validation
                clearValidationClasses(element);
            });
    }
    
    function updateRequirement(id, met) {
        const element = document.getElementById(id);
        if (element) {
            element.className = met ? 'requirement met' : 'requirement unmet';
        }
    }
    
    function updatePasswordStrength(score, strengthElement, feedbackElement) {
        if (!strengthElement || !feedbackElement) return;
        
        const levels = [
            { class: '', text: '', color: '' },
            { class: 'strength-weak', text: 'Weak', color: 'text-weak' },
            { class: 'strength-fair', text: 'Fair', color: 'text-fair' },
            { class: 'strength-strong', text: 'Good', color: 'text-strong' }
        ];
        
        const level = levels[Math.min(score, 3)] || levels[0];
        strengthElement.className = `password-strength ${level.class}`;
        feedbackElement.textContent = level.text;
        feedbackElement.className = `password-feedback ${level.color}`;
    }
    
    function validatePasswordMatch() {
        if (!confirmPasswordInput || !passwordInput) return;
        
        const password = passwordInput.value;
        const confirmPassword = confirmPasswordInput.value;
        
        clearValidationClasses(confirmPasswordInput);
        
        if (!confirmPassword) {
            clearCustomValidity(confirmPasswordInput);
            return;
        }
        
        if (password !== confirmPassword) {
            confirmPasswordInput.setCustomValidity('Passwords do not match');
            confirmPasswordInput.classList.add('is-invalid');
        } else {
            confirmPasswordInput.setCustomValidity('');
            confirmPasswordInput.classList.add('is-valid');
        }
    }
    
    function updateRoleDescription(role) {
        if (!roleDescription) return;
        
        const descriptions = {
            'ADMIN': '<strong class="text-danger"><i class="bi bi-shield-fill"></i> Administrator</strong><br><small>Full system access, can manage all users, games, and settings</small>',
            'EXTENDED_USER': '<strong class="text-warning"><i class="bi bi-person-gear"></i> Extended User</strong><br><small>Can create game sessions, manage loans, and access extended features</small>',
            'BASIC_USER': '<strong class="text-primary"><i class="bi bi-person"></i> Basic User</strong><br><small>Can view games, join sessions, and access basic features</small>'
        };
        
        roleDescription.innerHTML = descriptions[role] || 'Select a role to see permissions';
    }
});