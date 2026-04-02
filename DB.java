package com.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DB {

    private static final String URL =
        "jdbc:sqlite:C:/Users/Admin/Desktop/JAVA Files/Osdl Project/Hotel-booking/hotel.db";

    static {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS booking (" +
                "  id          INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  name        TEXT    NOT NULL," +
                "  room        TEXT    NOT NULL," +
                "  room_number TEXT    NOT NULL," +
                "  days        INTEGER NOT NULL," +
                "  total       INTEGER NOT NULL," +
                "  checkin     TEXT," +
                "  checkout    TEXT," +
                "  phone       TEXT," +
                "  email       TEXT" +
                ")"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS admin (" +
                "  id       INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  username TEXT NOT NULL UNIQUE," +
                "  password TEXT NOT NULL" +
                ")"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS prices (" +
                "  id     INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  type   TEXT NOT NULL UNIQUE," +
                "  price  INTEGER NOT NULL" +
                ")"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS feedback (" +
                "  id          INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  guest_name  TEXT NOT NULL," +
                "  room_number TEXT NOT NULL," +
                "  rating      INTEGER NOT NULL," +
                "  comment     TEXT," +
                "  created_at  TEXT NOT NULL" +
                ")"
            );

            // Insert default prices if not present
            stmt.execute(
                "INSERT OR IGNORE INTO prices (type, price) VALUES ('Single', 1000)"
            );
            stmt.execute(
                "INSERT OR IGNORE INTO prices (type, price) VALUES ('Double', 2000)"
            );
            stmt.execute(
                "INSERT OR IGNORE INTO prices (type, price) VALUES ('Suite', 5000)"
            );

            System.out.println("DB initialized.");

        } catch (SQLException e) {
            System.err.println("DB init error: " + e.getMessage());
        }
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    public static boolean adminExists() {
        String sql = "SELECT COUNT(*) FROM admin";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.getInt(1) > 0;
        } catch (SQLException e) { return false; }
    }

    public static boolean registerAdmin(String username, String password) {
        String sql = "INSERT INTO admin (username, password) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { return false; }
    }

    public static boolean validateLogin(String username, String password) {
        String sql = "SELECT COUNT(*) FROM admin WHERE username = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.getInt(1) > 0;
        } catch (SQLException e) { return false; }
    }

    // ── Prices ────────────────────────────────────────────────────────────────

    public static int getPrice(String type) {
        String sql = "SELECT price FROM prices WHERE type = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("price");
        } catch (SQLException e) {
            System.err.println("DB getPrice error: " + e.getMessage());
        }
        // fallback defaults
        return switch (type) {
            case "Single" -> 1000;
            case "Double" -> 2000;
            default       -> 5000;
        };
    }

    public static boolean updatePrice(String type, int price) {
        String sql = "UPDATE prices SET price = ? WHERE type = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, price);
            ps.setString(2, type);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("DB updatePrice error: " + e.getMessage());
            return false;
        }
    }

    // ── Feedback ──────────────────────────────────────────────────────────────

    public static boolean insertFeedback(String guestName, String roomNumber,
                                          int rating, String comment) {
        String sql = "INSERT INTO feedback (guest_name, room_number, rating, comment, created_at)" +
                     " VALUES (?, ?, ?, ?, datetime('now', 'localtime'))";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, guestName);
            ps.setString(2, roomNumber);
            ps.setInt(3, rating);
            ps.setString(4, comment);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("DB feedback error: " + e.getMessage());
            return false;
        }
    }

    public static List<String[]> getAllFeedback() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT guest_name, room_number, rating, comment, created_at" +
                     " FROM feedback ORDER BY id DESC";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("guest_name"),
                    rs.getString("room_number"),
                    String.valueOf(rs.getInt("rating")),
                    rs.getString("comment"),
                    rs.getString("created_at")
                });
            }
        } catch (SQLException e) {
            System.err.println("DB getAllFeedback error: " + e.getMessage());
        }
        return list;
    }

    public static double getAverageRating() {
        String sql = "SELECT AVG(rating) FROM feedback";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.getDouble(1);
        } catch (SQLException e) { return 0.0; }
    }

    // ── Bookings ──────────────────────────────────────────────────────────────

    public static boolean insert(String name, String room, String roomNumber,
                                  int days, int total, String checkIn, String checkOut,
                                  String phone, String email) {
        String sql = "INSERT INTO booking (name, room, room_number, days, total, " +
                     "checkin, checkout, phone, email) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, room);
            ps.setString(3, roomNumber);
            ps.setInt(4, days);
            ps.setInt(5, total);
            ps.setString(6, checkIn);
            ps.setString(7, checkOut);
            ps.setString(8, phone);
            ps.setString(9, email);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("DB insert error: " + e.getMessage());
            return false;
        }
    }

    public static List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT name, room, room_number, days, total, " +
                     "checkin, checkout, phone, email FROM booking ORDER BY id DESC";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Booking(
                    rs.getString("name"), rs.getString("room"),
                    rs.getString("room_number"), rs.getInt("days"),
                    rs.getInt("total"), rs.getString("checkin"),
                    rs.getString("checkout"), rs.getString("phone"),
                    rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            System.err.println("DB fetch error: " + e.getMessage());
        }
        return list;
    }

    public static List<Booking> searchBookings(String query) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT name, room, room_number, days, total, checkin, checkout, phone, email" +
                     " FROM booking WHERE LOWER(name) LIKE ? ORDER BY id DESC";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + query.toLowerCase() + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Booking(
                    rs.getString("name"), rs.getString("room"),
                    rs.getString("room_number"), rs.getInt("days"),
                    rs.getInt("total"), rs.getString("checkin"),
                    rs.getString("checkout"), rs.getString("phone"),
                    rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            System.err.println("DB search error: " + e.getMessage());
        }
        return list;
    }

    public static Booking getBookingByRoom(String roomNumber) {
        String sql = "SELECT name, room, room_number, days, total, checkin, checkout, phone, email" +
                     " FROM booking WHERE room_number = ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Booking(
                    rs.getString("name"), rs.getString("room"),
                    rs.getString("room_number"), rs.getInt("days"),
                    rs.getInt("total"), rs.getString("checkin"),
                    rs.getString("checkout"), rs.getString("phone"),
                    rs.getString("email")
                );
            }
        } catch (SQLException e) {
            System.err.println("DB room lookup error: " + e.getMessage());
        }
        return null;
    }

    public static boolean deleteByRoom(String roomNumber) {
        String sql = "DELETE FROM booking WHERE room_number = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomNumber);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("DB delete error: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteByName(String name) {
        String sql = "DELETE FROM booking WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { return false; }
    }

    public static List<String> getBookedRooms() {
        List<String> booked = new ArrayList<>();
        String sql = "SELECT room_number FROM booking";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) booked.add(rs.getString("room_number"));
        } catch (SQLException e) {
            System.err.println("DB booked rooms error: " + e.getMessage());
        }
        return booked;
    }

    public static int getTotalRevenue() {
        String sql = "SELECT SUM(total) FROM booking";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.getInt(1);
        } catch (SQLException e) { return 0; }
    }

    public static int getTotalBookings() {
        String sql = "SELECT COUNT(*) FROM booking";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.getInt(1);
        } catch (SQLException e) { return 0; }
    }
}