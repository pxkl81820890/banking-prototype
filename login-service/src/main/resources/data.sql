-- Insert test users into USERS table
-- Password for all users: password123
-- BCrypt hash generated with BCryptPasswordEncoder (strength 10)

INSERT INTO USERS (USER_ID, PASSWORD_HASH, BANK_CODE, BRANCH_CODE, CURRENCY) VALUES
('testuser', '$2a$10$SEsCf0Ai/swlgONnmnS.7eBiLcg68dSHQj5aL2qelvQ4MO8zjI3yG', '101', '1119', 'SGD'),
('1119test1', '$2a$10$SEsCf0Ai/swlgONnmnS.7eBiLcg68dSHQj5aL2qelvQ4MO8zjI3yG', '101', '1119', 'SGD'),
('1119test2', '$2a$10$SEsCf0Ai/swlgONnmnS.7eBiLcg68dSHQj5aL2qelvQ4MO8zjI3yG', '101', '1119', 'USD'),
('1119test3', '$2a$10$SEsCf0Ai/swlgONnmnS.7eBiLcg68dSHQj5aL2qelvQ4MO8zjI3yG', '101', '1119', 'EUR'),
('adminuser', '$2a$10$SEsCf0Ai/swlgONnmnS.7eBiLcg68dSHQj5aL2qelvQ4MO8zjI3yG', '102', '2001', 'SGD'),
('demouser', '$2a$10$SEsCf0Ai/swlgONnmnS.7eBiLcg68dSHQj5aL2qelvQ4MO8zjI3yG', '103', '3001', 'USD');
