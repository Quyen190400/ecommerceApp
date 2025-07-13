# Beui Tea Shop - E-commerce Website

Welcome to Beui Tea Shop! This is a modern e-commerce web application for selling premium Vietnamese tea and teaware products.

## Features
- User registration, login, and authentication
- Browse and search for tea products by category
- Product detail pages with images and descriptions
- Shopping cart and order management
- Order history and order status tracking
- Admin dashboard for product and order management
- Responsive design for desktop and mobile
- Secure payment and order confirmation flow
- File upload for product images (admin)
- Dark mode support

## Technologies Used
- **Backend:** Java, Spring Boot, Spring Security, JPA/Hibernate
- **Frontend:** Thymeleaf, HTML5, CSS3, JavaScript (Vanilla)
- **Database:** SQL Server (or compatible SQL database)
- **Build Tool:** Gradle
- **Other:** Docker, RESTful APIs

## Getting Started

### Prerequisites
- Java 17 or higher
- Gradle
- SQL Server (or update `application.yml` for your DB)
- Docker (optional, for containerized deployment)

### Installation
1. **Clone the repository:**
   ```bash
   git clone <your-repo-url>
   cd ecommerceapp
   ```
2. **Configure the database:**
   - Update `src/main/resources/application.yml` with your DB credentials.
   - Run the SQL script in `src/main/resources/create_tables.sql` to initialize tables (if needed).
3. **Build and run the application:**
   ```bash
   ./gradlew bootRun
   ```
   Or use Docker Compose:
   ```bash
   docker-compose up --build
   ```
4. **Access the website:**
   - User site: [http://localhost:8080/](http://localhost:8080/)
   - Admin dashboard: [http://localhost:8080/admin/products](http://localhost:8080/admin/products)

## Docker Deployment

### Docker & Docker Compose Configuration

- **Dockerfile**: Defines how to build the image for the Spring Boot application (Java 17). Only requires the pre-built JAR file from Gradle.
- **docker-compose.yml**: Manages multiple containers:
  - `beui-sqlserver`: Runs SQL Server with a default password (can be changed in the file).
  - `sql-init`: Automatically runs the table creation script when SQL Server is ready.
  - `greentea-app`: Runs the Spring Boot application, connects to the internal SQL Server.
  - Mounts volumes to persist uploaded images and database data.

### How it works
1. **beui-sqlserver** starts first, exposes port 1433, and stores data in a Docker volume.
2. **sql-init** waits for SQL Server to be healthy, then runs the table creation script (`create_tables.sql`).
3. **greentea-app** only starts when the database is ready and the script has finished. The app automatically connects to the internal database via Docker network.
4. Uploaded images are saved to the `uploads/images` directory on the host.

### Step-by-step Deployment Guide

1. **Install Docker & Docker Compose** (if not already installed):
   - **For WSL users**: Install Docker Engine directly in WSL
   - **For Windows users**: [Download Docker Desktop](https://www.docker.com/products/docker-desktop/)
2. **Build the JAR file:**
   ```bash
   ./gradlew build
   ```
   (The JAR file will be in `build/libs/`)
3. **Run Docker Compose:**
   ```bash
   docker-compose up --build
   ```
   - The first run will automatically build the image and initialize the database.
   - To run in the background: `docker-compose up -d --build`
4. **Access the website:**
   - [http://localhost:8080/](http://localhost:8080/)
   - Admin login: [http://localhost:8080/admin/products](http://localhost:8080/admin/products)
5. **Stop the services:**
   ```bash
   docker-compose down
   ```
   - Database data and uploaded images are preserved via Docker volumes.

> **Note:**
> - You can change the SQL Server password in `docker-compose.yml` and update the corresponding environment variables.
> - To completely remove all data, delete the related Docker volumes.

### Default Admin Account
- Email: `admin@greentea.com`
- Password: `123456`

## Folder Structure
- `src/main/java/com/tma/ecommerceApp/` - Main Java source code
- `src/main/resources/templates/` - Thymeleaf HTML templates
- `src/main/resources/static/` - Static assets (CSS, JS, images)
- `uploads/images/` - Uploaded product images

## Authorization & Access Control

### Public Access (No Login Required)
- **Anyone can access:**
  - Home page (`/`) - Browse all products without login
  - Product detail pages - View product information and images
  - Product search and filtering
  - View product categories

### Roles (Login Required)
- **User (Customer):**
  - Can access: All public pages plus cart, order history (`/orders`), and checkout.
  - Cannot access admin management pages.
- **Admin:**
  - Can access: All user pages (same as user role).
  - Plus: Admin dashboard (`/admin/products`, `/admin/orders`), product management, order management, and other admin features.

> **Note:** The home page and product browsing are publicly accessible. Users only need to login when they want to use shopping cart, place orders, or access admin features.

### Authentication & JWT Storage
- The application uses **JWT (JSON Web Token)** for authentication.
- After login, the JWT is stored in a **secure HTTP-only cookie** in the browser.
- The cookie is sent automatically with each request to authenticate the user.
- **Token Expiry:** JWT tokens are valid for 1 hour by default. After expiration, users must log in again.
- The backend checks the JWT on every request to protected endpoints and determines the user's role and permissions.

## Contact
For questions or support, please contact:
- Email: support@beuiteashop.com

---
Enjoy premium Vietnamese tea with Beui Tea Shop! 