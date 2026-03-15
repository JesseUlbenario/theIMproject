import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PayrollDao {
    public static List<PayrollRow> findByPeriod(int periodId) throws SQLException {
        List<PayrollRow> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT p.payroll_id, p.employee_id, e.full_name, COALESCE(e.employee_code, ''), p.payroll_period_id, p.gross_pay, p.total_deductions, p.net_pay, p.status " +
                 "FROM Payroll p JOIN Employee e ON e.employee_id = p.employee_id WHERE p.payroll_period_id=? ORDER BY e.full_name")) {
            ps.setInt(1, periodId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new PayrollRow(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getInt(5),
                        rs.getBigDecimal(6), rs.getBigDecimal(7), rs.getBigDecimal(8), rs.getString(9)));
            }
        }
        return list;
    }

    /** For signature ledger and reports: payroll rows with department, ordered by department then name. */
    public static List<PayrollRowWithDept> findByPeriodWithDepartment(int periodId) throws SQLException {
        List<PayrollRowWithDept> list = new ArrayList<>();
        String sql = "SELECT p.payroll_id, p.employee_id, e.full_name, COALESCE(e.employee_code, ''), p.payroll_period_id, " +
            "p.gross_pay, p.total_deductions, p.net_pay, p.status, COALESCE(d.department_name, 'No Department') " +
            "FROM Payroll p JOIN Employee e ON e.employee_id = p.employee_id " +
            "LEFT JOIN EmployeeRole er ON er.employee_id = e.employee_id AND er.is_active = 1 " +
            "LEFT JOIN Department d ON d.department_id = er.department_id " +
            "WHERE p.payroll_period_id = ? ORDER BY COALESCE(d.department_name, 'zzz'), e.full_name";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, periodId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new PayrollRowWithDept(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getInt(5),
                        rs.getBigDecimal(6), rs.getBigDecimal(7), rs.getBigDecimal(8), rs.getString(9), rs.getString(10)));
            }
        }
        return list;
    }

    public static class PayrollRow {
        public final int payrollId, employeeId, payrollPeriodId;
        public final String fullName, employeeCode, status;
        public final java.math.BigDecimal grossPay, totalDeductions, netPay;
        public PayrollRow(int id, int empId, String name, String code, int periodId, java.math.BigDecimal gross, java.math.BigDecimal ded, java.math.BigDecimal net, String st) {
            payrollId = id; employeeId = empId; fullName = name; employeeCode = code != null ? code : ""; payrollPeriodId = periodId; grossPay = gross; totalDeductions = ded; netPay = net; status = st;
        }
    }

    public static class PayrollRowWithDept {
        public final int payrollId, employeeId, payrollPeriodId;
        public final String fullName, employeeCode, status, departmentName;
        public final java.math.BigDecimal grossPay, totalDeductions, netPay;
        public PayrollRowWithDept(int id, int empId, String name, String code, int periodId, java.math.BigDecimal gross, java.math.BigDecimal ded, java.math.BigDecimal net, String st, String dept) {
            payrollId = id; employeeId = empId; fullName = name; employeeCode = code != null ? code : ""; payrollPeriodId = periodId; grossPay = gross; totalDeductions = ded; netPay = net; status = st; departmentName = dept;
        }
    }
}
