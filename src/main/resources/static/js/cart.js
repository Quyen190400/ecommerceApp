// Hàm dùng chung xử lý khi tài khoản bị vô hiệu hóa
function handleAuthDeactivated(status, body) {
  // Nếu đang ở trang cart (có phần tử cartItems), chỉ show UI giữa trang, không show toast
  if (status === 403 && body.message === "Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên.") {
    if (!document.getElementById('cartItems')) {
      if (typeof showToast === 'function') {
        showToast(body.message, 'error');
      } else {
        alert(body.message);
      }
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
      // Loại trừ theo id hoặc class
      if (!excludeIds.includes(el.id) && !excludeClasses.some(cls => el.classList.contains(cls))) {
        el.disabled = true;
      }
    });
    return true;
  }
  return false;
}
// Cách dùng:
// fetch(...).then(res => res.json().then(data => ({status: res.status, body: data}))).then(({status, body}) => { if (handleAuthDeactivated(status, body)) return; ... });

// Cart functionality
document.addEventListener('DOMContentLoaded', function() {
    loadCart();
    updateCartBadge();
    bindAddCartEvents();
});

// Global variables to track selected items
let cartItems = [];
let selectedItems = new Set();

// Load cart data
function loadCart() {
    fetch('/api/cart', {
        credentials: 'include'
    })
        .then(response => response.json().then(data => ({status: response.status, body: data})))
        .then(({status, body}) => {
            if (handleAuthDeactivated(status, body)) {
                showInactiveCart();
                return;
            }
            if (body && body.items && body.items.length > 0) {
                cartItems = body.items;
                // Select all items by default
                selectedItems = new Set(cartItems.map(item => item.id));
                renderCartItems(body.items);
                updateCartSummary();
            } else {
                showEmptyCart('Hãy chọn sản phẩm để tiến hành đặt hàng');
            }
        })
        .catch(error => {
            console.error('Error loading cart:', error);
            showEmptyCart('Có lỗi xảy ra khi tải giỏ hàng');
        });
}

// Hiển thị giao diện khi tài khoản bị vô hiệu hóa
function showInactiveCart() {
    const cartItemsContainer = document.getElementById('cartItems');
    const cartSummary = document.getElementById('cartSummary');
    if (cartItemsContainer) {
        cartItemsContainer.innerHTML = `
            <div class="empty-cart" style="text-align:center;padding:60px 20px;">
                <i class="fas fa-user-slash" style="font-size:3rem;color:#e57373;"></i>
                <h2 style="color:#e57373;">Tài khoản của bạn đã bị vô hiệu hóa.<br>Vui lòng liên hệ quản trị viên.</h2>
            </div>
        `;
    }
    if (cartSummary) {
        cartSummary.style.display = 'none';
    }
}

// Render cart items
function renderCartItems(items) {
    const cartItemsContainer = document.getElementById('cartItems');
    
    const itemsHTML = items.map(item => `
        <div class="cart-item" data-item-id="${item.id}">
            <div class="item-checkbox">
                <input type="checkbox" 
                       id="checkbox-${item.id}" 
                       checked 
                       onchange="toggleItemSelection(${item.id})">
            </div>
            <img src="${item.productImage}" alt="${item.productName}" class="item-image" onerror="this.src='/images/product-placeholder.jpg'">
            <div class="item-info">
                <h3>${item.productName}</h3>
                <div class="item-price">${formatPrice(item.unitPrice)}</div>
            </div>
            <div class="quantity-controls">
                <button class="quantity-btn" onclick="updateQuantity(${item.id}, ${item.quantity - 1})">-</button>
                <input type="number" class="quantity-input" value="${item.quantity}" min="1" 
                       onchange="updateQuantity(${item.id}, this.value)">
                <button class="quantity-btn" onclick="updateQuantity(${item.id}, ${item.quantity + 1})">+</button>
            </div>
            <div class="item-total">${formatPrice(item.totalPrice)}</div>
            <div class="item-actions">
                <button class="remove-btn" onclick="removeFromCart(${item.id})">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        </div>
    `).join('');
    
    cartItemsContainer.innerHTML = itemsHTML;
}

// Toggle item selection
function toggleItemSelection(itemId) {
    const checkbox = document.getElementById(`checkbox-${itemId}`);
    
    if (checkbox.checked) {
        selectedItems.add(itemId);
    } else {
        selectedItems.delete(itemId);
    }
    
    updateCartSummary();
}

// Get selected items
function getSelectedItems() {
    return cartItems.filter(item => selectedItems.has(item.id));
}

// Update cart summary based on selected items
function updateCartSummary() {
    const selectedItemsList = getSelectedItems();
    const totalItems = selectedItemsList.reduce((sum, item) => sum + item.quantity, 0);
    const totalPrice = selectedItemsList.reduce((sum, item) => sum + parseFloat(item.totalPrice), 0);
    
    document.getElementById('totalItems').textContent = totalItems;
    document.getElementById('totalPrice').textContent = formatPrice(totalPrice);
    document.getElementById('finalTotal').textContent = formatPrice(totalPrice);
    
    // Enable/disable checkout button
    const checkoutBtn = document.getElementById('checkoutBtn');
    if (selectedItems.size > 0) {
        checkoutBtn.disabled = false;
        checkoutBtn.innerHTML = '<i class="fas fa-shopping-bag"></i> Đặt hàng';
    } else {
        checkoutBtn.disabled = true;
        checkoutBtn.innerHTML = '<i class="fas fa-shopping-bag"></i> Chọn sản phẩm';
    }
}

// Show empty cart message
function showEmptyCart(message) {
    const cartItemsContainer = document.getElementById('cartItems');
    if (!cartItemsContainer) return;
    const cartSummary = document.getElementById('cartSummary');
    
    cartItemsContainer.innerHTML = `
        <div class="empty-cart" style="text-align:center;padding:60px 20px;">
            <i class="fas fa-shopping-cart" style="font-size:3rem;color:#bdbdbd;"></i>
            <h2 style="color:#bdbdbd;">${message}</h2>
            <a href="/#products" class="continue-shopping">Tiếp tục mua sắm</a>
        </div>
    `;
    
    // Luôn hiển thị tổng quan, cập nhật về 0
    cartSummary.style.display = 'block';
    document.getElementById('totalItems').textContent = 0;
    document.getElementById('totalPrice').textContent = '0đ';
    document.getElementById('finalTotal').textContent = '0đ';
    
    // Disable checkout button khi giỏ hàng trống
    const checkoutBtn = document.getElementById('checkoutBtn');
    if (checkoutBtn) {
        checkoutBtn.disabled = true;
        // checkoutBtn.innerHTML = '<i class="fas fa-shopping-bag"></i> Chọn sản phẩm';
        checkoutBtn.title = 'Vui lòng chọn sản phẩm trước khi đặt hàng';
    }
}

// Update quantity
function updateQuantity(itemId, newQuantity) {
    if (newQuantity <= 0) {
        removeFromCart(itemId);
        return;
    }
    
    fetch(`/api/cart/items/${itemId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({
            quantity: parseInt(newQuantity)
        })
    })
    .then(response => response.json().then(data => ({status: response.status, body: data})))
    .then(({status, body}) => {
        if (handleAuthDeactivated(status, body)) return;
        if (body.message) {
            showToast(body.message, 'success');
            loadCart(); // Reload cart to update totals
            updateCartBadge();
        }
    })
    .catch(error => {
        console.error('Error updating quantity:', error);
        showToast('Có lỗi xảy ra khi cập nhật số lượng', 'error');
    });
}

// Remove item from cart
function removeFromCart(itemId) {
    fetch(`/api/cart/items/${itemId}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include'
    })
    .then(response => response.json().then(data => ({status: response.status, body: data})))
    .then(({status, body}) => {
        if (handleAuthDeactivated(status, body)) return;
        if (body.message) { // Đã sửa từ data.message sang body.message
            showToast(body.message, 'success');
            loadCart(); // Reload cart
            updateCartBadge();
        }
    })
    .catch(error => {
        console.error('Error removing item:', error);
        showToast('Có lỗi xảy ra khi xóa sản phẩm', 'error');
    });
}

// Checkout function
function checkout() {
    const selectedItemsList = getSelectedItems();
    
    if (selectedItemsList.length === 0) {
        showToast('Vui lòng chọn ít nhất một sản phẩm để đặt hàng.', 'warning');
        return;
    }
    
    // Show order information form
    const orderInfoForm = document.getElementById('orderInfoForm');
    const checkoutBtn = document.getElementById('checkoutBtn');
    
    if (orderInfoForm.style.display === 'none') {
        // Show form first
        orderInfoForm.style.display = 'block';
        checkoutBtn.innerHTML = '<i class="fas fa-check"></i> Xác nhận đặt hàng';
        return;
    }
    
    // Validate form
    const shippingAddress = document.getElementById('shippingAddress').value.trim();
    const shippingMethod = document.getElementById('shippingMethod').value;
    const phoneNumber = document.getElementById('phoneNumber').value.trim();
    const orderNotes = document.getElementById('orderNotes').value.trim();
    
    if (!shippingAddress) {
        showToast('Vui lòng nhập địa chỉ giao hàng.', 'warning');
        return;
    }
    
    if (!shippingMethod) {
        showToast('Vui lòng chọn phương thức giao hàng.', 'warning');
        return;
    }
    
    if (!phoneNumber) {
        showToast('Vui lòng nhập số điện thoại.', 'warning');
        return;
    }
    // Validate số điện thoại 10 hoặc 11 số, không chứa chữ
    if (!/^\d{10,11}$/.test(phoneNumber)) {
        showToast('Số điện thoại phải là 10 hoặc 11 số, không chứa ký tự chữ.', 'warning');
        return;
    }
    
    // Disable button and show loading
    checkoutBtn.disabled = true;
    checkoutBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';
    
    // Prepare order data
    const orderData = {
        selectedItemIds: selectedItemsList.map(item => item.id),
        shippingAddress: shippingAddress,
        shippingMethod: shippingMethod,
        phoneNumber: phoneNumber,
        orderNotes: orderNotes
    };
    
    fetch('/api/orders', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify(orderData)
    })
    .then(response => response.json().then(data => ({status: response.status, body: data})))
    .then(({status, body}) => {
        if (handleAuthDeactivated(status, body)) return;
        if (body.message) {
            showToast(body.message, 'success');
            
            // Reload cart to reflect removed items
            loadCart();
            updateCartBadge();
            
            // Redirect to order success page
            setTimeout(() => {
                window.location.href = `/order-success?orderId=${body.order.id}`;
            }, 1500);
        } else {
            throw new Error(body.message || 'Có lỗi xảy ra');
        }
    })
    .catch(error => {
        console.error('Error during checkout:', error);
        showToast('Có lỗi xảy ra khi đặt hàng', 'error');
        checkoutBtn.disabled = false;
        checkoutBtn.innerHTML = '<i class="fas fa-check"></i> Xác nhận đặt hàng';
    });
}

// Update cart badge
function updateCartBadge() {
    fetch('/api/cart/count', {
        credentials: 'include'
    })
    .then(response => response.json().then(data => ({status: response.status, body: data})))
    .then(({status, body}) => {
        if (handleAuthDeactivated(status, body)) return;
        if (!body) return; // Đã xử lý ở trên
        const cartBadge = document.getElementById('cartBadge');
        if (cartBadge) {
            if (body.count && body.count > 0) {
                cartBadge.textContent = body.count;
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

// Format price
function formatPrice(price) {
    if (typeof price === 'string') {
        price = parseFloat(price);
    }
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(price);
}

// Show toast notification
function showToast(message, type = 'success') {
    // Create toast element if it doesn't exist
    let toast = document.getElementById('toast');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'toast';
        toast.className = 'toast';
        toast.innerHTML = `
            <div class="toast-content">
                <i class="toast-icon"></i>
                <span class="toast-message"></span>
            </div>
            <button class="toast-close">
                <i class="fas fa-times"></i>
            </button>
        `;
        document.body.appendChild(toast);
    }
    
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
    
    // Auto hide after 3 seconds
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
    
    // Close button functionality
    const closeBtn = toast.querySelector('.toast-close');
    if (closeBtn) {
        closeBtn.onclick = () => {
            toast.classList.remove('show');
        };
    }
}

window.addToCartIndex = function(productId) {
    fetch('/api/cart/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({
            productId: parseInt(productId),
            quantity: 1
        })
    })
    .then(response => response.json().then(data => ({status: response.status, body: data})))
    .then(({status, body}) => {
        if (status === 401) {
            if (window.showToast) {
                window.showToast('Vui lòng đăng nhập để sử dụng tính năng giỏ hàng.', 'warning');
            } else {
                showToast('Vui lòng đăng nhập để sử dụng tính năng giỏ hàng.', 'warning');
            }
            if (window.showAuthModal) {
                window.showAuthModal('login');
            }
            return;
        }
        if (status === 403 && body && body.message === "Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên.") {
            showToast(body.message, 'error');
            return;
        }
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

function bindAddCartEvents() {
    document.querySelectorAll('.add-cart-btn').forEach(function(btn) {
        btn.onclick = function() {
            var productId = btn.getAttribute('data-product-id');
            if (productId) {
                window.addToCartIndex(productId);
            }
        };
    });
}

document.addEventListener('DOMContentLoaded', function() {
    bindAddCartEvents();
    // Nếu có render lại sản phẩm bằng JS, hãy gọi lại bindAddCartEvents()
});