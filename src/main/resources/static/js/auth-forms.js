// Auth Forms JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // Xóa tất cả error message khi load trang
    const existingErrorMessages = document.querySelectorAll('.error-message');
    existingErrorMessages.forEach(errorElement => {
        errorElement.remove();
    });

    // Elements
    const authModal = document.getElementById('authModal');
    const authCloseBtn = document.getElementById('authCloseBtn');
    const authForms = document.querySelectorAll('.auth-form');
    const loginForm = document.getElementById('loginFormElement');
    const registerForm = document.getElementById('registerFormElement');
    const toast = document.getElementById('toast');
    const toastClose = document.querySelector('.toast-close');

    // Password toggle elements
    const passwordToggles = document.querySelectorAll('.password-toggle');
    const passwordInputs = document.querySelectorAll('input[type="password"]');

    // Validation patterns
    const patterns = {
        email: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
        phone: /^[0-9]{10,11}$/,
        password: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{8,}$/
    };

    let loginFormInteracted = false;

    // Show modal function
    window.showAuthModal = function(form = 'login') {
        clearErrors(); // Đảm bảo reset lỗi trước khi hiện modal
        loginFormInteracted = false; // Reset cờ tương tác form login
        setTimeout(function() {
            const modalInputs = document.querySelectorAll('#authModal input');
            modalInputs.forEach(input => input.blur());
        }, 0);
        // Reset form login khi mở modal đăng nhập
        if (form === 'login') {
            const loginForm = document.getElementById('loginFormElement');
            if (loginForm) loginForm.reset();
            // Xóa error message nếu có
            ['loginEmailError', 'loginPasswordError'].forEach(id => {
                const el = document.getElementById(id);
                if (el) el.textContent = '';
            });
        }
        // Dùng lại biến authModal đã khai báo ở đầu file
        authModal.classList.add('show');
        document.body.classList.add('modal-open');
        // Nếu có wrapper chính, thêm modal-blur
        const mainContent = document.querySelector('.main-content') || document.querySelector('main') || document.getElementById('app');
        if (mainContent) mainContent.classList.add('modal-blur');
        // Lưu trạng thái modal vào sessionStorage
        sessionStorage.setItem('authModalOpen', form);
        switchForm(form);
    };

    // Hide modal function
    function hideAuthModal() {
        const authModal = document.getElementById('authModal');
        authModal.classList.remove('show');
        document.body.classList.remove('modal-open');
        const mainContent = document.querySelector('.main-content') || document.querySelector('main') || document.getElementById('app');
        if (mainContent) mainContent.classList.remove('modal-blur');
        // Xóa trạng thái modal khỏi sessionStorage
        sessionStorage.removeItem('authModalOpen');
        clearErrors();
    }

    // Form switching
    function switchForm(formName) {
        // Update forms
        authForms.forEach(form => {
            form.classList.remove('active');
            if (form.id === formName + 'Form') {
                form.classList.add('active');
            }
        });
        // Reset form đăng ký khi chuyển sang tab đăng ký
        if (formName === 'register') {
            resetRegisterForm();
        }
    }

    // Hàm reset form đăng ký
    function resetRegisterForm() {
        const registerForm = document.getElementById('registerFormElement');
        if (registerForm) registerForm.reset();
        // Xóa các error message nếu có
        ['registerNameError', 'registerEmailError', 'registerPasswordError', 'registerConfirmPasswordError', 'agreeTermsError'].forEach(id => {
            const el = document.getElementById(id);
            if (el) el.textContent = '';
        });
    }

    window.switchForm = function(formName) {
        const authForms = document.querySelectorAll('.auth-form');
        authForms.forEach(form => {
            form.classList.remove('active');
        });
        var form = document.getElementById(formName + 'Form');
        if (form) {
            form.classList.add('active');
        }
    }

    // Event listeners for form switching
    const showRegisterForm = document.getElementById('showRegisterForm');
    const showLoginForm = document.getElementById('showLoginForm');
    const showForgotForm = document.getElementById('showForgotPasswordForm');

    if (showRegisterForm) {
        showRegisterForm.addEventListener('click', function(e) {
            e.preventDefault();
            switchForm('register');
        });
    }

    if (showLoginForm) {
        showLoginForm.addEventListener('click', function(e) {
            e.preventDefault();
            switchForm('login');
        });
    }

    if (showForgotForm) {
        showForgotForm.addEventListener('click', function(e) {
            e.preventDefault();
            switchForm('forgot');
        });
    }

    // Close modal events
    if (authCloseBtn) {
        authCloseBtn.addEventListener('click', hideAuthModal);
    }
    if (authModal) {
        authModal.addEventListener('click', function(e) {
            if (e.target === authModal) {
                hideAuthModal();
            }
        });
    }

    // Password toggle functionality
    passwordToggles.forEach((toggle, index) => {
        toggle.addEventListener('click', function() {
            const input = passwordInputs[index];
            const icon = this.querySelector('i');
            
            if (input.type === 'password') {
                input.type = 'text';
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
            } else {
                input.type = 'password';
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
            }
        });
    });

    // Validation functions
    function validateEmail(email) {
        return patterns.email.test(email);
    }

    function validatePhone(phone) {
        return patterns.phone.test(phone);
    }

    function validatePassword(password) {
        return patterns.password.test(password);
    }

    function showError(elementId, message) {
        let errorElement = document.getElementById(elementId);
        if (!errorElement) {
            // Tạo thẻ span mới nếu chưa có
            errorElement = document.createElement('span');
            errorElement.id = elementId;
            errorElement.className = 'error-message show';
            
            // Tìm input tương ứng để chèn error message vào sau
            const inputId = elementId.replace('Error', '');
            let targetElement = document.getElementById(inputId);
            
            // Nếu không tìm thấy input, tìm theo tên khác
//            if (!targetElement && inputId === 'agreeTerms') {
//                targetElement = document.querySelector('input[name="agreeTerms"]');
//            }
            
            if (targetElement && targetElement.parentNode) {
                targetElement.parentNode.appendChild(errorElement);
            } else {
                // Fallback: tìm form và chèn vào cuối
                const form = document.getElementById('registerFormElement');
                if (form) {
                    form.appendChild(errorElement);
                }
            }
        }
        errorElement.textContent = message;
        errorElement.classList.add('show');
    }

    function hideError(elementId) {
        const errorElement = document.getElementById(elementId);
        if (errorElement) {
            // Xóa thẻ span hoàn toàn khỏi DOM
            errorElement.remove();
        }
    }

    function clearErrors() {
        const errorElements = document.querySelectorAll('.error-message');
        errorElements.forEach(element => {
            element.classList.remove('show');
        });
        
        const errorInputs = document.querySelectorAll('.form-group input.error');
        errorInputs.forEach(input => {
            input.classList.remove('error');
        });
    }

    // Real-time validation
    function setupRealTimeValidation() {
        // Login form validation
        const loginEmail = document.getElementById('loginEmail');
        const loginPassword = document.getElementById('loginPassword');

        // --- Cập nhật cho login form ---
        if (loginEmail) {
            let interacted = false;
            loginEmail.addEventListener('blur', function() {
                interacted = true;
                if (!this.value.trim()) {
                    showError('loginEmailError', 'Email không được để trống');
                    this.classList.add('error');
                } else if (!validateEmail(this.value)) {
                    showError('loginEmailError', 'Email không hợp lệ');
                    this.classList.add('error');
                } else {
                    hideError('loginEmailError');
                    this.classList.remove('error');
                }
            });
            loginEmail.addEventListener('input', function() {
                if (interacted) {
                    hideError('loginEmailError');
                    this.classList.remove('error');
                }
            });
        }

        if (loginPassword) {
            let interacted = false;
            loginPassword.addEventListener('blur', function() {
                interacted = true;
                if (!this.value.trim()) {
                    showError('loginPasswordError', 'Mật khẩu không được để trống');
                    this.classList.add('error');
                } else {
                    hideError('loginPasswordError');
                    this.classList.remove('error');
                }
            });
            loginPassword.addEventListener('input', function() {
                if (interacted) {
                    hideError('loginPasswordError');
                    this.classList.remove('error');
                }
            });
        }
        // --- END cập nhật ---

        // Register form validation
        const registerName = document.getElementById('registerName');
        const registerEmail = document.getElementById('registerEmail');
        const registerPassword = document.getElementById('registerPassword');
        const registerConfirmPassword = document.getElementById('registerConfirmPassword');
//        const agreeTerms = document.getElementById('agreeTerms');

        // --- Cập nhật cho register form ---
        if (registerName) {
            let interacted = false;
            registerName.addEventListener('blur', function() {
                interacted = true;
                if (!this.value.trim()) {
                    showError('registerNameError', 'Họ tên không được để trống');
                    this.classList.add('error');
                } else if (this.value.trim().length < 2) {
                    showError('registerNameError', 'Họ tên phải có ít nhất 2 ký tự');
                    this.classList.add('error');
                } else {
                    hideError('registerNameError');
                    this.classList.remove('error');
                }
            });
            registerName.addEventListener('input', function() {
                if (interacted) {
                    hideError('registerNameError');
                    this.classList.remove('error');
                }
            });
        }

        if (registerEmail) {
            let interacted = false;
            registerEmail.addEventListener('blur', function() {
                interacted = true;
                if (!this.value.trim()) {
                    showError('registerEmailError', 'Email không được để trống');
                    this.classList.add('error');
                } else if (!validateEmail(this.value)) {
                    showError('registerEmailError', 'Email không hợp lệ');
                    this.classList.add('error');
                } else {
                    hideError('registerEmailError');
                    this.classList.remove('error');
                }
            });
            registerEmail.addEventListener('input', function() {
                if (interacted) {
                    hideError('registerEmailError');
                    this.classList.remove('error');
                }
            });
        }

        if (registerPassword) {
            let interacted = false;
            registerPassword.addEventListener('blur', function() {
                interacted = true;
                if (!this.value.trim()) {
                    showError('registerPasswordError', 'Mật khẩu không được để trống');
                    this.classList.add('error');
                } else if (!validatePassword(this.value)) {
                    showError('registerPasswordError', 'Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số');
                    this.classList.add('error');
                } else {
                    hideError('registerPasswordError');
                    this.classList.remove('error');
                }
            });
            registerPassword.addEventListener('input', function() {
                if (interacted) {
                    hideError('registerPasswordError');
                    this.classList.remove('error');
                }
            });
        }

        if (registerConfirmPassword) {
            let interacted = false;
            registerConfirmPassword.addEventListener('blur', function() {
                interacted = true;
                const password = registerPassword ? registerPassword.value : '';
                if (!this.value.trim()) {
                    showError('registerConfirmPasswordError', 'Xác nhận mật khẩu không được để trống');
                    this.classList.add('error');
                } else if (this.value !== password) {
                    showError('registerConfirmPasswordError', 'Mật khẩu xác nhận không khớp');
                    this.classList.add('error');
                } else {
                    hideError('registerConfirmPasswordError');
                    this.classList.remove('error');
                }
            });
            registerConfirmPassword.addEventListener('input', function() {
                if (interacted) {
                    hideError('registerConfirmPasswordError');
                    this.classList.remove('error');
                }
            });
        }

//        if (agreeTerms) {
//            let interacted = false;
//            agreeTerms.addEventListener('change', function() {
//                interacted = true;
//                if (!this.checked) {
//                    showError('agreeTermsError', 'Bạn phải đồng ý với điều khoản & chính sách');
//                } else {
//                    hideError('agreeTermsError');
//                }
//            });
//            agreeTerms.addEventListener('blur', function() {
//                interacted = true;
//                if (!this.checked) {
//                    showError('agreeTermsError', 'Bạn phải đồng ý với điều khoản & chính sách');
//                }
//            });
//        }
        // --- END cập nhật ---
    }

    // Toast notification
    function showToast(message, type = 'success') {
        const toastContent = toast.querySelector('.toast-content');
        const toastIcon = toastContent.querySelector('.toast-icon');
        const toastMessage = toastContent.querySelector('.toast-message');

        // Set icon based on type
        toastIcon.className = 'toast-icon fas';
        if (type === 'success') {
            toastIcon.classList.add('fa-check-circle');
        } else if (type === 'error') {
            toastIcon.classList.add('fa-exclamation-circle');
        } else if (type === 'warning') {
            toastIcon.classList.add('fa-exclamation-triangle');
        }

        // Set message
        toastMessage.textContent = message;

        // Set toast type
        toast.className = `toast ${type}`;
        toast.classList.add('show');

        // Auto hide after 5 seconds
        setTimeout(() => {
            hideToast();
        }, 5000);
    }

    function hideToast() {
        toast.classList.remove('show');
    }

    // Toast close button
    if (toastClose) {
        toastClose.addEventListener('click', hideToast);
    }

    // Logout function
    window.logout = function() {
        // Call logout API to clear JWT cookie
        fetch('/api/auth/logout', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include' // Include cookies
        })
        .then(response => {
            if (response.ok) {
                if (window.showToast) {
                    window.showToast('Đăng xuất thành công!', 'success');
                }
                // Clear all localStorage and sessionStorage
                localStorage.clear();
                sessionStorage.clear();
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
            } else {
                throw new Error('Logout failed');
            }
        })
        .catch(error => {
            console.error('Logout error:', error);
            localStorage.clear();
            sessionStorage.clear();
            if (window.showToast) {
                window.showToast('Đăng xuất thành công!', 'success');
            }
            setTimeout(() => {
                window.location.reload();
            }, 1000);
        });
    };

    // Form submission
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const email = document.getElementById('loginEmail').value.trim();
            const password = document.getElementById('loginPassword').value.trim();
            
            // Validate
            let isValid = true;
            
            if (!email) {
                showError('loginEmailError', 'Email không được để trống');
                document.getElementById('loginEmail').classList.add('error');
                isValid = false;
            } else if (!validateEmail(email)) {
                showError('loginEmailError', 'Email không hợp lệ');
                document.getElementById('loginEmail').classList.add('error');
                isValid = false;
            }
            
            if (!password) {
                showError('loginPasswordError', 'Mật khẩu không được để trống');
                document.getElementById('loginPassword').classList.add('error');
                isValid = false;
            }
            
            if (isValid) {
                // Disable submit button
                const submitBtn = this.querySelector('.auth-submit-btn');
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang đăng nhập...';
                
                // Call real API
                fetch('/api/auth/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    credentials: 'include', // Include cookies for JWT
                    body: JSON.stringify({
                        email: email,
                        password: password
                    })
                })
                .then(async response => {
                    const data = await response.json().catch(() => ({}));
                    if (response.ok && data.token) {
                        window.showToast('Đăng nhập thành công!', 'success');
                        hideAuthModal();
                        sessionStorage.setItem('loginSuccess', '1');
                        setTimeout(() => {
                            window.location.reload();
                        }, 1500);
                    } else {
                        // Hiển thị lỗi trả về từ backend
                        let msg = data.message || 'Email hoặc mật khẩu không đúng';
                        window.showToast(msg, 'error');
                        // Hiển thị lỗi cụ thể lên form
                        showError('loginEmailError', msg);
                    }
                })
                .catch(error => {
                    console.error('Login error:', error);
                    window.showToast('Đăng nhập thất bại: ' + error.message, 'error');
                })
                .finally(() => {
                    // Reset button
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = '<i class="fas fa-sign-in-alt"></i> Đăng nhập';
                });
            }
        });
    }

    if (registerForm) {
        registerForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const fullName = document.getElementById('registerName').value.trim();
            const email = document.getElementById('registerEmail').value.trim();
            const password = document.getElementById('registerPassword').value.trim();
            const confirmPassword = document.getElementById('registerConfirmPassword').value.trim();
//            const agreeTerms = document.getElementById('agreeTerms').checked;
            
            // Validate
            let isValid = true;
            
            if (!fullName) {
                showError('registerNameError', 'Họ tên không được để trống');
                document.getElementById('registerName').classList.add('error');
                isValid = false;
            }
            
            if (!email) {
                showError('registerEmailError', 'Email không được để trống');
                document.getElementById('registerEmail').classList.add('error');
                isValid = false;
            } else if (!validateEmail(email)) {
                showError('registerEmailError', 'Email không hợp lệ');
                document.getElementById('registerEmail').classList.add('error');
                isValid = false;
            }
            
            if (!password) {
                showError('registerPasswordError', 'Mật khẩu không được để trống');
                document.getElementById('registerPassword').classList.add('error');
                isValid = false;
            } else if (!validatePassword(password)) {
                showError('registerPasswordError', 'Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số');
                document.getElementById('registerPassword').classList.add('error');
                isValid = false;
            }
            
            if (!confirmPassword) {
                showError('registerConfirmPasswordError', 'Xác nhận mật khẩu không được để trống');
                document.getElementById('registerConfirmPassword').classList.add('error');
                isValid = false;
            } else if (confirmPassword !== password) {
                showError('registerConfirmPasswordError', 'Mật khẩu xác nhận không khớp');
                document.getElementById('registerConfirmPassword').classList.add('error');
                isValid = false;
            }
            
//            if (!agreeTerms) {
//                showError('agreeTermsError', 'Bạn phải đồng ý với điều khoản & chính sách');
//                // Focus và scroll đến checkbox
//                const agreeCheckbox = document.getElementById('agreeTerms');
//                if (agreeCheckbox) {
//                    agreeCheckbox.focus();
//                    agreeCheckbox.scrollIntoView({behavior: 'smooth', block: 'center'});
//                }
//                // Hiển thị warning phía trên form nếu chưa có
//                let warning = document.getElementById('agreeTermsWarning');
//                if (!warning) {
//                    warning = document.createElement('div');
//                    warning.id = 'agreeTermsWarning';
//                    warning.className = 'warning-message';
//                    warning.innerHTML = '<i class="fas fa-exclamation-triangle"></i> Bạn phải đồng ý với điều khoản & chính sách để tiếp tục đăng ký.';
//                    const form = document.getElementById('registerFormElement');
//                    if (form && form.parentNode) {
//                        form.parentNode.insertBefore(warning, form);
//                    }
//                }
//                // Thêm class error cho checkbox container để highlight
//                const checkboxContainer = agreeCheckbox ? agreeCheckbox.closest('.checkbox-group') : null;
//                if (checkboxContainer) {
//                    checkboxContainer.classList.add('error');
//                }
//                isValid = false;
//            } else {
//                // Nếu đã tick thì ẩn warning và error
//                const warning = document.getElementById('agreeTermsWarning');
//                if (warning) warning.remove();
////                hideError('agreeTermsError');
//                // Xóa class error cho checkbox container
////                const agreeCheckbox = document.getElementById('agreeTerms');
//                const checkboxContainer = agreeCheckbox ? agreeCheckbox.closest('.checkbox-group') : null;
//                if (checkboxContainer) {
//                    checkboxContainer.classList.remove('error');
//                }
//            }
            
            if (isValid) {
                // Disable submit button
                const submitBtn = this.querySelector('.auth-submit-btn');
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang đăng ký...';
                
                // Call real API
                fetch('/api/auth/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    credentials: 'include', // Include cookies for JWT
                    body: JSON.stringify({
                        fullName: fullName,
                        email: email,
                        password: password,
                        confirmPassword: confirmPassword
                    })
                })
                .then(async response => {
                    const data = await response.json().catch(() => ({}));
                    if (response.ok && data.success) {
                        window.showToast('Đăng ký thành công! Vui lòng đăng nhập.', 'success');
                        switchForm('login');
                    } else {
                        // Hiển thị lỗi trả về từ backend
                        let msg = data.message || 'Đăng ký thất bại';
                        window.showToast(msg, 'error');
                        // Hiển thị lỗi cụ thể lên form nếu có
                        if (msg.toLowerCase().includes('email')) {
                            showError('registerEmailError', msg);
                        }
                        if (msg.toLowerCase().includes('mật khẩu')) {
                            showError('registerPasswordError', msg);
                        }
                    }
                })
                .catch(error => {
                    console.error('Register error:', error);
                    window.showToast('Đăng ký thất bại: ' + error.message, 'error');
                })
                .finally(() => {
                    // Reset button
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = '<i class="fas fa-user-plus"></i> Đăng ký';
                });
            }
        });
    }

    // Initialize real-time validation
    setupRealTimeValidation();

    // Keyboard shortcuts
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && authModal.classList.contains('show')) {
            hideAuthModal();
        }
    });

    // Header desktop
    const headerLoginBtn = document.getElementById('headerLoginBtn');
    if (headerLoginBtn) {
        headerLoginBtn.addEventListener('click', function(e) {
            e.preventDefault();
            showAuthModal('login');
        });
    }
    const headerRegisterBtn = document.getElementById('headerRegisterBtn');
    if (headerRegisterBtn) {
        headerRegisterBtn.addEventListener('click', function(e) {
            e.preventDefault();
            showAuthModal('register');
        });
    }
    // Mobile menu
    const mobileLoginBtn = document.getElementById('mobileLoginBtn');
    if (mobileLoginBtn) {
        mobileLoginBtn.addEventListener('click', function(e) {
            e.preventDefault();
            showAuthModal('login');
        });
    }
    const mobileRegisterBtn = document.getElementById('mobileRegisterBtn');
    if (mobileRegisterBtn) {
        mobileRegisterBtn.addEventListener('click', function(e) {
            e.preventDefault();
            showAuthModal('register');
        });
    }
}); 

// --- ĐẢM BẢO FORM QUÊN MẬT KHẨU ĐƯỢC BẮT SỰ KIỆN ĐÚNG ---
document.addEventListener('DOMContentLoaded', function() {
    var forgotFormElement = document.getElementById('forgotFormElement');
    if (forgotFormElement) {
        forgotFormElement.addEventListener('submit', function(e) {
            e.preventDefault();
            var email = document.getElementById('forgotEmail').value;
            var errorSpan = document.getElementById('forgotEmailError');
            if (errorSpan) errorSpan.textContent = '';
            fetch('/api/users/forgot-password', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'email=' + encodeURIComponent(email)
            })
            .then(res => res.json())
            .then(data => {
                if (data.message) {
                    if (errorSpan) {
                        errorSpan.style.color = data.message.includes('gửi') ? '#28a745' : '#e74c3c';
                        errorSpan.textContent = data.message;
                    } else {
                        alert(data.message);
                    }
                    if (data.message.includes('gửi')) {
                        setTimeout(function() {
                            // Chuyển về form login
                            const authForms = document.querySelectorAll('.auth-form');
                            authForms.forEach(form => form.classList.remove('active'));
                            var loginForm = document.getElementById('loginForm');
                            if (loginForm) loginForm.classList.add('active');
                            if (typeof showToast === 'function') {
                                showToast('Mật khẩu mới đã được gửi tới email của bạn. Vui lòng kiểm tra!', 'success');
                            } else {
                                alert('Mật khẩu mới đã được gửi tới email của bạn. Vui lòng kiểm tra!');
                            }
                            var loginEmail = document.getElementById('loginEmail');
                            if (loginEmail) loginEmail.focus();
                        }, 1200);
                    }
                }
            })
            .catch((err) => {
                if (errorSpan) {
                    errorSpan.style.color = '#e74c3c';
                    errorSpan.textContent = 'Có lỗi xảy ra, vui lòng thử lại.';
                } else {
                    alert('Có lỗi xảy ra, vui lòng thử lại.');
                }
            });
        });
    }
}); 

document.addEventListener('DOMContentLoaded', function() {
    var backToLoginBtn = document.getElementById('backToLoginForm');
    if (backToLoginBtn) {
        backToLoginBtn.addEventListener('click', function(e) {
            e.preventDefault();
            if (typeof switchForm === 'function') {
                switchForm('login');
            }
        });
    }
}); 