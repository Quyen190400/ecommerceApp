// ========== Cấu hình ========== //
const API_BASE = '/api/admin/products';
const API_CATEGORY = '/api/categories';
const PAGE_SIZE = 10;

// ========== State ========== //
let currentPage = 0;
let totalPages = 1;
let editingProduct = null;
let categories = [];

// ========== Helper ========== //
function formatVND(amount) {
  return amount?.toLocaleString('vi-VN', {style: 'currency', currency: 'VND'}) || '';
}
function escapeHTML(str) {
  return str?.replace(/[&<>'"]/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','\'':'&#39;','"':'&quot;'}[c])) || '';
}

function normalizeImageUrl(url) {
  if (!url) return '';
  if (url.startsWith('/')) return url;
  // Nếu chỉ là tên file, tự động prepent
  return '/uploads/images/' + url;
}

function handleAuthExpired() {
  if (window.showToast) {
    window.showToast('Vui lòng đăng nhập để tiếp tục', 'warning');
  }
  if (window.showAuthModal) {
    window.showAuthModal('login');
  }
}

// ========== Load loại trà từ category ========== //
async function loadTeaTypes() {
  try {
    const res = await globalFetch(API_CATEGORY);
    const data = await res.json();
    window.teaTypes = data.map(c => c.name);
    renderTeaTypeOptions();
  } catch (e) {
    window.teaTypes = [];
  }
}
function renderTeaTypeOptions(selected) {
  const select = document.getElementById('teaType');
  if (!select) return;
  select.innerHTML = window.teaTypes.map(type => `<option value="${type}" ${selected===type?'selected':''}>${type}</option>`).join('');
}

// ========== Load danh sách sản phẩm ========== //
async function loadProducts(page = 0) {
  const tbody = document.getElementById('productsTableBody');
  tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;">Đang tải...</td></tr>';
  try {
    const res = await globalFetch(`${API_BASE}?page=${page}&size=${PAGE_SIZE}`);
    const data = await res.json();
    renderProducts(data.content);
    renderPagination(data.totalPages, page);
    totalPages = data.totalPages;
    currentPage = page;
  } catch (e) {
    tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;color:red;">Không thể tải danh sách sản phẩm!</td></tr>';
  }
}

function renderProducts(products) {
  const tbody = document.getElementById('productsTableBody');
  tbody.innerHTML = '';
  if (!products || products.length === 0) {
    tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;color:#888;">Không có sản phẩm nào</td></tr>';
    return;
  }
  products.forEach(p => {
    const imgUrl = normalizeImageUrl(p.imageUrl);
    tbody.innerHTML += `
      <tr>
        <td><img src="${escapeHTML(imgUrl) || '/static/images/product-placeholder.jpg'}" alt="Ảnh"/></td>
        <td>${escapeHTML(p.name)}${p.stockQuantity === 0 ? '<span class="badge-outstock">Hết hàng</span>' : ''}</td>
        <td>${formatVND(p.price)}</td>
        <td>${p.stockQuantity}</td>
        <td>${escapeHTML(p.teaType)}</td>
        <td>${escapeHTML(p.origin)}</td>
        <td>
          <span class="badge-status ${p.status ? 'badge-active' : 'badge-inactive'}">
            ${p.status ? 'Hiển thị' : 'Đã ẩn'}
          </span>
        </td>
        <td>
          <button class="action-btn edit" title="Sửa" onclick="openEditProduct(${p.id})"><i class="fa fa-pen"></i></button>
          <button class="action-btn delete" title="Xoá" onclick="openDeleteProduct(${p.id})"><i class="fa fa-trash"></i></button>
          <button class="action-btn toggle${p.status ? ' active' : ''}" title="${p.status ? 'Ẩn' : 'Hiện'}" onclick="toggleStatus(${p.id})">
            <i class="fa fa-eye${p.status ? '-slash' : ''}"></i>
          </button>
        </td>
      </tr>
    `;
  });
}

function renderPagination(total, current) {
  const pag = document.getElementById('pagination');
  pag.innerHTML = '';
  for (let i = 0; i < total; i++) {
    pag.innerHTML += `<button class="${i === current ? 'active' : ''}" onclick="loadProducts(${i})">${i+1}</button>`;
  }
}

// ========== Modal Thêm/Sửa ========== //
document.getElementById('btnAddProduct').onclick = () => openAddProduct();
document.getElementById('btnCancelModal').onclick = closeProductModal;
document.getElementById('closeModal').onclick = closeProductModal;

function randomPngFileName() {
    return (crypto.randomUUID ? crypto.randomUUID() : (Date.now() + '-' + Math.floor(Math.random()*10000))) + '.png';
}
function openAddProduct() {
  editingProduct = null;
  document.getElementById('modalTitle').innerText = 'Thêm sản phẩm';
  document.getElementById('productForm').reset();
  document.getElementById('productId').value = '';
  // Random tên file PNG cho imageUrl khi mở form
  document.getElementById('imageUrl').value = randomPngFileName();
  showProductModal();
}

async function openEditProduct(id) {
  try {
    const res = await globalFetch(`/api/products/${id}`);
    if (!res.ok) throw new Error();
    const p = await res.json();
    editingProduct = p;
    document.getElementById('modalTitle').innerText = 'Sửa sản phẩm';
    document.getElementById('productId').value = p.id;
    document.getElementById('name').value = p.name;
    document.getElementById('description').value = p.description;
    // Khi edit, nếu imageUrl là /uploads/images/xxx.jpg thì chỉ show tên file
    let imgVal = p.imageUrl || '';
    if (imgVal.startsWith('/uploads/images/')) imgVal = imgVal.substring(15);
    document.getElementById('imageUrl').value = imgVal;
    document.getElementById('price').value = p.price;
    document.getElementById('stockQuantity').value = p.stockQuantity;
    document.getElementById('teaType').value = p.teaType;
    document.getElementById('tasteNote').value = p.tasteNote;
    document.getElementById('usageGuide').value = p.usageGuide;
    document.getElementById('healthBenefit').value = p.healthBenefit;
    document.getElementById('origin').value = p.origin;
    showProductModal();
    loadTeaTypes(); // Gọi loadTeaTypes khi mở modal sửa
    renderTeaTypeOptions(p.teaType); // Render loại trà đã chọn
  } catch {
    alert('Không thể tải thông tin sản phẩm!');
  }
}

function showProductModal() {
  document.getElementById('productModal').style.display = 'block';
  document.getElementById('modalOverlay').classList.add('show');
}
function closeProductModal() {
  document.getElementById('productModal').style.display = 'none';
  document.getElementById('modalOverlay').classList.remove('show');
}
document.getElementById('closeModal').onclick = closeProductModal;
document.getElementById('modalOverlay').onclick = closeProductModal;

// ========== Upload ảnh sản phẩm ========== //
let selectedImageFile = null;
const imageFileInput = document.getElementById('imageFile');
if (imageFileInput) {
  imageFileInput.addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (!file) return;
    selectedImageFile = file;
    // Random tên file giữ đuôi gốc
    let ext = '';
    if (file.name && file.name.lastIndexOf('.') !== -1) {
      ext = file.name.substring(file.name.lastIndexOf('.'));
    }
    const randomName = (crypto.randomUUID ? crypto.randomUUID() : (Date.now() + '-' + Math.floor(Math.random()*10000))) + ext;
    document.getElementById('imageUrl').value = randomName;
    showToast('Đã chọn ảnh, tên file: ' + randomName, 'info');
  });
}

// ========== Submit Thêm/Sửa ========== //
document.getElementById('productForm').onsubmit = async function(e) {
  e.preventDefault();
  let imageUrl = this.imageUrl.value.trim();
  const isEdit = !!document.getElementById('productId').value;

  // Bắt buộc chọn ảnh khi thêm mới
  if (!isEdit && !selectedImageFile) {
    alert('Vui lòng chọn hình ảnh sản phẩm!');
    return;
  }

  // Nếu sửa, nếu không chọn ảnh mới thì giữ nguyên imageUrl cũ
  if (isEdit && !selectedImageFile) {
    imageUrl = this.imageUrl.value.trim(); // giữ nguyên tên file ảnh cũ
  }
  // Nếu sửa và có chọn ảnh mới, imageUrl đã được random lại khi chọn file

  const data = {
    name: this.name.value.trim(),
    description: this.description.value,
    imageUrl: imageUrl,
    price: Number(this.price.value),
    stockQuantity: Number(this.stockQuantity.value),
    teaType: this.teaType.value,
    tasteNote: this.tasteNote.value,
    usageGuide: this.usageGuide.value,
    healthBenefit: this.healthBenefit.value,
    origin: this.origin.value,
    status: true
  };
  // Validate
  if (!data.name || isNaN(data.price) || data.price <= 0 || !data.imageUrl || !data.teaType) {
    alert('Vui lòng nhập đầy đủ và hợp lệ các trường bắt buộc: Tên sản phẩm, Giá > 0, Ảnh, Loại trà!');
    return;
  }
  try {
    let res;
    if (selectedImageFile) {
      const formData = new FormData();
      Object.entries(data).forEach(([k,v]) => formData.append(k, v));
      formData.append('file', selectedImageFile);
      if (isEdit) {
        res = await globalFetch(`${API_BASE}/`+document.getElementById('productId').value, {
          method: 'PUT',
          body: formData
        });
      } else {
        res = await globalFetch(API_BASE, {
          method: 'POST',
          body: formData
        });
      }
    } else {
      if (isEdit) {
        res = await globalFetch(`${API_BASE}/`+document.getElementById('productId').value, {
          method: 'PUT',
          headers: {'Content-Type':'application/json'},
          body: JSON.stringify(data)
        });
      } else {
        res = await globalFetch(API_BASE, {
          method: 'POST',
          headers: {'Content-Type':'application/json'},
          body: JSON.stringify(data)
        });
      }
    }
    let result = null;
    try {
      result = await res.json();
    } catch (parseError) {
      console.log('Response không phải JSON, có thể là text hoặc empty');
    }
    
    // Debug: log response status và result
    console.log('=== DEBUG RESPONSE ===');
    console.log('Response status:', res.status);
    console.log('Response URL:', res.url);
    console.log('Response headers:', res.headers);
    console.log('Response result:', result);
    console.log('Is edit mode:', isEdit);
    console.log('=====================');
    
    // Xử lý response theo status code
    if (res.status >= 400) {
      console.log('❌ ERROR: Status >= 400, showing error message');
      let errMsg = 'Có lỗi khi lưu sản phẩm!';
      if (result && result.message) errMsg = result.message;
      alert(errMsg);
      return;
    }
    
    // Thành công khi status < 400 (200, 201, etc.) - không cần kiểm tra result.message
    console.log('✅ SUCCESS: Status < 400, showing success message');
    closeProductModal();
    let msg = isEdit ? 'Cập nhật sản phẩm thành công!' : 'Thêm sản phẩm thành công!';
    // Chỉ dùng result.message nếu thực sự có, còn không thì dùng msg mặc định
    if (result && result.message && result.message.trim() !== '') {
      msg = result.message;
    }
    showToast(msg, 'success');
    loadProducts(currentPage);
    selectedImageFile = null;
  } catch (err) {
    console.error('Error in form submit:', err);
    if (err.message !== 'Unauthorized') {
      alert('Có lỗi khi lưu sản phẩm!');
    }
  }
};

// ========== Xoá sản phẩm ========== //
let deleteProductId = null;
function openDeleteProduct(id) {
  deleteProductId = id;
  document.getElementById('confirmModal').style.display = 'block';
}
document.getElementById('btnCancelDelete').onclick = closeConfirmModal;
document.getElementById('closeConfirmModal').onclick = closeConfirmModal;
function closeConfirmModal() {
  document.getElementById('confirmModal').style.display = 'none';
  deleteProductId = null;
}
document.getElementById('btnConfirmDelete').onclick = async function() {
  if (!deleteProductId) return;
  try {
    const res = await globalFetch(`${API_BASE}/`+deleteProductId, {method:'DELETE'});
    if (!res.ok) throw new Error();
    closeConfirmModal();
    loadProducts(currentPage);
  } catch {
    alert('Không thể xoá sản phẩm!');
  }
};

// ========== Ẩn/Hiện sản phẩm ========== //
async function toggleStatus(id) {
  try {
    const res = await globalFetch(`${API_BASE}/${id}/toggle-status`, {method:'PATCH'});
    if (!res.ok) throw new Error();
    loadProducts(currentPage);
  } catch {
    alert('Không thể cập nhật trạng thái!');
  }
}

// ========== GLOBAL FETCH WRAPPER ========== //
async function globalFetch(input, init) {
  const res = await fetch(input, init);
  if (res.status === 401) {
    handleAuthExpired();
    throw new Error('Unauthorized');
  }
  return res;
}

async function checkAuthOnPageLoad() {
  try {
    const res = await globalFetch('/api/auth/me', { credentials: 'include' });
    if (res.status === 401) {
      showToast('Vui lòng đăng nhập để tiếp tục', 'warning');
      showAuthModal('login');
    }
  } catch {}
}

// ========== Khởi tạo ========== //
window.onload = async function() {
  loadTeaTypes();
  loadProducts();
  checkAuthOnPageLoad();
};
window.onAdminLoginSuccess = function() {
  showToast('Đăng nhập thành công!', 'success');
  document.body.classList.remove('blurred');
  loadProducts(); // reload lại data, không reload trang
};

// Khi trang load, tự động gọi loadProducts và loadTeaTypes
window.addEventListener('DOMContentLoaded', function() {
  loadProducts();
  loadTeaTypes();
}); 