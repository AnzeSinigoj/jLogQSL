/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package source;

import source.OtherUtilityForms.DataEntry;
import java.awt.Frame;
import java.io.File;
import java.sql.Statement;
import java.util.Scanner;
import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import java.sql.ResultSetMetaData;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author anze
 */
public class DatabaseTools {

    public static Connection connectToDB(File dbFile) throws SQLException {
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        return DriverManager.getConnection(url);
    }

    public static void createDatabase(String dbPath) {
        String url = "jdbc:sqlite:" + dbPath;

        String[] sqlStatements = new String[]{
            "CREATE TABLE IF NOT EXISTS bands ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "name TEXT NOT NULL, "
            + "wavelength TEXT"
            + ");",
            "CREATE TABLE IF NOT EXISTS custom_qths ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "name TEXT NOT NULL, "
            + "qra_locator TEXT, "
            + "latitude TEXT, "
            + "longitude TEXT"
            + ");",
            "CREATE TABLE IF NOT EXISTS modes ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "name TEXT NOT NULL, "
            + "description TEXT"
            + ");",
            "CREATE TABLE IF NOT EXISTS log_owner ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "callsign TEXT NOT NULL, "
            + "name TEXT, "
            + "surname TEXT, "
            + "country TEXT, "
            + "licence_class TEXT, "
            + "notes TEXT"
            + ");",
            "CREATE TABLE IF NOT EXISTS log_entries ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "callsign TEXT NOT NULL, "
            + "date TEXT NOT NULL, "
            + "sent_report TEXT NOT NULL, "
            + "received_report TEXT NOT NULL, "
            + "power REAL, "
            + "frequency TEXT, "
            + "qth TEXT, "
            + "note TEXT, "
            + "modes_id INTEGER NOT NULL, "
            + "bands_id INTEGER NOT NULL, "
            + "custom_qths_id INTEGER, "
            + "FOREIGN KEY (modes_id) REFERENCES modes(id), "
            + "FOREIGN KEY (bands_id) REFERENCES bands(id), "
            + "FOREIGN KEY (custom_qths_id) REFERENCES custom_qths(id)"
            + ");"
        };

        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {

            for (String sql : sqlStatements) {
                stmt.execute(sql);
            }

            System.out.println("Database created or already exists at: " + dbPath);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    boolean isDatabaseValid(String dbPath) {
        String sql = "SELECT * FROM log_owner";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    public File readDataAndFindDB(JOptionPane prompt) {
        File data_file = new File("data" + File.separator + "user_data.txt");
        File data_dir = data_file.getParentFile();

        if (!data_dir.exists()) {
            data_dir.mkdir();
        }

        if (!data_file.exists() || data_file.length() == 0) {
            try {
                data_file.createNewFile();
                showPanelAndWait(new DataEntry(), "Data Entry");
            } catch (Exception e) {
                prompt.showMessageDialog(null, "Failed to create the data file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        String db_path = "data" + File.separator + "jLogQSL.db"; //Fallback path
        try {
            Scanner data = new Scanner(data_file);

            while (data.hasNext()) {
                String read = data.nextLine();
                if (read.contains("DB_path")) {
                    db_path = read.split("\"")[1];
                }
            }

        } catch (Exception e) {
            prompt.showMessageDialog(null, "Failed to read the data file.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        File DB = new File(db_path);

        if (!DB.exists()) {
            prompt.showMessageDialog(null, "Database not found.", "Error", JOptionPane.ERROR_MESSAGE);
            showPanelAndWait(new DataEntry(), "Data Entry");
        }

        if (!isDatabaseValid(db_path)) {
            prompt.showMessageDialog(null, "Database path not valid.", "Error", JOptionPane.ERROR_MESSAGE);
            showPanelAndWait(new DataEntry(), "Data Entry");
        }

        return DB;
    }

    public void showPanelAndWait(JPanel panel, String title) {
        JDialog dialog = new JDialog((Frame) null, title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public void setStringOrNull(PreparedStatement pstmt, int index, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            pstmt.setNull(index, java.sql.Types.VARCHAR);
        } else {
            pstmt.setString(index, value);
        }
    }

    public String returnQthID(String qth_name, String DBpathPublic, JOptionPane prompt) {
        String id = "-1";
        try (Connection DBcon = connectToDB(new File(DBpathPublic))) {
            String sql_call = "SELECT id FROM custom_qths where name = '" + qth_name + "' LIMIT 1";
            PreparedStatement pstmt = DBcon.prepareStatement(sql_call);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    id = rs.getString("id");
                }
            }

        } catch (SQLException e) {
            prompt.showMessageDialog(null, "Database connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return id;
    }

    public String returnModeID(String mode, String DBpathPublic, JOptionPane prompt) {
        String id = "-1";
        try (Connection DBcon = connectToDB(new File(DBpathPublic))) {
            String sql_call = "SELECT id FROM modes where name = '" + mode + "' LIMIT 1";
            PreparedStatement pstmt = DBcon.prepareStatement(sql_call);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    id = rs.getString("id");
                }
            }

        } catch (SQLException e) {
            prompt.showMessageDialog(null, "Database connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return id;
    }

    public String returnBandID(String band, String DBpathPublic, JOptionPane prompt) {
        String id = "-1";
        try (Connection DBcon = connectToDB(new File(DBpathPublic))) {
            String sql_call = "SELECT id FROM bands where name = '" + band + "' LIMIT 1";
            PreparedStatement pstmt = DBcon.prepareStatement(sql_call);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    id = rs.getString("id");
                }
            }

        } catch (SQLException e) {
            prompt.showMessageDialog(null, "Database connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return id;
    }

    public void insertDefaultModesAndBands(String DBpathPublic, JOptionPane prompt) {
        String[] insertStatements = new String[]{
            // Modes
            "INSERT INTO modes (name) VALUES ('USB');",
            "INSERT INTO modes (name) VALUES ('LSB');",
            "INSERT INTO modes (name) VALUES ('DSB');",
            "INSERT INTO modes (name) VALUES ('CW');",
            "INSERT INTO modes (name) VALUES ('FM');",
            "INSERT INTO modes (name) VALUES ('AM');",
            "INSERT INTO modes (name) VALUES ('RTTY');",
            "INSERT INTO modes (name) VALUES ('PSK31');",
            "INSERT INTO modes (name) VALUES ('FT8');",
            "INSERT INTO modes (name) VALUES ('FT4');",
            "INSERT INTO modes (name) VALUES ('JS8');",
            "INSERT INTO modes (name) VALUES ('MSK144');",
            "INSERT INTO modes (name) VALUES ('OLIVIA');",
            "INSERT INTO modes (name) VALUES ('PACKET');",
            "INSERT INTO modes (name) VALUES ('SSTV');",
            "INSERT INTO modes (name) VALUES ('WSPR');",
            "INSERT INTO modes (name) VALUES ('FSK441');",
            "INSERT INTO modes (name) VALUES ('JT65');",
            "INSERT INTO modes (name) VALUES ('JT9');",
            "INSERT INTO modes (name) VALUES ('D-STAR');",
            "INSERT INTO modes (name) VALUES ('DMR');",
            "INSERT INTO modes (name) VALUES ('C4FM');",
            "INSERT INTO modes (name) VALUES ('FreeDV');",
            // Bands
            "INSERT INTO bands (name) VALUES ('160m');",
            "INSERT INTO bands (name) VALUES ('80m');",
            "INSERT INTO bands (name) VALUES ('60m');",
            "INSERT INTO bands (name) VALUES ('40m');",
            "INSERT INTO bands (name) VALUES ('30m');",
            "INSERT INTO bands (name) VALUES ('20m');",
            "INSERT INTO bands (name) VALUES ('17m');",
            "INSERT INTO bands (name) VALUES ('15m');",
            "INSERT INTO bands (name) VALUES ('12m');",
            "INSERT INTO bands (name) VALUES ('10m');",
            "INSERT INTO bands (name) VALUES ('6m');",
            "INSERT INTO bands (name) VALUES ('4m');",
            "INSERT INTO bands (name) VALUES ('2m');",
            "INSERT INTO bands (name) VALUES ('70cm');",
            "INSERT INTO bands (name) VALUES ('33cm');",
            "INSERT INTO bands (name) VALUES ('23cm');",
            "INSERT INTO bands (name) VALUES ('13cm');",
            "INSERT INTO bands (name) VALUES ('9cm');",
            "INSERT INTO bands (name) VALUES ('6cm');",
            "INSERT INTO bands (name) VALUES ('3cm');",
            "INSERT INTO bands (name) VALUES ('1.25cm');"
        };

        try (Connection DBcon = connectToDB(new File(DBpathPublic)); Statement stmt = DBcon.createStatement()) {

            for (String sql : insertStatements) {
                stmt.executeUpdate(sql);
            }

        } catch (SQLException e) {
            prompt.showMessageDialog(null, "Failed to write to database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadCustomQTH(JComboBox box, String DBpathPublic, JOptionPane prompt) {
        box.removeAllItems();
        ArrayList<String> qths = new ArrayList<>();

        try (Connection DBcon = connectToDB(new File(DBpathPublic))) {
            String sql_call = "SELECT name FROM custom_qths";
            PreparedStatement pstmt = DBcon.prepareStatement(sql_call);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    qths.add(rs.getString("name"));
                }
            }

        } catch (SQLException e) {
            prompt.showMessageDialog(null, "Database connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        if (qths.size() == 0) {
            box.addItem("No custom QTHs found.");
        } else {
            for (String a : qths) {
                box.addItem(a);
            }
        }
    }

    public void loadModes(JComboBox modes, String DBpathPublic, JOptionPane prompt) {
        modes.removeAllItems();
        ArrayList<String> modes_arr = new ArrayList<>();

        try (Connection DBcon = connectToDB(new File(DBpathPublic))) {
            String sql_call = "SELECT name FROM modes";
            PreparedStatement pstmt = DBcon.prepareStatement(sql_call);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    modes_arr.add(rs.getString("name"));
                }
            }

        } catch (SQLException e) {
            prompt.showMessageDialog(null, "Database connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        if (modes_arr.size() == 0) {
            modes.addItem("No modes found.");
        } else {
            for (String a : modes_arr) {
                modes.addItem(a);
            }
        }
    }

    public void loadBands(JComboBox bands, String DBpathPublic, JOptionPane prompt) {
        bands.removeAllItems();
        ArrayList<String> bands_arr = new ArrayList<>();

        try (Connection DBcon = connectToDB(new File(DBpathPublic))) {
            String sql_call = "SELECT name FROM bands";
            PreparedStatement pstmt = DBcon.prepareStatement(sql_call);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bands_arr.add(rs.getString("name"));
                }
            }

        } catch (SQLException e) {
            prompt.showMessageDialog(null, "Database connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        if (bands_arr.size() == 0) {
            bands.addItem("No modes found.");
        } else {
            for (String a : bands_arr) {
                bands.addItem(a);
            }
        }
    }

    public void fillTableFromQuery(JTable table, String DBpathPublic, String query, JOptionPane prompt) {
        try (Connection DBcon = connectToDB(new File(DBpathPublic))) {
            PreparedStatement pstmt = DBcon.prepareStatement(query);

            try (ResultSet rs = pstmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                String[] columns = new String[columnCount];
                String[] columnNames = new String[columnCount];

                for (int i = 1; i <= columnCount; i++) {
                    columnNames[i - 1] = metaData.getColumnLabel(i);
                }

                DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
                    public boolean isCellEditable(int row, int column) {
                        return column != 0; // lock first column (ID)
                    }
                };

                while (rs.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        row[i - 1] = rs.getObject(i);
                    }
                    model.addRow(row);
                }

                table.setModel(model);
            }
        } catch (SQLException e) {
            prompt.showMessageDialog(null, "Database connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String[][] getArrayFromQuery(String DBpathPublic, String query, JOptionPane prompt) {
        try (Connection DBcon = connectToDB(new File(DBpathPublic)); PreparedStatement pstmt = DBcon.prepareStatement(query); ResultSet rs = pstmt.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            List<String[]> rows = new ArrayList<>();

            String[] columnNames = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                columnNames[i - 1] = metaData.getColumnLabel(i);
            }
            rows.add(columnNames);

            while (rs.next()) {
                String[] row = new String[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    Object obj = rs.getObject(i);
                    row[i - 1] = (obj == null) ? null : obj.toString();
                }
                rows.add(row);
            }

            String[][] result = new String[rows.size()][columnCount];
            for (int i = 0; i < rows.size(); i++) {
                result[i] = rows.get(i);
            }

            return result;

        } catch (SQLException e) {
            prompt.showMessageDialog(null, "Database connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
            return new String[0][0];
        }
    }

    public void updateDB(int id, String call, String date, String sent_rep, String rcv_rep, int band, int mode, double power, String freq, String qth, int custom_qth, String note, JOptionPane prompt, String DBPathPublic) {
        String sql = "UPDATE log_entries "
                + "SET callsign = ?, "
                + "date = ?, "
                + "sent_report = ?, "
                + "received_report = ?, "
                + "bands_id = ?, "
                + "modes_id = ?, "
                + "power = ?, "
                + "frequency = ?, "
                + "qth = ?, "
                + "custom_qths_id = ?, "
                + "note = ? "
                + "WHERE id = ?;";

        try (Connection DBcon = connectToDB(new File(DBPathPublic)); PreparedStatement stmt = DBcon.prepareStatement(sql)) {
            stmt.setString(1, call);
            stmt.setString(2, date);
            stmt.setString(3, sent_rep);
            stmt.setString(4, rcv_rep);
            stmt.setInt(5, band);
            stmt.setInt(6, mode);
            stmt.setDouble(7, power);
            stmt.setString(8, freq);
            stmt.setString(9, qth);
            stmt.setInt(10, custom_qth);
            stmt.setString(11, note);
            stmt.setInt(12, id);

            int rowsUpdated = stmt.executeUpdate();
            System.out.println(rowsUpdated + " row(s) updated.");
        } catch (SQLException e) {
            prompt.showMessageDialog(null, "Database error.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
