import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PayrollDao {
    public static List<PayrollRow> findByPeriod(int periodId) throws SQLException {
        List<PayrollRow> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT p.payroll_id, p.employee_id, e.full_name, p.payroll_period_id, p.gross_pay, p.total_deductions, p.net_pay, p.status " +
                 "FROM Payroll p JOIN Employee e ON e.employee_id = p.employee_id WHERE p.payroll_period_id=? ORDER BY e.full_name")) {
            ps.setInt(1, periodId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new PayrollRow(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4),
                        rs.getBigDecimal(5), rs.getBigDecimal(6), rs.getBigDecimal(7), rs.getString(8)));
            }
        }
        return list;
    }

    public static class PayrollRow {
        public final int payrollId, employeeId, payrollPeriodId;
        public final String fullName, status;
        public final java.math.BigDecimal grossPay, totalDeductions, netPay;
        public PayrollRow(int id, int empId, String name, int periodId, java.math.BigDecimal gross, java.math.BigDecimal ded, java.math.BigDecimal net, String st) {
            payrollId = id; employeeId = empId; fullName = name; payrollPeriodId = periodId; grossPay = gross; totalDeductions = ded; netPay = net; status = st;
        }
    }
}
