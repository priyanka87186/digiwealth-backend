-- ============================================================================
-- DigiWealth AI — Demo data seed (direct SQL, no backend required to be running)
--
-- Creates the same demo user/data as seed_demo_data.py, but writes straight
-- into MySQL. Run this against the digiwealth_db database.
--
-- Login after running: aditi.demo@digiwealth.ai / Demo@1234
--
-- NOTE on the password hash: Spring Security's BCryptPasswordEncoder needs a
-- real BCrypt hash, not plaintext. The hash below was generated for the
-- password "Demo@1234" at cost factor 10 (Spring's default strength). If
-- login fails after importing, regenerate a hash yourself, e.g. in a Java/
-- Kotlin scratch file: new BCryptPasswordEncoder().encode("Demo@1234")
-- ============================================================================

USE digiwealth_db;

-- Clean up any previous run of this script (safe to re-run)
SET @old_user_id = (SELECT id FROM users WHERE email = 'aditi.demo@digiwealth.ai');
DELETE FROM ai_chat WHERE user_id = @old_user_id;
DELETE FROM recommendations WHERE user_id = @old_user_id;
DELETE FROM goals WHERE user_id = @old_user_id;
DELETE FROM investments WHERE user_id = @old_user_id;
DELETE FROM transactions WHERE account_id IN (SELECT id FROM accounts WHERE user_id = @old_user_id);
DELETE FROM accounts WHERE user_id = @old_user_id;
DELETE FROM users WHERE id = @old_user_id;

-- ---------------------------------------------------------------------------
-- 1. User  (password = "Demo@1234", BCrypt cost 10)
-- ---------------------------------------------------------------------------
INSERT INTO users (name, email, password, phone, role, created_at)
VALUES (
  'Aditi Sharma',
  'aditi.demo@digiwealth.ai',
  '$2b$10$DQ6LfNFJZVd5Ov4OE5mSy.rp/34SAqBl1qTChuFUNs6PhA2q5jj76',
  '9876543210',
  'CUSTOMER',
  NOW()
);
SET @user_id = LAST_INSERT_ID();

-- ---------------------------------------------------------------------------
-- 2. Account
-- ---------------------------------------------------------------------------
INSERT INTO accounts (user_id, account_number, balance)
VALUES (@user_id, CONCAT('DW', UPPER(SUBSTRING(MD5(RAND()), 1, 12))), 124500.00);
SET @account_id = LAST_INSERT_ID();

-- ---------------------------------------------------------------------------
-- 3. Transactions (18 rows, spanning the last 28 days, every category)
-- ---------------------------------------------------------------------------
INSERT INTO transactions (account_id, amount, category, transaction_type, date, description)
VALUES
  (@account_id, 85000.00, 'SALARY',        'CREDIT', DATE_SUB(NOW(), INTERVAL 28 DAY), 'Monthly Salary'),
  (@account_id, 25000.00, 'INVESTMENTS',   'DEBIT',  DATE_SUB(NOW(), INTERVAL 26 DAY), 'SIP - Mutual Fund'),
  (@account_id,  1850.00, 'BILLS',         'DEBIT',  DATE_SUB(NOW(), INTERVAL 25 DAY), 'Electricity Bill'),
  (@account_id,   899.00, 'BILLS',         'DEBIT',  DATE_SUB(NOW(), INTERVAL 24 DAY), 'Internet Bill'),
  (@account_id,  3200.00, 'FOOD',          'DEBIT',  DATE_SUB(NOW(), INTERVAL 22 DAY), 'Grocery Shopping - BigBasket'),
  (@account_id,  2499.00, 'SHOPPING',      'DEBIT',  DATE_SUB(NOW(), INTERVAL 20 DAY), 'Amazon - Headphones'),
  (@account_id,  1200.00, 'TRAVEL',        'DEBIT',  DATE_SUB(NOW(), INTERVAL 18 DAY), 'Uber rides'),
  (@account_id,  4500.00, 'ENTERTAINMENT', 'DEBIT',  DATE_SUB(NOW(), INTERVAL 16 DAY), 'Movie night + dinner'),
  (@account_id, 12000.00, 'LOAN_EMI',      'DEBIT',  DATE_SUB(NOW(), INTERVAL 15 DAY), 'Home Loan EMI'),
  (@account_id,  1500.00, 'HEALTHCARE',    'DEBIT',  DATE_SUB(NOW(), INTERVAL 14 DAY), 'Pharmacy - Apollo'),
  (@account_id,  6000.00, 'EDUCATION',     'DEBIT',  DATE_SUB(NOW(), INTERVAL 12 DAY), 'Online course subscription'),
  (@account_id,  2100.00, 'FOOD',          'DEBIT',  DATE_SUB(NOW(), INTERVAL 10 DAY), 'Swiggy orders (week)'),
  (@account_id,  1800.00, 'SHOPPING',      'DEBIT',  DATE_SUB(NOW(), INTERVAL 8 DAY),  'Myntra - Clothing'),
  (@account_id,   950.00, 'TRAVEL',        'DEBIT',  DATE_SUB(NOW(), INTERVAL 6 DAY),  'Metro card recharge'),
  (@account_id,  3000.00, 'ENTERTAINMENT', 'DEBIT',  DATE_SUB(NOW(), INTERVAL 5 DAY),  'Concert tickets'),
  (@account_id, 15000.00, 'OTHERS',        'CREDIT', DATE_SUB(NOW(), INTERVAL 3 DAY),  'Freelance project payment'),
  (@account_id,  2800.00, 'FOOD',          'DEBIT',  DATE_SUB(NOW(), INTERVAL 2 DAY),  'Dining out - weekend'),
  (@account_id,   500.00, 'BILLS',         'DEBIT',  DATE_SUB(NOW(), INTERVAL 1 DAY),  'Mobile recharge');

-- ---------------------------------------------------------------------------
-- 4. Investments (5 rows, mix of gains and losses)
-- ---------------------------------------------------------------------------
INSERT INTO investments (user_id, investment_type, invested_amount, current_value)
VALUES
  (@user_id, 'MUTUAL_FUNDS',   240000.00, 268000.00),
  (@user_id, 'STOCKS',         150000.00, 142000.00),
  (@user_id, 'FIXED_DEPOSITS', 200000.00, 214000.00),
  (@user_id, 'GOLD',            50000.00,  56000.00),
  (@user_id, 'NPS',              90000.00,  97000.00);

-- ---------------------------------------------------------------------------
-- 5. Goals (4 rows, at different progress stages)
-- ---------------------------------------------------------------------------
INSERT INTO goals (user_id, goal_name, target_amount, target_date, current_amount, monthly_contribution)
VALUES
  (@user_id, 'Emergency Fund',   300000.00,  DATE_ADD(CURDATE(), INTERVAL 365 DAY),  210000.00, 15000.00),
  (@user_id, 'Buy a Car',        800000.00,  DATE_ADD(CURDATE(), INTERVAL 540 DAY),  180000.00, 20000.00),
  (@user_id, 'Goa Vacation',     120000.00,  DATE_ADD(CURDATE(), INTERVAL 90 DAY),    95000.00, 10000.00),
  (@user_id, 'Child Education', 2000000.00,  DATE_ADD(CURDATE(), INTERVAL 2555 DAY), 350000.00, 25000.00);

-- ---------------------------------------------------------------------------
-- Done
-- ---------------------------------------------------------------------------
SELECT 'Seed complete.' AS status, @user_id AS user_id, @account_id AS account_id;
