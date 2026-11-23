package gym.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Small JDBC helpers shared by every DAO so each DAO only contains its SQL.
 * Every call opens a connection, runs one PreparedStatement and closes it.
 */
final class Sql {

    /** Turns one result row into an object. */
    interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private Sql() {
    }

    static <T> List<T> query(String sql, List<Object> params, RowMapper<T> mapper) throws SQLException {
        List<T> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapper.map(rs));
                }
            }
        }
        return list;
    }

    static <T> T queryOne(String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        List<T> list = query(sql, Arrays.asList(params), mapper);
        return list.isEmpty() ? null : list.get(0);
    }

    static int count(String sql, Object... params) throws SQLException {
        Integer n = queryOne(sql, rs -> rs.getInt(1), params);
        return n == null ? 0 : n;
    }

    /** Runs an INSERT and returns the AUTO_INCREMENT key that MySQL generated (or 0 if none). */
    static int insert(String sql, Object... params) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, Arrays.asList(params));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    /** Runs an UPDATE or DELETE and returns the number of rows changed. */
    static int update(String sql, Object... params) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, Arrays.asList(params));
            return ps.executeUpdate();
        }
    }

    static void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            if (value instanceof LocalDate) {
                ps.setDate(i + 1, java.sql.Date.valueOf((LocalDate) value));
            } else if (value instanceof LocalTime) {
                ps.setTime(i + 1, java.sql.Time.valueOf((LocalTime) value));
            } else {
                ps.setObject(i + 1, value);
            }
        }
    }

    static LocalDate date(ResultSet rs, String column) throws SQLException {
        java.sql.Date d = rs.getDate(column);
        return d == null ? null : d.toLocalDate();
    }

    static LocalTime time(ResultSet rs, String column) throws SQLException {
        java.sql.Time t = rs.getTime(column);
        return t == null ? null : t.toLocalTime();
    }

    /** Stores empty optional text fields as NULL. */
    static String nullIfBlank(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }
}
