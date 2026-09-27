-- Ghost Net Fishing: Datenbank und Benutzer in MySQL 8 anlegen
-- Ausführen z. B. mit: mysql -u root -p < setup/01_mysql_datenbank.sql

CREATE DATABASE IF NOT EXISTS ghostnet
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'ghostnet'@'localhost' IDENTIFIED BY 'ghostnet';
GRANT ALL PRIVILEGES ON ghostnet.* TO 'ghostnet'@'localhost';
FLUSH PRIVILEGES;
