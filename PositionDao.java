import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PositionDao {
    public static List<Position> findAll() throws SQLException {
        List<Position> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT position_id, position FROM Position ORDER BY position");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(new Position(rs.getInt(1), rs.getString(2)));
        }
        return list;
    }

    public static void insert(String position) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO Position (position) VALUES (?)")) {
            ps.setString(1, position);
            ps.executeUpdate();
        }
    }

    public static class Position {
        public final int positionId;
        public final String position;
        public Position(int id, String name) { positionId = id; position = name; }
        @Override public String toString() { return position; }
    }
}
