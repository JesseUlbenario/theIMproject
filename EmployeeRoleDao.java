import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeRoleDao {
    public static Integer getEmployeeIdByRoleId(int employeeRoleId) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT employee_id FROM EmployeeRole WHERE employee_role_id=?")) {
            ps.setInt(1, employeeRoleId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    /** Get active role for employee (department name, role type). */
    public static EmployeeRoleInfo getActiveRole(int employeeId) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT er.employee_role_id, er.department_id, d.department_name, er.role_type FROM EmployeeRole er " +
                 "LEFT JOIN Department d ON d.department_id = er.department_id WHERE er.employee_id=? AND er.is_active=1 ORDER BY er.date_effective DESC LIMIT 1")) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return new EmployeeRoleInfo(rs.getInt(1), rs.getObject(2) != null ? rs.getInt(2) : null, rs.getString(3), rs.getString(4));
            }
        }
        return null;
    }

    /** Ensure there is an active role row and return its id. */
    public static int ensureActiveRole(int employeeId, int departmentId, String roleType) throws SQLException {
        EmployeeRoleInfo existing = getActiveRole(employeeId);
        if (existing != null) return existing.employeeRoleId;
        insert(employeeId, departmentId, roleType);
        EmployeeRoleInfo created = getActiveRole(employeeId);
        if (created == null) throw new SQLException("Could not create employee role");
        return created.employeeRoleId;
    }

    public static void insert(int employeeId, int departmentId, String roleType) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO EmployeeRole (employee_id, date_effective, is_active, department_id, role_type) VALUES (?,CURRENT_DATE,1,?,?)")) {
            ps.setInt(1, employeeId);
            ps.setInt(2, departmentId);
            ps.setString(3, roleType);
            ps.executeUpdate();
        }
    }

    public static class EmployeeRoleInfo {
        public final int employeeRoleId;
        public final Integer departmentId;
        public final String departmentName, roleType;
        public EmployeeRoleInfo(int roleId, Integer deptId, String deptName, String type) {
            employeeRoleId = roleId; departmentId = deptId; departmentName = deptName; roleType = type;
        }
    }
}
