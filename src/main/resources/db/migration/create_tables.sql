-- Drop existing tables if they exist (in reverse order due to foreign keys)
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'order_item')
    DROP TABLE order_item;

IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'customer_order')
    DROP TABLE customer_order;

IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'product')
    DROP TABLE product;

IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'category')
    DROP TABLE category;

IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'app_user')
    DROP TABLE app_user;

-- Create tables in correct order (no foreign key dependencies first)

-- 1. Create app_user table first (no dependencies)
CREATE TABLE app_user (
    id INT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(MAX),
    password NVARCHAR(MAX),
    email NVARCHAR(MAX),
    role NVARCHAR(MAX),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

-- 2. Create category table (no dependencies)
CREATE TABLE category (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(MAX) NOT NULL UNIQUE,
    description NVARCHAR(MAX),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

-- 3. Create product table (depends on category)
CREATE TABLE product (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(MAX) NOT NULL,
    description NVARCHAR(MAX),
    price DECIMAL(10,2) NOT NULL,
    image_url NVARCHAR(MAX),
    origin NVARCHAR(MAX),
    tea_type NVARCHAR(MAX),
    taste_note NVARCHAR(MAX),
    health_benefit NVARCHAR(MAX),
    usage_guide NVARCHAR(MAX),
    stock_quantity INT DEFAULT 0,
    sold_count INT DEFAULT 0,
    status BIT NOT NULL DEFAULT 1,
    category_id INT,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (category_id) REFERENCES category(id)
);

-- 4. Create customer_order table (depends on app_user)
CREATE TABLE customer_order (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    status NVARCHAR(MAX) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES app_user(id)
);

-- 5. Create order_item table (depends on customer_order and product)
CREATE TABLE order_item (
    id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (order_id) REFERENCES customer_order(id),
    FOREIGN KEY (product_id) REFERENCES product(id)
);

PRINT 'All tables created successfully!'; 