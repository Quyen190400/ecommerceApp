// User Dropdown Functionality
// Toast notification function (make available globally before DOMContentLoaded)
window.showToast = function(message, type = 'info', duration = 3000) {
    let toastContainer = document.querySelector('.toast-container');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.className = 'toast-container';
        document.body.appendChild(toastContainer);
    }
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <div class="toast-header">
            <div class="toast-title">${type.charAt(0).toUpperCase() + type.slice(1)}</div>
            <button class="toast-close" onclick="this.parentElement.parentElement.remove()">&times;</button>
        </div>
        <div class="toast-message">${message}</div>
    `;
    toastContainer.appendChild(toast);
    setTimeout(() => { toast.classList.add('show'); }, 10);
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => { if (toast.parentElement) toast.remove(); }, 300);
    }, duration);
};

document.addEventListener('DOMContentLoaded', function() {
    
    // Desktop user dropdown
    const userDropdownBtn = document.getElementById('userDropdownBtn');
    const userDropdownMenu = document.getElementById('userDropdownMenu');
    
    // Mobile user dropdown
    const mobileUserBtn = document.getElementById('mobileUserBtn');
    const mobileUserMenu = document.getElementById('mobileUserMenu');
    
    // Theme toggle
    const themeToggle = document.getElementById('themeToggleBtn');
    const mobileThemeToggle = document.getElementById('mobileThemeToggleBtn');
    
    // Toggle desktop dropdown
    if (userDropdownBtn && userDropdownMenu) {
        userDropdownBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            userDropdownMenu.classList.toggle('show');
        });
    }
    
    // Toggle mobile dropdown
    if (mobileUserBtn && mobileUserMenu) {
        mobileUserBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            mobileUserMenu.classList.toggle('show');
        });
    }
    
    // Close dropdowns when clicking outside
    document.addEventListener('click', function(e) {
        if (userDropdownMenu && !userDropdownMenu.contains(e.target) && !userDropdownBtn.contains(e.target)) {
            userDropdownMenu.classList.remove('show');
        }
        
        if (mobileUserMenu && !mobileUserMenu.contains(e.target) && !mobileUserBtn.contains(e.target)) {
            mobileUserMenu.classList.remove('show');
        }
    });
    
    // XÓA TOÀN BỘ ĐOẠN CODE LIÊN QUAN ĐẾN CHUYỂN THEME (toggleTheme, loadTheme, event click cho themeToggle, mobileThemeToggle)
    
    // Logout function - Updated for JWT
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
                // Close dropdowns
                if (userDropdownMenu) {
                    userDropdownMenu.classList.remove('show');
                }
                if (mobileUserMenu) {
                    mobileUserMenu.classList.remove('show');
                }
                // Clear all localStorage and sessionStorage
                localStorage.clear();
                sessionStorage.clear();
                // Show success message
                if (window.showToast) {
                    window.showToast('Đăng xuất thành công!', 'success');
                }
                sessionStorage.setItem('logoutSuccess', '1');
                // Redirect to home page
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
            } else {
                throw new Error('Logout failed');
            }
        })
        .catch(error => {
            console.error('Logout error:', error);
            // Even if API fails, redirect to login
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
    
    // Function to check authentication status
    window.checkAuthStatus = function() {
        return fetch('/api/auth/me', {
            method: 'GET',
            credentials: 'include' // Include cookies
        })
        .then(response => response.json())
        .then(data => {
            return data.authenticated;
        })
        .catch(error => {
            console.error('Auth check error:', error);
            return false;
        });
    };
    
    // Function to get current user info
    window.getCurrentUser = function() {
        return fetch('/api/auth/me', {
            method: 'GET',
            credentials: 'include' // Include cookies
        })
        .then(response => response.json())
        .catch(error => {
            console.error('Get user error:', error);
            return null;
        });
    };
    
    // Cart click handler
    window.handleCartClick = function(event) {
        event.preventDefault();
        // Check authentication status
        checkAuthStatus()
            .then(isAuthenticated => {
                if (isAuthenticated) {
                    // User is authenticated, redirect to cart
                    window.location.href = '/cart';
                } else {
                    // User is not authenticated, show message and open login modal
                    if (window.showToast) {
                        window.showToast('Vui lòng đăng nhập để sử dụng tính năng giỏ hàng.', 'warning');
                    } else {
                        alert('Vui lòng đăng nhập để sử dụng tính năng giỏ hàng.');
                    }
                    if (window.showAuthModal) {
                        window.showAuthModal('login');
                    }
                    // Do NOT redirect
                }
            })
            .catch(error => {
                console.error('Auth check error:', error);
                if (window.showToast) {
                    window.showToast('Vui lòng đăng nhập để sử dụng tính năng giỏ hàng.', 'warning');
                } else {
                    alert('Vui lòng đăng nhập để sử dụng tính năng giỏ hàng.');
                }
                if (window.showAuthModal) {
                    window.showAuthModal('login');
                }
                // Do NOT redirect
            });
    };
}); 