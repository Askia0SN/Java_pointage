CREATE DATABASE IF NOT EXISTS pointage_epf
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'pointage_user'@'localhost'
    IDENTIFIED BY 'pointage_pwd';

GRANT ALL PRIVILEGES ON pointage_epf.* TO 'pointage_user'@'localhost';

FLUSH PRIVILEGES;
