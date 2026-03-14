import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompensationDao {
    public static List<CompensationRow> findByPeriod(Integer periodId) throws SQLException {
        List<CompensationRow> list = new ArrayList<>();
        String sql = "SELECT c.compensation_id, c.employee_id, e.full_name, c.payroll_period_id, c.basic_amount, c.overtime_amount, c.total_compensation, c.hr_status " +
            "FROM Compensation c JOIN Employee e ON e.employee_id = c.employee_id";
        if (periodId != null) sql += " WHERE c.payroll_period_id=?";
        sql += " ORDER BY c.compensation_id DESC";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (periodId != null) ps.setInt(1, periodId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new CompensationRow(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4),
                        rs.getBigDecimal(5), rs.getBigDecimal(6), rs.getBigDecimal(7), rs.getString(8)));
            }
        }
        return list;
    }

    public static List<CompensationRow> findByPeriodAndEmployee(Integer periodId, Integer employeeId) throws SQLException {
        List<CompensationRow> list = new ArrayList<>();
        String sql = "SELECT c.compensation_id, c.employee_id, e.full_name, c.payroll_period_id, c.basic_amount, c.overtime_amount, c.total_compensation, c.hr_status " +
            "FROM Compensation c JOIN Employee e ON e.employee_id = c.employee_id WHERE 1=1";
        if (periodId != null) sql += " AND c.payroll_period_id=?";
        if (employeeId != null) sql += " AND c.employee_id=?";
        sql += " ORDER BY c.compensation_id DESC";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            if (periodId != null) ps.setInt(i++, periodId);
            if (employeeId != null) ps.setInt(i, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new CompensationRow(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4),
                        rs.getBigDecimal(5), rs.getBigDecimal(6), rs.getBigDecimal(7), rs.getString(8)));
            }
        }
        return list;
    }

    public static void insert(int employeeId, int payrollPeriodId, Integer dtrSummaryId, Integer createdBy,
                              java.math.BigDecimal basicHours, java.math.BigDecimal basicAmount,
                              java.math.BigDecimal overtimeHours, java.math.BigDecimal overtimeAmount,
                              java.math.BigDecimal totalCompensation, String hrStatus) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO Compensation (employee_id, payroll_period_id, dtr_summary_id, created_by, basic_hours, basic_amount, overtime_hours, overtime_amount, total_compensation, hr_status) VALUES (?,?,?,?,?,?,?,?,?,?)")) {
            ps.setInt(1, employeeId);
            ps.setInt(2, payrollPeriodId);
            ps.setObject(3, dtrSummaryId);
            ps.setObject(4, createdBy);
            ps.setBigDecimal(5, basicHours);
            ps.setBigDecimal(6, basicAmount);
            ps.setBigDecimal(7, overtimeHours);
            ps.setBigDecimal(8, overtimeAmount);
            ps.setBigDecimal(9, totalCompensation);
            ps.setString(10, hrStatus);
            ps.executeUpdate();
        }
    }

    public static class CompensationRow {
        public final int compensationId, employeeId, payrollPeriodId;
        public final String fullName, hrStatus;
        public final java.math.BigDecimal basicAmount, overtimeAmount, totalCompensation;
        public CompensationRow(int id, int empId, String name, int periodId, java.math.BigDecimal basic, java.math.BigDecimal ot, java.math.BigDecimal total, String st) {
            compensationId = id; employeeId = empId; fullName = name; payrollPeriodId = periodId; basicAmount = basic; overtimeAmount = ot; totalCompensation = total; hrStatus = st;
        }
    }
}
