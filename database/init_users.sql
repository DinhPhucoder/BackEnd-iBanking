-- Create service users and grant schema privileges
CREATE USER IF NOT EXISTS 'account_user'@'%' IDENTIFIED BY 'account123';
GRANT ALL ON account_db.* TO 'account_user'@'%';

CREATE USER IF NOT EXISTS 'user_user'@'%' IDENTIFIED BY 'user123';
GRANT ALL ON user_db.* TO 'user_user'@'%';

CREATE USER IF NOT EXISTS 'tuition_user'@'%' IDENTIFIED BY 'tuition123';
GRANT ALL ON tuition_db.* TO 'tuition_user'@'%';

FLUSH PRIVILEGES;


