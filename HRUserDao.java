import java.sql.*;

/**
 * Login: match username and password_hash (plain text for demo; use hashing in production).
 */
public class HRUserDao {
    public static HRUser findByUsernameAndPassword(String username, String password) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT hr_user_id, employee_role_id, hr_role, username FROM HRUser WHERE username=? AND password_hash=?")) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return new HRUser(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4));
            }
        }
        return null;
    }

    public static void createUser(int employeeRoleId, String hrRole, String username, String password) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO HRUser (employee_role_id, hr_role, username, password_hash) VALUES (?,?,?,?)")) {
            ps.setInt(1, employeeRoleId);
            ps.setString(2, hrRole);
            ps.setString(3, username);
            ps.setString(4, password);
            ps.executeUpdate();
        }
    }

    public static class HRUser {
        public final int hrUserId;
        public final int employeeRoleId;
        public final String hrRole;
        public final String username;
        public HRUser(int id, int roleId, String role, String user) {
            hrUserId = id; employeeRoleId = roleId; hrRole = role; username = user;
        }
    }
}
