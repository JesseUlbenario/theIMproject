import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LedgerDao {
    public static List<LedgerRow> findAll(Integer periodId, Integer departmentId) throws SQLException {
        List<LedgerRow> list = new ArrayList<>();
        String sql = "SELECT l.ledger_id, l.department_id, d.department_name, l.payroll_period_id, l.total_gross, l.total_deductions, l.total_net, l.generation_date " +
            "FROM Ledger l JOIN Department d ON d.department_id = l.department_id WHERE 1=1";
        if (periodId != null) sql += " AND l.payroll_period_id=?";
        if (departmentId != null) sql += " AND l.department_id=?";
        sql += " ORDER BY l.ledger_id DESC";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            if (periodId != null) ps.setInt(i++, periodId);
            if (departmentId != null) ps.setInt(i++, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new LedgerRow(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4),
                        rs.getBigDecimal(5), rs.getBigDecimal(6), rs.getBigDecimal(7), rs.getTimestamp(8)));
            }
        }
        return list;
    }

    public static class LedgerRow {
        public final int ledgerId, departmentId, payrollPeriodId;
        public final String departmentName;
        public final java.math.BigDecimal totalGross, totalDeductions, totalNet;
        public final Timestamp generationDate;
        public LedgerRow(int id, int deptId, String deptName, int periodId, java.math.BigDecimal gross, java.math.BigDecimal ded, java.math.BigDecimal net, Timestamp gen) {
            ledgerId = id; departmentId = deptId; departmentName = deptName; payrollPeriodId = periodId;
            totalGross = gross; totalDeductions = ded; totalNet = net; generationDate = gen;
        }
    }
}
