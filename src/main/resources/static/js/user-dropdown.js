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
        // Clear cookies manually first
        document.cookie = "jwt_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
        document.cookie = "JSESSIONID=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
        document.cookie = "remember-me=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
        
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
                // Redirect to home page immediately
                window.location.href = '/?logout=true';
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
            // Redirect immediately even on error
            window.location.href = '/?logout=true';
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

// === SEARCH DROPDOWN SUGGESTION FOR HEADER ===
(function() {
    let allProducts = [];
    let suggestList = document.getElementById('searchSuggestList');
    let searchInput = document.getElementById('productSearchInput');
    let fetched = false;
    let blurTimeout = null;

    async function fetchProductsForSuggest() {
        try {
            let res = await fetch('/api/products');
            if (res.ok) {
                let data = await res.json();
                allProducts = data.filter(p => p.status === true || p.status === 1);
                fetched = true;
            }
        } catch (e) { allProducts = []; }
    }

    function renderSuggestList(keyword) {
        if (!suggestList) return;
        if (!keyword || keyword.trim() === '') {
            suggestList.classList.remove('show');
            suggestList.style.display = 'none';
            suggestList.innerHTML = '';
            return;
        }
        const kw = keyword.trim().toLowerCase();
        const matches = allProducts.filter(p => p.name && p.name.toLowerCase().includes(kw));
        if (matches.length === 0) {
            suggestList.classList.remove('show');
            suggestList.style.display = 'none';
            suggestList.innerHTML = '';
            return;
        }
        suggestList.innerHTML = matches.slice(0, 8).map(p =>
            `<div class="search-suggest-item" data-id="${p.id}">${p.name}</div>`
        ).join('');
        suggestList.classList.add('show');
        suggestList.style.display = 'block';
        suggestList.style.zIndex = '9999';
        // Gắn sự kiện click
        suggestList.querySelectorAll('.search-suggest-item').forEach(item => {
            item.onclick = function() {
                window.location.href = '/product/' + item.getAttribute('data-id');
            };
        });
    }

    if (searchInput && suggestList) {
        searchInput.addEventListener('focus', async function() {
            if (!fetched) {
                await fetchProductsForSuggest();
            }
            if (this.value) renderSuggestList(this.value);
        });
        searchInput.addEventListener('input', async function() {
            if (!fetched) {
                await fetchProductsForSuggest();
            }
            renderSuggestList(this.value);
        });
        searchInput.addEventListener('blur', function() {
            blurTimeout = setTimeout(() => {
                suggestList.classList.remove('show');
                suggestList.style.display = 'none';
            }, 180);
        });
        suggestList.addEventListener('mousedown', function(e) {
            e.preventDefault();
            clearTimeout(blurTimeout);
        });
    }
})(); 