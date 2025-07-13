IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'greenteashop')
BEGIN
    CREATE DATABASE greenteashop;
END
GO 