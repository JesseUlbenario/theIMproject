import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LeaveDao {
    public static List<LeaveRow> findAll(String statusFilter) throws SQLException {
        List<LeaveRow> list = new ArrayList<>();
        String sql = "SELECT l.leave_id, l.employee_id, e.full_name, l.leave_type, l.start_date, l.end_date, l.total_days, l.with_pay, l.status FROM `Leave` l JOIN Employee e ON e.employee_id = l.employee_id";
        if (statusFilter != null && !statusFilter.isEmpty() && !"All".equals(statusFilter))
            sql += " WHERE l.status = ?";
        sql += " ORDER BY l.start_date DESC";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (statusFilter != null && !statusFilter.isEmpty() && !"All".equals(statusFilter))
                ps.setString(1, statusFilter);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new LeaveRow(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4),
                        rs.getDate(5), rs.getDate(6), rs.getBigDecimal(7), rs.getBoolean(8), rs.getString(9)));
            }
        }
        return list;
    }

    public static void insert(int employeeId, String leaveType, Date start, Date end, java.math.BigDecimal totalDays, boolean withPay, String status, String remarks) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO `Leave` (employee_id, leave_type, start_date, end_date, total_days, with_pay, status, remarks) VALUES (?,?,?,?,?,?,?,?)")) {
            ps.setInt(1, employeeId);
            ps.setString(2, leaveType);
            ps.setDate(3, start);
            ps.setDate(4, end);
            ps.setBigDecimal(5, totalDays);
            ps.setBoolean(6, withPay);
            ps.setString(7, status);
            ps.setString(8, remarks);
            ps.executeUpdate();
        }
    }

    public static class LeaveRow {
        public final int leaveId, employeeId;
        public final String fullName, leaveType, status;
        public final Date startDate, endDate;
        public final java.math.BigDecimal totalDays;
        public final boolean withPay;
        public LeaveRow(int id, int empId, String name, String type, Date start, Date end, java.math.BigDecimal days, boolean wp, String st) {
            leaveId = id; employeeId = empId; fullName = name; leaveType = type; startDate = start; endDate = end; totalDays = days; withPay = wp; status = st;
        }
    }
}
