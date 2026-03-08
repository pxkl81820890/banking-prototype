-- Insert test data into MASTER table
INSERT INTO MASTER (ID, FEATURE_FLAG, IS_ACL_ENABLED, ACL_ID) VALUES
(1, 'isArchiveEnquiryEnabled', true, 1000),
(2, 'isReportsEnabled', true, 1001);

-- Insert test data into ACL_CONFIG table
INSERT INTO ACL_CONFIG (USER_ID, ACL_ID) VALUES
('testuser', 1000),
('testuser', 1001),
('1119test1', 1000),
('1119test2', 1001),
('1119test3', 1000),
('1119test3', 1001);
