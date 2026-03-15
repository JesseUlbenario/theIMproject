-- MySQL schema for Philippine Payroll (theIMproject)
-- Run this after creating the database: CREATE DATABASE payroll;

USE payroll;

-- Departments (e.g. Finance, Golf Maintenance)
CREATE TABLE IF NOT EXISTS Department (
  department_id   INT AUTO_INCREMENT PRIMARY KEY,
  department_code VARCHAR(50),
  department_name VARCHAR(255)
);

-- Employees
CREATE TABLE IF NOT EXISTS Employee (
  employee_id   INT AUTO_INCREMENT PRIMARY KEY,
  employee_code VARCHAR(50),
  full_name     VARCHAR(255),
  basic_salary  DECIMAL(18,2),
  daily_rate    DECIMAL(18,2),
  hourly_rate   DECIMAL(18,2),
  is_active     TINYINT(1) DEFAULT 1
);

-- Employee assignment to department (and role)
CREATE TABLE IF NOT EXISTS EmployeeRole (
  employee_role_id INT AUTO_INCREMENT PRIMARY KEY,
  employee_id      INT NOT NULL,
  date_effective   DATE,
  is_active        TINYINT(1) DEFAULT 1,
  department_id    INT,
  role_type        VARCHAR(100),
  FOREIGN KEY (employee_id)   REFERENCES Employee(employee_id),
  FOREIGN KEY (department_id) REFERENCES Department(department_id)
);

-- Position (job title) - backticks because Position is a reserved word in MySQL/MariaDB
CREATE TABLE IF NOT EXISTS `Position` (
  position_id INT AUTO_INCREMENT PRIMARY KEY,
  position    VARCHAR(255)
);

-- RegUser: links EmployeeRole to Position
CREATE TABLE IF NOT EXISTS RegUser (
  employee_role_id INT PRIMARY KEY,
  position_id      INT,
  FOREIGN KEY (employee_role_id) REFERENCES EmployeeRole(employee_role_id),
  FOREIGN KEY (position_id)      REFERENCES `Position`(position_id)
);

-- HR users (login)
CREATE TABLE IF NOT EXISTS HRUser (
  hr_user_id       INT AUTO_INCREMENT PRIMARY KEY,
  employee_role_id INT NOT NULL,
  hr_role          VARCHAR(100),
  username         VARCHAR(100),
  password_hash    VARCHAR(255),
  FOREIGN KEY (employee_role_id) REFERENCES EmployeeRole(employee_role_id)
);

-- Payroll periods (e.g. "Nov 24 to Dec 08, 2025")
CREATE TABLE IF NOT EXISTS PayrollPeriod (
  period_id   INT AUTO_INCREMENT PRIMARY KEY,
  period_name VARCHAR(255),
  start_date  DATE,
  end_date    DATE,
  pay_date    DATE,
  status      VARCHAR(50)
);

-- Daily Time Record
CREATE TABLE IF NOT EXISTS DTR (
  dtr_id          INT AUTO_INCREMENT PRIMARY KEY,
  employee_id     INT NOT NULL,
  date_val        DATE,
  time_in         TIME,
  time_out        TIME,
  regular_hours   DECIMAL(10,2),
  overtime_hours  DECIMAL(10,2),
  status          VARCHAR(50),
  FOREIGN KEY (employee_id) REFERENCES Employee(employee_id)
);

-- Compensation per period (basic + OT)
CREATE TABLE IF NOT EXISTS Compensation (
  compensation_id     INT AUTO_INCREMENT PRIMARY KEY,
  employee_id         INT NOT NULL,
  payroll_period_id  INT NOT NULL,
  dtr_summary_id     INT,
  created_by         INT,
  basic_hours        DECIMAL(10,2),
  basic_amount       DECIMAL(18,2),
  overtime_hours     DECIMAL(10,2),
  overtime_amount    DECIMAL(18,2),
  total_compensation DECIMAL(18,2),
  hr_status          VARCHAR(50),
  FOREIGN KEY (employee_id)        REFERENCES Employee(employee_id),
  FOREIGN KEY (payroll_period_id)  REFERENCES PayrollPeriod(period_id)
);

-- Deductions (SSS, PhilHealth, Pag-IBIG, loans, etc.)
CREATE TABLE IF NOT EXISTS Deduction (
  deduction_id      INT AUTO_INCREMENT PRIMARY KEY,
  employee_id      INT NOT NULL,
  payroll_period_id INT NOT NULL,
  deduction_type   VARCHAR(100),
  amount           DECIMAL(18,2),
  description      VARCHAR(500),
  status           VARCHAR(50),
  applied_by       INT,
  applied_date     DATETIME,
  FOREIGN KEY (employee_id)        REFERENCES Employee(employee_id),
  FOREIGN KEY (payroll_period_id)  REFERENCES PayrollPeriod(period_id)
);

-- Payroll (gross, deductions, net per employee per period)
CREATE TABLE IF NOT EXISTS Payroll (
  payroll_id       INT AUTO_INCREMENT PRIMARY KEY,
  employee_id      INT NOT NULL,
  payroll_period_id INT NOT NULL,
  gross_pay        DECIMAL(18,2),
  total_deductions DECIMAL(18,2),
  net_pay          DECIMAL(18,2),
  status           VARCHAR(50),
  FOREIGN KEY (employee_id)        REFERENCES Employee(employee_id),
  FOREIGN KEY (payroll_period_id)  REFERENCES PayrollPeriod(period_id)
);

-- Ledger (department-level totals per period)
CREATE TABLE IF NOT EXISTS Ledger (
  ledger_id        INT AUTO_INCREMENT PRIMARY KEY,
  department_id    INT NOT NULL,
  payroll_period_id INT NOT NULL,
  total_gross      DECIMAL(18,2),
  total_deductions DECIMAL(18,2),
  total_net        DECIMAL(18,2),
  generation_date  DATETIME,
  FOREIGN KEY (department_id)      REFERENCES Department(department_id),
  FOREIGN KEY (payroll_period_id)  REFERENCES PayrollPeriod(period_id)
);

-- Leave - backticks because Leave is a reserved word in MySQL/MariaDB
CREATE TABLE IF NOT EXISTS `Leave` (
  leave_id    INT AUTO_INCREMENT PRIMARY KEY,
  employee_id INT NOT NULL,
  leave_type  VARCHAR(100),
  start_date  DATE,
  end_date    DATE,
  total_days  DECIMAL(10,2),
  with_pay    TINYINT(1),
  status      VARCHAR(50),
  remarks     VARCHAR(500),
  FOREIGN KEY (employee_id) REFERENCES Employee(employee_id)
);

-- Optional: seed one department, one employee, one role, one HR user (admin)
INSERT INTO Department (department_code, department_name) VALUES ('HR', 'Human Resource & Development');
INSERT INTO Employee (employee_code, full_name, basic_salary, daily_rate, hourly_rate, is_active)
  VALUES ('ADMIN001', 'Admin User', 25000.00, 1250.00, 156.25, 1);
INSERT INTO EmployeeRole (employee_id, date_effective, is_active, department_id, role_type)
  VALUES (1, CURDATE(), 1, 1, 'Admin');
INSERT INTO HRUser (employee_role_id, hr_role, username, password_hash)
  VALUES (1, 'Admin', 'admin', 'admin');
