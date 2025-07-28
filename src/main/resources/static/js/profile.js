// Profile Page JavaScript - Trà Đạo Quản Trị Theme

document.addEventListener('DOMContentLoaded', function() {
    // Initialize profile functionality
    initializeProfile();
});

function initializeProfile() {
    // Auto-hide alerts after 5 seconds
    setTimeout(function() {
        const alerts = document.querySelectorAll('.alert');
        alerts.forEach(function(alert) {
            if (!alert.classList.contains('hide')) {
                alert.classList.add('hide');
                setTimeout(() => {
                    alert.style.display = 'none';
                }, 300);
            }
        });
    }, 5000);

    // Add form validation
    setupFormValidation();
    
    // Setup avatar upload
    setupAvatarUpload();
    
    // Setup alert toggles
    setupAlertToggles();
    
    // Setup mobile-specific enhancements
    setupMobileEnhancements();
}

function setupMobileEnhancements() {
    // Add touch-friendly interactions for mobile
    const isMobile = window.innerWidth <= 768;
    
    if (isMobile) {
        // Enhance button touch targets
        const buttons = document.querySelectorAll('.btn-green, .btn-brown, .avatar-upload-btn');
        buttons.forEach(button => {
            button.style.minHeight = '44px'; // iOS recommended touch target
            button.style.minWidth = '44px';
        });
        
        // Add swipe gestures for form toggles
        setupSwipeGestures();
        
        // Optimize form inputs for mobile
        setupMobileFormInputs();
        
        // Add mobile-specific keyboard handling
        setupMobileKeyboardHandling();
    }
}

function setupSwipeGestures() {
    let startX = 0;
    let startY = 0;
    let endX = 0;
    let endY = 0;
    
    document.addEventListener('touchstart', function(e) {
        startX = e.touches[0].clientX;
        startY = e.touches[0].clientY;
    });
    
    document.addEventListener('touchend', function(e) {
        endX = e.changedTouches[0].clientX;
        endY = e.changedTouches[0].clientY;
        
        const diffX = startX - endX;
        const diffY = startY - endY;
        
        // Swipe left to close forms
        if (Math.abs(diffX) > Math.abs(diffY) && diffX > 50) {
            const editForm = document.getElementById('editForm');
            const passwordForm = document.getElementById('passwordForm');
            
            if (editForm.style.display === 'block') {
                closeEditForm();
            } else if (passwordForm.style.display === 'block') {
                closePasswordForm();
            }
        }
    });
}

function setupMobileFormInputs() {
    const inputs = document.querySelectorAll('.profile-input');
    inputs.forEach(input => {
        // Add mobile-specific attributes
        input.setAttribute('autocomplete', 'off');
        input.setAttribute('autocorrect', 'off');
        input.setAttribute('autocapitalize', 'off');
        
        // Enhance focus behavior for mobile
        input.addEventListener('focus', function() {
            this.style.fontSize = '16px'; // Prevent zoom on iOS
        });
        
        input.addEventListener('blur', function() {
            this.style.fontSize = ''; // Reset font size
        });
    });
}

function setupMobileKeyboardHandling() {
    // Handle viewport adjustments when keyboard appears
    const inputs = document.querySelectorAll('input, textarea');
    inputs.forEach(input => {
        input.addEventListener('focus', function() {
            // Scroll to input with offset for mobile
            setTimeout(() => {
                this.scrollIntoView({ 
                    behavior: 'smooth', 
                    block: 'center' 
                });
            }, 300);
        });
    });
}

function setupAlertToggles() {
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        // Add close button
        const closeBtn = document.createElement('button');
        closeBtn.innerHTML = '<i class="fas fa-times"></i>';
        closeBtn.style.cssText = `
            background: none;
            border: none;
            color: inherit;
            cursor: pointer;
            margin-left: auto;
            padding: 4px;
            border-radius: 50%;
            width: 24px;
            height: 24px;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: background 0.2s;
            min-width: 44px;
            min-height: 44px;
        `;
        closeBtn.addEventListener('mouseenter', () => {
            closeBtn.style.background = 'rgba(0,0,0,0.1)';
        });
        closeBtn.addEventListener('mouseleave', () => {
            closeBtn.style.background = 'none';
        });
        closeBtn.addEventListener('click', () => {
            alert.classList.add('hide');
            setTimeout(() => {
                alert.style.display = 'none';
            }, 300);
        });
        alert.appendChild(closeBtn);
    });
}

function setupAvatarUpload() {
    const avatarInput = document.getElementById('avatarInput');
    const avatarImg = document.getElementById('avatarImg');
    const avatarPreview = document.getElementById('avatarPreview');
    const avatarActionButtons = document.getElementById('avatarActionButtons');
    const confirmBtn = document.getElementById('confirmAvatarBtn');
    const cancelBtn = document.getElementById('cancelAvatarBtn');
    const avatarUploadForm = document.getElementById('avatarUploadForm');
    const avatarHiddenInput = document.getElementById('avatarHiddenInput');
    
    let selectedFile = null;
    
    // Handle avatar image load error
    if (avatarImg) {
        avatarImg.addEventListener('error', function() {
            console.log('Avatar image failed to load, using default avatar');
            this.src = '/images/default-avatar.png';
        });
    }
    
    if (avatarInput) {
        avatarInput.addEventListener('change', function() {
            const file = this.files[0];
            if (file && file.type.startsWith('image/')) {
                // Validate file size (max 5MB)
                if (file.size > 5 * 1024 * 1024) {
                    showError('Kích thước file không được vượt quá 5MB');
                    this.value = '';
                    return;
                }
                
                const reader = new FileReader();
                reader.onload = function(e) {
                    // Hiển thị preview đè lên ảnh gốc
                    avatarPreview.src = e.target.result;
                    avatarPreview.style.opacity = '1';
                    avatarPreview.style.pointerEvents = 'auto';
                    
                    // Handle preview image load error
                    avatarPreview.onerror = function() {
                        console.log('Preview image failed to load');
                        this.style.display = 'none';
                    };
                    
                    // Hiển thị action buttons
                    avatarActionButtons.style.display = 'flex';
                    
                    selectedFile = file;
                };
                reader.readAsDataURL(file);
            } else {
                showError('Vui lòng chọn file ảnh hợp lệ');
                this.value = '';
            }
        });
    }
    
    if (cancelBtn) {
        cancelBtn.addEventListener('click', function() {
            // Ẩn preview và action buttons
            avatarPreview.style.opacity = '0';
            avatarPreview.style.pointerEvents = 'none';
            avatarActionButtons.style.display = 'none';
            
            // Reset file input
            avatarInput.value = '';
            selectedFile = null;
        });
    }
    
    if (confirmBtn) {
        confirmBtn.addEventListener('click', function() {
            if (selectedFile) {
                // Hiển thị loading state
                confirmBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang tải...';
                confirmBtn.disabled = true;
                
                // Tạo form upload ẩn
                const formData = new FormData();
                formData.append('avatar', selectedFile);
                
                // Gửi request upload
                fetch('/profile/avatar', {
                    method: 'POST',
                    body: formData
                })
                .then(response => {
                    if (response.ok) {
                        return response.json().catch(() => ({})); // Handle non-JSON response
                    } else {
                        throw new Error('Upload failed');
                    }
                })
                .then(data => {
                    // Cập nhật ảnh hiện tại với URL mới từ server
                    if (data.avatarUrl) {
                        avatarImg.src = data.avatarUrl;
                    } else {
                        // Fallback to preview if no URL returned
                        avatarImg.src = avatarPreview.src;
                    }
                    showSuccess('Cập nhật ảnh đại diện thành công!');
                    
                    // Ẩn preview và action buttons
                    avatarPreview.style.opacity = '0';
                    avatarPreview.style.pointerEvents = 'none';
                    avatarActionButtons.style.display = 'none';
                    
                    // Reset
                    avatarInput.value = '';
                    selectedFile = null;
                })
                .catch(error => {
                    showError('Lỗi upload ảnh: ' + error.message);
                })
                .finally(() => {
                    // Reset button
                    confirmBtn.innerHTML = '<i class="fas fa-check"></i> Xác nhận';
                    confirmBtn.disabled = false;
                });
            }
        });
    }
}

function toggleEditForm() {
    const editForm = document.getElementById('editForm');
    const passwordForm = document.getElementById('passwordForm');
    
    // Ẩn password form nếu đang mở
    passwordForm.style.display = 'none';
    
    // Toggle edit form
    if (editForm.style.display === 'none' || editForm.style.display === '') {
        editForm.style.display = 'block';
        // Focus on first input when form is shown
        setTimeout(() => {
            const firstInput = editForm.querySelector('input[type="text"]');
            if (firstInput) {
                firstInput.focus();
                // Scroll to form on mobile
                if (window.innerWidth <= 768) {
                    editForm.scrollIntoView({ 
                        behavior: 'smooth', 
                        block: 'start' 
                    });
                }
            }
        }, 100);
    }
}

function closeEditForm() {
    const editForm = document.getElementById('editForm');
    editForm.style.display = 'none';
}

function togglePasswordForm() {
    const editForm = document.getElementById('editForm');
    const passwordForm = document.getElementById('passwordForm');
    
    // Ẩn edit form nếu đang mở
    editForm.style.display = 'none';
    
    // Toggle password form
    if (passwordForm.style.display === 'none' || passwordForm.style.display === '') {
        passwordForm.style.display = 'block';
        // Focus on first input when form is shown
        setTimeout(() => {
            const firstInput = passwordForm.querySelector('input[type="password"]');
            if (firstInput) {
                firstInput.focus();
                // Scroll to form on mobile
                if (window.innerWidth <= 768) {
                    passwordForm.scrollIntoView({ 
                        behavior: 'smooth', 
                        block: 'start' 
                    });
                }
            }
        }, 100);
        
        // Setup password validation
        setupPasswordValidation();
    }
}

function setupPasswordValidation() {
    const newPasswordInput = document.getElementById('newPassword');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    
    if (newPasswordInput && confirmPasswordInput) {
        // Real-time password validation
        newPasswordInput.addEventListener('input', function() {
            validatePasswordStrength(this.value);
        });
        
        confirmPasswordInput.addEventListener('input', function() {
            validatePasswordConfirmation(newPasswordInput.value, this.value);
        });
    }
}

function validatePasswordStrength(password) {
    const newPasswordInput = document.getElementById('newPassword');
    if (!newPasswordInput) return true;
    // Password strength validation
    if (password.length < 6) {
        showFieldError(newPasswordInput, 'Mật khẩu phải có ít nhất 6 ký tự');
        return false;
    } else {
        clearFieldError(newPasswordInput);
        return true;
    }
}

function validatePasswordConfirmation(password, confirmPassword) {
    const confirmPasswordInput = document.getElementById('confirmPassword');
    
    if (password !== confirmPassword) {
        showFieldError(confirmPasswordInput, 'Mật khẩu xác nhận không khớp');
        return false;
    } else {
        clearFieldError(confirmPasswordInput);
        return true;
    }
}

function closePasswordForm() {
    const passwordForm = document.getElementById('passwordForm');
    passwordForm.style.display = 'none';
}

function setupFormValidation() {
    // Phone number formatting
    const phoneInput = document.getElementById('phone');
    if (phoneInput) {
        phoneInput.addEventListener('input', function() {
            this.value = formatPhoneNumber(this.value);
        });
    }
    
    // Form submission validation
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            if (!validateForm(this)) {
                e.preventDefault();
            }
        });
    });
}

function validateForm(form) {
    let isValid = true;
    
    // Clear all previous errors
    const errorMessages = form.querySelectorAll('.error-message');
    errorMessages.forEach(error => {
        error.textContent = '';
        error.style.display = 'none';
    });
    
    // Remove error classes from inputs
    const inputs = form.querySelectorAll('input');
    inputs.forEach(input => {
        input.classList.remove('error');
    });
    
    // Validate required fields
    const requiredFields = form.querySelectorAll('[required]');
    requiredFields.forEach(field => {
        if (!field.value.trim()) {
            showFieldError(field, 'Trường này là bắt buộc');
            isValid = false;
        } else {
            clearFieldError(field);
        }
    });
    
    // Validate phone number if present
    const phoneField = form.querySelector('input[type="tel"]');
    if (phoneField && phoneField.value.trim()) {
        if (!isValidPhone(phoneField.value)) {
            showFieldError(phoneField, 'Số điện thoại không hợp lệ');
            isValid = false;
        } else {
            clearFieldError(phoneField);
        }
    }
    
    // Validate password confirmation for password form
    const passwordField = form.querySelector('#newPassword');
    const confirmPasswordField = form.querySelector('#confirmPassword');
    const currentPasswordField = form.querySelector('#currentPassword');
    
    if (passwordField && confirmPasswordField && currentPasswordField) {
        // Validate current password
        if (currentPasswordField.value.trim().length === 0) {
            showFieldError(currentPasswordField, 'Vui lòng nhập mật khẩu hiện tại');
            isValid = false;
        } else {
            clearFieldError(currentPasswordField);
        }
        
        // Validate new password strength
        if (passwordField.value.length < 6) {
            showFieldError(passwordField, 'Mật khẩu mới phải có ít nhất 6 ký tự');
            isValid = false;
        } else if (passwordField.value.length > 50) {
            showFieldError(passwordField, 'Mật khẩu mới không được quá 50 ký tự');
            isValid = false;
        } else if (passwordField.value === currentPasswordField.value) {
            showFieldError(passwordField, 'Mật khẩu mới không được trùng với mật khẩu hiện tại');
            isValid = false;
        } else {
            clearFieldError(passwordField);
        }
        
        // Validate password confirmation
        if (confirmPasswordField.value !== passwordField.value) {
            showFieldError(confirmPasswordField, 'Mật khẩu xác nhận không khớp');
            isValid = false;
        } else {
            clearFieldError(confirmPasswordField);
        }
    }
    
    return isValid;
}

function isValidPhone(phone) {
    // Vietnamese phone number validation
    const phoneRegex = /^(0|\+84)(3[2-9]|5[689]|7[06-9]|8[1-689]|9[0-46-9])[0-9]{7}$/;
    return phoneRegex.test(phone.replace(/\s/g, ''));
}

function showFieldError(field, message) {
    // Remove existing error
    clearFieldError(field);
    
    // Add error class
    field.classList.add('error');
    
    // Create error message
    const errorDiv = document.createElement('div');
    errorDiv.className = 'field-error';
    errorDiv.textContent = message;
    errorDiv.style.cssText = `
        color: var(--color-error);
        font-size: 0.85rem;
        margin-top: 4px;
        padding: 4px 8px;
        background: rgba(220,53,69,0.1);
        border-radius: 6px;
        border-left: 3px solid #dc3545;
    `;
    
    // Insert after field
    field.parentNode.insertBefore(errorDiv, field.nextSibling);
}

function clearFieldError(field) {
    field.classList.remove('error');
    const errorDiv = field.parentNode.querySelector('.field-error');
    if (errorDiv) {
        errorDiv.remove();
    }
}

function showSuccess(message) {
    removeExistingAlerts();
    
    const alertDiv = document.createElement('div');
    alertDiv.className = 'alert alert-success';
    alertDiv.innerHTML = `
        <i class="fas fa-check-circle"></i>
        <span>${message}</span>
        <button type="button" class="alert-close" onclick="this.parentElement.remove()">
            <i class="fas fa-times"></i>
        </button>
    `;
    
    // Insert at the top of profile container
    const profileContainer = document.querySelector('.profile-container');
    if (profileContainer) {
        profileContainer.insertBefore(alertDiv, profileContainer.firstChild);
        
        // Auto remove after 5 seconds
        setTimeout(() => {
            if (alertDiv.parentElement) {
                alertDiv.remove();
            }
        }, 5000);
    }
}

function showError(message) {
    removeExistingAlerts();
    
    const alertDiv = document.createElement('div');
    alertDiv.className = 'alert alert-danger';
    alertDiv.innerHTML = `
        <i class="fas fa-exclamation-circle"></i>
        <span>${message}</span>
        <button type="button" class="alert-close" onclick="this.parentElement.remove()">
            <i class="fas fa-times"></i>
        </button>
    `;
    
    // Insert at the top of profile container
    const profileContainer = document.querySelector('.profile-container');
    if (profileContainer) {
        profileContainer.insertBefore(alertDiv, profileContainer.firstChild);
        
        // Auto remove after 8 seconds for errors
        setTimeout(() => {
            if (alertDiv.parentElement) {
                alertDiv.remove();
            }
        }, 8000);
    }
}

function removeExistingAlerts() {
    const existingAlerts = document.querySelectorAll('.alert');
    existingAlerts.forEach(alert => alert.remove());
}

function formatPhoneNumber(input) {
    // Remove all non-digits
    let value = input.replace(/\D/g, '');
    
    // Format Vietnamese phone number
    if (value.length > 0) {
        if (value.startsWith('84')) {
            value = '0' + value.substring(2);
        }
        
        // Add spaces for better readability
        if (value.length >= 4) {
            value = value.substring(0, 4) + ' ' + value.substring(4);
        }
        if (value.length >= 8) {
            value = value.substring(0, 8) + ' ' + value.substring(8);
        }
        if (value.length >= 12) {
            value = value.substring(0, 12) + ' ' + value.substring(12);
        }
    }
    
    return value;
} 