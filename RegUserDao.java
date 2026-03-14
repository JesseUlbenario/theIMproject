import java.sql.*;

/**
 * Stores employee position assignment per EmployeeRole via RegUser.
 */
public class RegUserDao {
    public static Integer getPositionIdForRole(int employeeRoleId) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT position_id FROM RegUser WHERE employee_role_id=? LIMIT 1")) {
            ps.setInt(1, employeeRoleId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? (Integer) rs.getObject(1) : null;
            }
        }
    }

    public static void upsertPositionForRole(int employeeRoleId, Integer positionId) throws SQLException {
        // Try update first
        try (Connection c = Database.getConnection();
             PreparedStatement upd = c.prepareStatement("UPDATE RegUser SET position_id=? WHERE employee_role_id=?")) {
            upd.setObject(1, positionId);
            upd.setInt(2, employeeRoleId);
            int updated = upd.executeUpdate();
            if (updated > 0) return;
        }
        // Insert
        try (Connection c = Database.getConnection();
             PreparedStatement ins = c.prepareStatement("INSERT INTO RegUser (employee_role_id, position_id) VALUES (?,?)")) {
            ins.setInt(1, employeeRoleId);
            ins.setObject(2, positionId);
            ins.executeUpdate();
        }
    }
}

