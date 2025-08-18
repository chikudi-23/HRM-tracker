CREATE DATABASE hrm_db1;

use hrm_db1;

SELECT * FROM users;

SELECT * FROM departments;

SELECT * FROM roles;

ALTER TABLE users MODIFY password VARCHAR(255) NULL;

DELETE FROM users WHERE id = '9';

ALTER TABLE users DROP COLUMN joining_date;

