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

    /** For company-style compensation summary: compensation with department, ordered by department then name. */
    public static List<CompensationExportRow> findByPeriodWithDepartment(Integer periodId) throws SQLException {
        List<CompensationExportRow> list = new ArrayList<>();
        String sql = "SELECT COALESCE(d.department_name, 'No Department'), c.employee_id, e.full_name, e.basic_salary, e.hourly_rate, " +
            "c.payroll_period_id, c.basic_hours, c.basic_amount, c.overtime_hours, c.overtime_amount, c.total_compensation, c.hr_status " +
            "FROM Compensation c JOIN Employee e ON e.employee_id = c.employee_id " +
            "LEFT JOIN EmployeeRole er ON er.employee_id = e.employee_id AND er.is_active = 1 " +
            "LEFT JOIN Department d ON d.department_id = er.department_id " +
            "WHERE c.payroll_period_id = ? " +
            "ORDER BY COALESCE(d.department_name, 'zzz'), e.full_name";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, periodId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new CompensationExportRow(rs.getString(1), rs.getInt(2), rs.getString(3),
                        rs.getBigDecimal(4), rs.getBigDecimal(5), rs.getInt(6),
                        rs.getBigDecimal(7), rs.getBigDecimal(8), rs.getBigDecimal(9), rs.getBigDecimal(10),
                        rs.getBigDecimal(11), rs.getString(12)));
            }
        }
        return list;
    }

    /** For 13th-month report: total compensation by employee and period (period has start_date for month). */
    public static List<CompensationDao.CompensationRow> findByPeriodsFor13thMonth(List<Integer> periodIds) throws SQLException {
        if (periodIds == null || periodIds.isEmpty()) return new ArrayList<>();
        List<CompensationRow> list = new ArrayList<>();
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < periodIds.size(); i++) placeholders.append(i == 0 ? "?" : ",?");
        String sql = "SELECT c.compensation_id, c.employee_id, e.full_name, c.payroll_period_id, c.basic_amount, c.overtime_amount, c.total_compensation, c.hr_status " +
            "FROM Compensation c JOIN Employee e ON e.employee_id = c.employee_id WHERE c.payroll_period_id IN (" + placeholders + ") ORDER BY e.full_name, c.payroll_period_id";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < periodIds.size(); i++) ps.setInt(i + 1, periodIds.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new CompensationRow(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4),
                        rs.getBigDecimal(5), rs.getBigDecimal(6), rs.getBigDecimal(7), rs.getString(8)));
            }
        }
        return list;
    }

    public static class CompensationRow {
        public final int compensationId, employeeId, payrollPeriodId;
        public final String fullName, hrStatus;
        public final java.math.BigDecimal basicAmount, overtimeAmount, totalCompensation;
        public CompensationRow(int id, int empId, String name, int periodId, java.math.BigDecimal basic, java.math.BigDecimal ot, java.math.BigDecimal total, String st) {
            compensationId = id; employeeId = empId; fullName = name; payrollPeriodId = periodId; basicAmount = basic; overtimeAmount = ot; totalCompensation = total; hrStatus = st;
        }
    }

    public static class CompensationExportRow {
        public final String departmentName;
        public final int employeeId;
        public final String fullName;
        public final java.math.BigDecimal basicSalary, hourlyRate;
        public final int payrollPeriodId;
        public final java.math.BigDecimal basicHours, basicAmount, overtimeHours, overtimeAmount, totalCompensation;
        public final String hrStatus;
        public CompensationExportRow(String dept, int empId, String name, java.math.BigDecimal salary, java.math.BigDecimal hourly,
            int periodId, java.math.BigDecimal bHrs, java.math.BigDecimal bAmt, java.math.BigDecimal otHrs, java.math.BigDecimal otAmt,
            java.math.BigDecimal total, String st) {
            departmentName = dept; employeeId = empId; fullName = name; basicSalary = salary; hourlyRate = hourly; payrollPeriodId = periodId;
            basicHours = bHrs; basicAmount = bAmt; overtimeHours = otHrs; overtimeAmount = otAmt; totalCompensation = total; hrStatus = st != null ? st : "";
        }
    }
}
