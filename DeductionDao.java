import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DeductionDao {
    public static final String[] CONTRIBUTION_TYPES = { "SSS", "PhilHealth", "PagIBIG" };
    public static final String[] DEDUCTION_TYPES = { "Loan", "Cash Advance", "Other" };

    /** Rows where type is SSS, PhilHealth, or PagIBIG. */
    public static List<DeductionRow> findContributionsByPeriod(Integer periodId) throws SQLException {
        return findByTypes(periodId, CONTRIBUTION_TYPES);
    }

    /** Rows where type is Loan, Cash Advance, or Other. */
    public static List<DeductionRow> findDeductionsByPeriod(Integer periodId) throws SQLException {
        return findByTypes(periodId, DEDUCTION_TYPES);
    }

    private static List<DeductionRow> findByTypes(Integer periodId, String[] types) throws SQLException {
        List<DeductionRow> list = new ArrayList<>();
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < types.length; i++) placeholders.append(i == 0 ? "?" : ",?");
        String sql = "SELECT d.deduction_id, d.employee_id, e.full_name, d.payroll_period_id, d.deduction_type, d.amount, d.description, d.status " +
            "FROM Deduction d JOIN Employee e ON e.employee_id = d.employee_id WHERE d.deduction_type IN (" + placeholders + ")";
        if (periodId != null) sql += " AND d.payroll_period_id=?";
        sql += " ORDER BY d.payroll_period_id, e.full_name, d.deduction_type";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            for (String t : types) ps.setString(i++, t);
            if (periodId != null) ps.setInt(i, periodId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new DeductionRow(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4),
                        rs.getString(5), rs.getBigDecimal(6), rs.getString(7), rs.getString(8)));
            }
        }
        return list;
    }

    /** Summary totals by period and type for contributions (SSS, PhilHealth, PagIBIG). Keys: periodName -> (type -> total). */
    public static Map<String, Map<String, java.math.BigDecimal>> getContributionSummaryForExport(Integer periodId) throws SQLException {
        return getSummaryForExport(periodId, CONTRIBUTION_TYPES);
    }

    /** Summary totals by period and type for deductions (Loan, Cash Advance, Other). */
    public static Map<String, Map<String, java.math.BigDecimal>> getDeductionSummaryForExport(Integer periodId) throws SQLException {
        return getSummaryForExport(periodId, DEDUCTION_TYPES);
    }

    private static Map<String, Map<String, java.math.BigDecimal>> getSummaryForExport(Integer periodId, String[] types) throws SQLException {
        String sql = "SELECT p.period_name, d.deduction_type, SUM(d.amount) FROM Deduction d " +
            "JOIN PayrollPeriod p ON p.period_id = d.payroll_period_id WHERE d.deduction_type IN (?,?,?)";
        if (periodId != null) sql += " AND d.payroll_period_id=?";
        sql += " GROUP BY p.period_id, p.period_name, d.deduction_type ORDER BY p.period_name, d.deduction_type";
        Map<String, Map<String, java.math.BigDecimal>> byPeriod = new LinkedHashMap<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, types[0]);
            ps.setString(2, types[1]);
            ps.setString(3, types[2]);
            if (periodId != null) ps.setInt(4, periodId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String periodName = rs.getString(1);
                    String type = rs.getString(2);
                    java.math.BigDecimal total = rs.getBigDecimal(3);
                    byPeriod.computeIfAbsent(periodName, k -> new LinkedHashMap<>()).put(type, total != null ? total : java.math.BigDecimal.ZERO);
                }
            }
        }
        return byPeriod;
    }

    /** Generic summary by type (for export) for a specific employee (or all if employeeId null). */
    public static Map<String, java.math.BigDecimal> getSummaryByType(Integer periodId, Integer employeeId) throws SQLException {
        String sql = "SELECT d.deduction_type, COALESCE(SUM(d.amount),0) FROM Deduction d WHERE 1=1";
        if (periodId != null) sql += " AND d.payroll_period_id=?";
        if (employeeId != null) sql += " AND d.employee_id=?";
        sql += " GROUP BY d.deduction_type ORDER BY d.deduction_type";
        Map<String, java.math.BigDecimal> out = new LinkedHashMap<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            if (periodId != null) ps.setInt(i++, periodId);
            if (employeeId != null) ps.setInt(i, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.put(rs.getString(1), rs.getBigDecimal(2));
            }
        }
        return out;
    }

    public static List<DeductionRow> findByPeriodAndEmployee(Integer periodId, Integer employeeId) throws SQLException {
        List<DeductionRow> list = new ArrayList<>();
        String sql = "SELECT d.deduction_id, d.employee_id, e.full_name, d.payroll_period_id, d.deduction_type, d.amount, d.description, d.status " +
            "FROM Deduction d JOIN Employee e ON e.employee_id = d.employee_id WHERE 1=1";
        if (periodId != null) sql += " AND d.payroll_period_id=?";
        if (employeeId != null) sql += " AND d.employee_id=?";
        sql += " ORDER BY d.deduction_id DESC";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            if (periodId != null) ps.setInt(i++, periodId);
            if (employeeId != null) ps.setInt(i++, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new DeductionRow(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4),
                        rs.getString(5), rs.getBigDecimal(6), rs.getString(7), rs.getString(8)));
            }
        }
        return list;
    }

    public static java.math.BigDecimal totalByPeriod(int periodId) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COALESCE(SUM(amount),0) FROM Deduction WHERE payroll_period_id=?")) {
            ps.setInt(1, periodId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : java.math.BigDecimal.ZERO;
            }
        }
    }

    public static void insert(int employeeId, int payrollPeriodId, String deductionType, java.math.BigDecimal amount, String description, String status, Integer appliedBy) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO Deduction (employee_id, payroll_period_id, deduction_type, amount, description, status, applied_by, applied_date) VALUES (?,?,?,?,?,?,?,CURRENT_TIMESTAMP)")) {
            ps.setInt(1, employeeId);
            ps.setInt(2, payrollPeriodId);
            ps.setString(3, deductionType);
            ps.setBigDecimal(4, amount);
            ps.setString(5, description);
            ps.setString(6, status);
            ps.setObject(7, appliedBy);
            ps.executeUpdate();
        }
    }

    /** For company-style deduction summary: all deductions for period with department, grouped by dept/employee/type. */
    public static List<DeductionExportRow> getDeductionDetailForExportByDepartment(Integer periodId) throws SQLException {
        List<DeductionExportRow> list = new ArrayList<>();
        String sql = "SELECT COALESCE(d.department_name, 'No Department'), e.employee_id, e.full_name, ded.deduction_type, COALESCE(SUM(ded.amount), 0) " +
            "FROM Deduction ded JOIN Employee e ON e.employee_id = ded.employee_id " +
            "LEFT JOIN EmployeeRole er ON er.employee_id = e.employee_id AND er.is_active = 1 " +
            "LEFT JOIN Department d ON d.department_id = er.department_id " +
            "WHERE ded.payroll_period_id = ? " +
            "GROUP BY d.department_name, e.employee_id, e.full_name, ded.deduction_type " +
            "ORDER BY COALESCE(d.department_name, 'zzz'), e.full_name, ded.deduction_type";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, periodId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new DeductionExportRow(rs.getString(1), rs.getInt(2), rs.getString(3),
                        rs.getString(4), rs.getBigDecimal(5) != null ? rs.getBigDecimal(5) : java.math.BigDecimal.ZERO));
            }
        }
        return list;
    }

    public static class DeductionExportRow {
        public final String departmentName;
        public final int employeeId;
        public final String employeeName;
        public final String deductionType;
        public final java.math.BigDecimal amount;
        public DeductionExportRow(String dept, int empId, String name, String type, java.math.BigDecimal amt) {
            departmentName = dept; employeeId = empId; employeeName = name; deductionType = type; amount = amt;
        }
    }

    public static class DeductionRow {
        public final int deductionId, employeeId, payrollPeriodId;
        public final String fullName, deductionType, description, status;
        public final java.math.BigDecimal amount;
        public DeductionRow(int id, int empId, String name, int periodId, String type, java.math.BigDecimal amt, String desc, String st) {
            deductionId = id; employeeId = empId; fullName = name; payrollPeriodId = periodId; deductionType = type; amount = amt; description = desc; status = st;
        }
    }
}
