// Hàm dùng chung xử lý khi tài khoản bị vô hiệu hóa
function handleAuthDeactivated(status, body) {
  if (status === 403 && body.message === "Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên.") {
    if (typeof showToast === 'function') {
      showToast(body.message, 'error');
    } else {
      alert(body.message);
    }
    document.querySelectorAll('button, input[type=checkbox], input[type=submit]').forEach(el => {
      const excludeIds = [
        'userDropdownBtn',
        'mobileUserBtn',
        'themeToggleBtn',
        'mobileThemeToggleBtn'
      ];
      const excludeClasses = [
        'logout-btn'
      ];
      if (!excludeIds.includes(el.id) && !excludeClasses.some(cls => el.classList.contains(cls))) {
        el.disabled = true;
      }
    });
    return true;
  }
  return false;
}
// Product Detail Page JavaScript
document.addEventListener('DOMContentLoaded', function() {
    
    // Quantity controls
    window.decreaseQuantity = function() {
        const quantityInput = document.getElementById('quantity');
        const currentValue = parseInt(quantityInput.value);
        if (currentValue > 1) {
            quantityInput.value = currentValue - 1;
        }
    };
    
    window.increaseQuantity = function() {
        const quantityInput = document.getElementById('quantity');
        const currentValue = parseInt(quantityInput.value);
        const maxValue = parseInt(quantityInput.max);
        if (currentValue < maxValue) {
            quantityInput.value = currentValue + 1;
        }
    };
    
    // Image gallery
    window.changeMainImage = function(imageUrl) {
        const mainImage = document.getElementById('mainImage');
        if (mainImage) {
            mainImage.src = imageUrl;
            
            // Update active thumbnail
            const thumbnails = document.querySelectorAll('.thumbnail');
            thumbnails.forEach(thumb => {
                thumb.classList.remove('active');
                if (thumb.querySelector('img').src === imageUrl) {
                    thumb.classList.add('active');
                }
            });
        }
    };
    
    // Tab functionality
    window.showTab = function(tabName) {
        // Hide all tab contents
        const tabContents = document.querySelectorAll('.tab-content');
        tabContents.forEach(content => {
            content.classList.remove('active');
        });
        
        // Remove active class from all tab buttons
        const tabButtons = document.querySelectorAll('.tab-btn');
        tabButtons.forEach(btn => {
            btn.classList.remove('active');
        });
        
        // Show selected tab content
        const selectedTab = document.getElementById(tabName);
        if (selectedTab) {
            selectedTab.classList.add('active');
        }
        
        // Add active class to clicked button
        event.target.classList.add('active');
    };
    
    // Add to cart functionality
    window.addToCart = function() {
        const quantity = document.getElementById('quantity').value;
        const productId = getProductIdFromUrl();
        fetch('/api/cart/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify({
                productId: parseInt(productId),
                quantity: parseInt(quantity)
            })
        })
        .then(response => {
            if (response.status === 401) {
                // User not logged in
                if (window.showToast) {
                    window.showToast('Vui lòng đăng nhập để sử dụng tính năng giỏ hàng.', 'warning');
                } else {
                    showToast('Vui lòng đăng nhập để sử dụng tính năng giỏ hàng.', 'warning');
                }
                if (window.showAuthModal) {
                    window.showAuthModal('login');
                }
                // Do NOT redirect
                return;
            }
            return response.json().then(data => ({status: response.status, body: data}));
        })
        .then(res => {
            if (!res) return;
            const {status, body} = res;
            if (handleAuthDeactivated(status, body)) return;
            if (body && body.message) {
                if (window.showToast) {
                    window.showToast(body.message, 'success');
                } else {
                    showToast(body.message, 'success');
                }
                updateCartBadge();
            }
        })
        .catch(error => {
            console.error('Error adding to cart:', error);
            if (window.showToast) {
                window.showToast('Có lỗi xảy ra khi thêm vào giỏ hàng', 'error');
            } else {
                showToast('Có lỗi xảy ra khi thêm vào giỏ hàng', 'error');
            }
        });
    };
    
    // Buy now functionality
    window.buyNow = function() {
        // Hiển thị popup
        document.getElementById('buyNowModal').classList.add('show');
        document.getElementById('buyNowModal').style.display = 'flex';
        fetch('/api/user/profile', { credentials: 'include' })
            .then(res => res.ok ? res.json() : null)
            .then(data => {
                if (data && data.phoneNumber) {
                    document.getElementById('buyNowPhone').value = data.phoneNumber;
                }
            });
    };
    window.closeBuyNowModal = function() {
        document.getElementById('buyNowModal').classList.remove('show');
        document.getElementById('buyNowModal').style.display = 'none';
    };
    window.submitBuyNowForm = function(event) {
        event.preventDefault();
        const productId = getProductIdFromUrl();
        const quantity = document.getElementById('quantity').value;
        const address = document.getElementById('buyNowAddress').value;
        const phone = document.getElementById('buyNowPhone').value;
        const payment = document.getElementById('buyNowPayment').value;
        const receiveMethod = document.getElementById('buyNowReceiveMethod').value;
        const note = document.getElementById('buyNowNote').value;
        let shippingMethod = '';
        if (payment === 'COD') shippingMethod = 'STANDARD';
        else if (payment === 'BANK') shippingMethod = 'EXPRESS';
        fetch('/api/orders/buy-now', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({
                productId: parseInt(productId),
                quantity: parseInt(quantity),
                shippingAddress: address,
                phoneNumber: phone,
                paymentMethod: payment,
                shippingMethod: shippingMethod,
                receiveMethod: receiveMethod,
                orderNotes: note
            })
        })
        .then(res => {
            if (res.status === 401) {
                // Token hết hạn, show login modal
                if (window.showToast) {
                    window.showToast('Vui lòng đăng nhập để mua hàng.', 'warning');
                } else {
                    showToast('Vui lòng đăng nhập để mua hàng.', 'warning');
                }
                if (window.showAuthModal) {
                    window.showAuthModal('login');
                }
                // Do NOT redirect
                return null;
            }
            return res.json().then(data => ({status: res.status, body: data}));
        })
        .then(res => {
            if (!res) return;
            const {status, body} = res;
            if (status === 403 && body && body.message === "Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên.") {
                showToast(body.message, 'error');
                return;
            }
            if (handleAuthDeactivated(status, body)) return;
            if (!body) return;
            if (body.success) {
                showToast('Đặt hàng thành công! Đang chuyển hướng...', 'success');
                closeBuyNowModal();
                setTimeout(() => { window.location.href = '/orders'; }, 1500);
            } else {
                showToast(body.message || 'Có lỗi xảy ra khi đặt hàng.', 'error');
            }
        })
        .catch(() => {
            showToast('Có lỗi xảy ra khi đặt hàng.', 'error');
        });
    };
    
    // Helper function to get product ID from URL
    function getProductIdFromUrl() {
        const pathParts = window.location.pathname.split('/');
        return pathParts[pathParts.length - 1];
    }
    
    // Update cart badge
    function updateCartBadge() {
        fetch('/api/cart/count', {
            credentials: 'include'
        })
        .then(response => {
            if (response.status === 401 || response.status === 403) {
                // Chưa đăng nhập, ẩn badge
                const cartBadge = document.getElementById('cartBadge');
                if (cartBadge) {
                    cartBadge.textContent = '';
                    cartBadge.style.display = 'none';
                }
                return null;
            }
            return response.json();
        })
        .then(data => {
            if (!data) return; // Đã xử lý ở trên
            const cartBadge = document.getElementById('cartBadge');
            if (cartBadge) {
                if (data.count && data.count > 0) {
                    cartBadge.textContent = data.count;
                    cartBadge.style.display = 'inline-block';
                } else {
                    cartBadge.textContent = '';
                    cartBadge.style.display = 'none';
                }
            }
        })
        .catch(error => {
            // Lỗi khác cũng ẩn badge
            const cartBadge = document.getElementById('cartBadge');
            if (cartBadge) {
                cartBadge.textContent = '';
                cartBadge.style.display = 'none';
            }
            console.error('Error updating cart badge:', error);
        });
    }
    
    // Initialize cart badge
    updateCartBadge();
    
    // Quantity input validation
    const quantityInput = document.getElementById('quantity');
    if (quantityInput) {
        quantityInput.addEventListener('change', function() {
            const value = parseInt(this.value);
            const min = parseInt(this.min);
            const max = parseInt(this.max);
            
            if (value < min) {
                this.value = min;
            } else if (value > max) {
                this.value = max;
            }
        });
    }
    
    // Image gallery initialization
    const thumbnails = document.querySelectorAll('.thumbnail');
    if (thumbnails.length > 0) {
        // Set first thumbnail as active
        thumbnails[0].classList.add('active');
    }
    
    // Smooth scroll for anchor links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
    
    // Add loading state to buttons
    const actionButtons = document.querySelectorAll('.add-to-cart-btn, .buy-now-btn');
    actionButtons.forEach(button => {
        button.addEventListener('click', function() {
            const originalText = this.innerHTML;
            this.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';
            this.disabled = true;
            
            // Re-enable after 2 seconds
            setTimeout(() => {
                this.innerHTML = originalText;
                this.disabled = false;
            }, 2000);
        });
    });
    
    // Product image zoom on hover (desktop only)
    if (window.innerWidth > 768) {
        const mainImage = document.getElementById('mainImage');
        if (mainImage) {
            mainImage.addEventListener('mouseenter', function() {
                this.style.transform = 'scale(1.1)';
            });
            
            mainImage.addEventListener('mouseleave', function() {
                this.style.transform = 'scale(1)';
            });
        }
    }
    
    // Keyboard shortcuts
    document.addEventListener('keydown', function(e) {
        // Ctrl/Cmd + Enter to buy now
        if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
            e.preventDefault();
            const buyNowBtn = document.querySelector('.buy-now-btn');
            if (buyNowBtn && !buyNowBtn.disabled) {
                buyNow();
            }
        }
        
        // Ctrl/Cmd + C to add to cart
        if ((e.ctrlKey || e.metaKey) && e.key === 'c') {
            e.preventDefault();
            const addToCartBtn = document.querySelector('.add-to-cart-btn');
            if (addToCartBtn && !addToCartBtn.disabled) {
                addToCart();
            }
        }
    });
    
    // Share functionality
    function shareProduct() {
        if (navigator.share) {
            navigator.share({
                title: document.querySelector('.product-title').textContent,
                text: document.querySelector('.product-title').textContent,
                url: window.location.href
            });
        } else {
            // Fallback: copy URL to clipboard
            navigator.clipboard.writeText(window.location.href).then(() => {
                showToast('Đã sao chép link sản phẩm!', 'success');
            });
        }
    }
    
    // Add share button if not exists
    if (!document.querySelector('.share-btn')) {
        const shareBtn = document.createElement('button');
        shareBtn.className = 'share-btn';
        shareBtn.innerHTML = '<i class="fas fa-share-alt"></i> Chia sẻ';
        shareBtn.onclick = shareProduct;
        
        const productActions = document.querySelector('.product-actions');
        if (productActions) {
            productActions.appendChild(shareBtn);
        }
    }
}); 