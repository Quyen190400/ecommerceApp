# Tính năng Profile - Trà Đạo Quản Trị

## Tổng quan
Tính năng Profile cho phép người dùng xem và cập nhật thông tin cá nhân của mình theo phong cách thiết kế "Trà Đạo Quản Trị".

## Tính năng chính

### 1. Xem thông tin profile
- Hiển thị thông tin cơ bản: Họ tên, Email, SĐT, ID, Ngày đăng ký, Vai trò, Trạng thái
- Giao diện chia làm 2 phần: Thông tin cơ bản (35%) và Chi tiết (65%)
- Avatar tròn với border màu xanh lá
- Badge hiển thị vai trò và trạng thái

### 2. Chỉnh sửa thông tin
- Cập nhật họ tên và số điện thoại
- Validation form với thông báo lỗi
- Auto-format số điện thoại Việt Nam

### 3. Đổi mật khẩu
- Đổi mật khẩu với xác nhận
- Validation mật khẩu mới (tối thiểu 6 ký tự)
- Kiểm tra mật khẩu hiện tại

## Cấu trúc Backend

### Controller
- `ProfileController`: Xử lý các request liên quan đến profile
  - `GET /profile`: Hiển thị trang profile
  - `POST /profile/update`: Cập nhật thông tin
  - `POST /profile/change-password`: Đổi mật khẩu

### Service
- `UserService`: Interface định nghĩa các method
- `UserServiceImpl`: Implementation các method
  - `getUserInfoByUsername()`: Lấy thông tin user
  - `updateUserProfile()`: Cập nhật profile
  - `changePassword()`: Đổi mật khẩu

### DTO
- `UserInfo`: Chứa thông tin user cho profile
  - id, fullName, email, phone, role, active, createdAt

### Entity
- `AppUser`: Entity chính, đã thêm field `phone`

## Cấu trúc Frontend

### Template
- `profile.html`: Template chính với Thymeleaf
- Responsive design cho mobile và desktop
- Dark mode support

### CSS
- `profile.css`: Styles riêng cho trang profile
- Sử dụng CSS variables cho theme colors
- Animation và transitions mượt mà

### JavaScript
- `profile.js`: Xử lý tương tác người dùng
- Form validation
- Phone number formatting
- Keyboard shortcuts (Ctrl+E, Ctrl+P, Escape)

## Cách sử dụng

### 1. Truy cập profile
- Đăng nhập vào hệ thống
- Click vào dropdown user menu
- Chọn "Hồ sơ của tôi"

### 2. Chỉnh sửa thông tin
- Click nút "Chỉnh sửa hồ sơ"
- Điền thông tin mới
- Click "Cập nhật"

### 3. Đổi mật khẩu
- Click nút "Đổi mật khẩu"
- Nhập mật khẩu hiện tại và mới
- Click "Đổi mật khẩu"

### 4. Keyboard shortcuts
- `Ctrl+E`: Toggle form chỉnh sửa
- `Ctrl+P`: Toggle form đổi mật khẩu
- `Escape`: Đóng form

## Database Migration

### V2__add_phone_to_app_user.sql
```sql
ALTER TABLE app_user ADD COLUMN phone VARCHAR(20);
```

## Testing

### ProfileControllerTest
- Test các method trong ProfileController
- Mock UserService và SecurityContext
- Test success và error cases

## Theme Colors

### Light Mode
- Primary: #7bc47f (xanh lá)
- Secondary: #a67c52 (nâu đất)
- Background: #f8faf7 (trắng ngà)
- Text: #222 (đen nhẹ)

### Dark Mode
- Primary: #4e7c59 (xanh đậm)
- Secondary: #8d6742 (nâu đậm)
- Background: #2e2b24 (nâu tối)
- Text: #f8faf7 (trắng ngà)

## Responsive Design

### Desktop (>900px)
- Layout 2 cột: 35% + 65%
- Full feature set

### Tablet (600px-900px)
- Layout 1 cột
- Adjusted spacing

### Mobile (<600px)
- Compact layout
- Smaller avatar
- Centered text alignment

## Security

### Authentication
- Yêu cầu đăng nhập để truy cập
- Sử dụng Spring Security

### Password Change
- Validate mật khẩu hiện tại
- BCrypt encryption cho mật khẩu mới
- Session management

### Input Validation
- Server-side validation
- Client-side validation với JavaScript
- SQL injection prevention

## Performance

### Optimization
- Lazy loading cho user data
- Efficient database queries
- Minified CSS/JS files
- Image optimization

### Caching
- User session caching
- Static resource caching

## Future Enhancements

### Planned Features
- Upload avatar image
- Social media links
- Notification preferences
- Activity history
- Export profile data

### Technical Improvements
- API endpoints cho mobile app
- Real-time updates
- Advanced validation rules
- Multi-language support 