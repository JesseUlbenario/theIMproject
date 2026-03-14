import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDao {
    public static List<Department> findAll() throws SQLException {
        List<Department> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT department_id, department_code, department_name FROM Department ORDER BY department_code");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(new Department(rs.getInt(1), rs.getString(2), rs.getString(3)));
        }
        return list;
    }

    public static void insert(String code, String name) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO Department (department_code, department_name) VALUES (?, ?)")) {
            ps.setString(1, code);
            ps.setString(2, name);
            ps.executeUpdate();
        }
    }

    public static void update(int id, String code, String name) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE Department SET department_code=?, department_name=? WHERE department_id=?")) {
            ps.setString(1, code);
            ps.setString(2, name);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    public static class Department {
        public final int departmentId;
        public final String departmentCode;
        public final String departmentName;
        public Department(int id, String code, String name) {
            departmentId = id; departmentCode = code; departmentName = name;
        }
        @Override public String toString() { return departmentName != null ? departmentName : departmentCode; }
    }
}
