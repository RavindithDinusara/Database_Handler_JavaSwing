package main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.ArrayList;

public class DatabaseHandlerSwing {
    private JFrame mainFrame;
    private JTabbedPane tabbedPane;
    
    // Database connection fields
    private JTextField dbHostField;
    private JTextField dbPortField;
    private JTextField dbNameField;
    private JTextField dbUsernameField;
    private JPasswordField dbPasswordField;
    
    // Backup fields
    private JTextField mysqldumpPathField;
    private JTextField backupFolderField;
    
    // Restore fields
    private JTextField mysqlPathField;
    private JTextField restoreFileField;
    private JTextField newDbNameField;
    
    private final String CONFIG_DIR;
    private final String CONFIG_FILE;
    private final String DB_CONFIG_FILE;

    // Color scheme
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private final Color DANGER_COLOR = new Color(231, 76, 60);
    private final Color WARNING_COLOR = new Color(241, 196, 15);
    private final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private final Color CARD_COLOR = Color.WHITE;
    private final Color TEXT_COLOR = new Color(51, 51, 51);
    private final Color BORDER_COLOR = new Color(220, 220, 220);

    // Auto-save tracking
    private boolean needsDBSave = false;
    private boolean needsBackupSave = false;

    public DatabaseHandlerSwing() {
        // Use AppData directory for configuration files
        String userHome = System.getProperty("user.home");
        CONFIG_DIR = userHome + File.separator + "DatabaseHandler";
        CONFIG_FILE = CONFIG_DIR + File.separator + "backup-config.properties";
        DB_CONFIG_FILE = CONFIG_DIR + File.separator + "db-config.properties";
        
        // Create config directory if it doesn't exist
        File configDir = new File(CONFIG_DIR);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DatabaseHandlerSwing().createAndShowGUI();
        });
    }

    private void createAndShowGUI() {
        setupLookAndFeel();
        
        mainFrame = new JFrame("SwinglineFX - Database Handler");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(900, 650);
        mainFrame.setMinimumSize(new Dimension(850, 600));
        mainFrame.setLocationRelativeTo(null);
        
        initializeUI();
        loadConfig();
        loadDBConfig();
        
        mainFrame.setVisible(true);
    }

    private void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void initializeUI() {
        // Create main panel with clean background
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Create header
        JPanel headerPanel = createHeaderPanel();
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        // Create tabs
        tabbedPane.addTab("Database Connection", createDBConnectionPanel());
        tabbedPane.addTab("Backup", createBackupPanel());
        tabbedPane.addTab("Restore", createRestorePanel());
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        mainFrame.setContentPane(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(20, 25, 20, 25)
        ));

        JLabel titleLabel = new JLabel("Database Handler");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);

        JLabel subtitleLabel = new JLabel("Backup & Restore Utility - Settings Auto-save");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 100, 100));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(CARD_COLOR);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);

        headerPanel.add(textPanel, BorderLayout.WEST);
        
        return headerPanel;
    }

    private JPanel createDBConnectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel contentPanel = createCardPanel();
        contentPanel.setLayout(new BorderLayout(0, 20));

        // Title
        JLabel titleLabel = createSectionLabel("Database Connection Settings");
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        // Info panel
               
       

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_COLOR);
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Database host
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.3;
        formPanel.add(createFieldLabel("Database Host:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        dbHostField = createTextField();
        dbHostField.setText("localhost");
        addAutoSaveListener(dbHostField, "db");
        formPanel.add(dbHostField, gbc);

        // Database port
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.3;
        formPanel.add(createFieldLabel("Database Port:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        dbPortField = createTextField();
        dbPortField.setText("3306");
        addAutoSaveListener(dbPortField, "db");
        formPanel.add(dbPortField, gbc);

        // Database name
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0.3;
        formPanel.add(createFieldLabel("Database Name:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        dbNameField = createTextField();
        dbNameField.setText("enterDB");
        addAutoSaveListener(dbNameField, "db");
        formPanel.add(dbNameField, gbc);

        // Database username
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.weightx = 0.3;
        formPanel.add(createFieldLabel("Database Username:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        dbUsernameField = createTextField();
        dbUsernameField.setText("root");
        addAutoSaveListener(dbUsernameField, "db");
        formPanel.add(dbUsernameField, gbc);

        // Database password
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.weightx = 0.3;
        formPanel.add(createFieldLabel("Database Password:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        dbPasswordField = new JPasswordField();
        styleTextField(dbPasswordField);
        addAutoSaveListener(dbPasswordField, "db");
        formPanel.add(dbPasswordField, gbc);

        // Buttons panel
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 0, 10);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        buttonPanel.setBackground(CARD_COLOR);
        
        JButton testButton = createPrimaryButton("Test Connection");
        testButton.addActionListener(e -> testDBConnection());
        
        testButton.addActionListener(e -> {
            saveDBConfig();

        });
        
        buttonPanel.add(testButton);
        
        formPanel.add(buttonPanel, gbc);

        contentPanel.add(formPanel, BorderLayout.SOUTH);
        
        panel.add(contentPanel, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createBackupPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel contentPanel = createCardPanel();
        contentPanel.setLayout(new BorderLayout(0, 20));

        // Title
        JLabel titleLabel = createSectionLabel("Database Backup");
        contentPanel.add(titleLabel, BorderLayout.NORTH);


        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_COLOR);
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // mysqldump path
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createFieldLabel("Path to mysqldump.exe:"), gbc);
        gbc.gridx = 1;
        JPanel mysqldumpPanel = new JPanel(new BorderLayout(8, 0));
        mysqldumpPanel.setBackground(CARD_COLOR);
        mysqldumpPathField = createTextField();
        mysqldumpPathField.setToolTipText("Path to mysqldump.exe (used for backup)");
        addAutoSaveListener(mysqldumpPathField, "backup");
        JButton browseMysqldumpBtn = createSecondaryButton("Browse");
        browseMysqldumpBtn.addActionListener(e -> {
            browseMysqldump();
            scheduleBackupSave();
        });
        mysqldumpPanel.add(mysqldumpPathField, BorderLayout.CENTER);
        mysqldumpPanel.add(browseMysqldumpBtn, BorderLayout.EAST);
        formPanel.add(mysqldumpPanel, gbc);

        // Backup folder
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createFieldLabel("Backup Destination Folder:"), gbc);
        gbc.gridx = 1;
        JPanel backupFolderPanel = new JPanel(new BorderLayout(8, 0));
        backupFolderPanel.setBackground(CARD_COLOR);
        backupFolderField = createTextField();
        backupFolderField.setToolTipText("Folder where backup files will be saved");
        addAutoSaveListener(backupFolderField, "backup");
        JButton browseBackupBtn = createSecondaryButton("Browse");
        browseBackupBtn.addActionListener(e -> {
            browseBackupFolder();
            scheduleBackupSave();
        });
        backupFolderPanel.add(backupFolderField, BorderLayout.CENTER);
        backupFolderPanel.add(browseBackupBtn, BorderLayout.EAST);
        formPanel.add(backupFolderPanel, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        buttonPanel.setBackground(CARD_COLOR);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        
        
        JButton backupButton = createSuccessButton("Backup Now");
        backupButton.addActionListener(e -> backupDatabase());
        
        backupButton.addActionListener(e -> {
            saveConfig();

        });
        
        buttonPanel.add(backupButton);

        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(contentPanel, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createRestorePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel contentPanel = createCardPanel();
        contentPanel.setLayout(new BorderLayout(0, 20));

        // Title
        JLabel titleLabel = createSectionLabel("Database Restore");
        contentPanel.add(titleLabel, BorderLayout.NORTH);


        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_COLOR);
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // MySQL path (now in Restore tab)
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createFieldLabel("Path to mysql.exe:"), gbc);
        gbc.gridx = 1;
        JPanel mysqlPanel = new JPanel(new BorderLayout(8, 0));
        mysqlPanel.setBackground(CARD_COLOR);
        mysqlPathField = createTextField();
        mysqlPathField.setToolTipText("Path to mysql.exe (used for restore)");
        addAutoSaveListener(mysqlPathField, "backup");
        JButton browseMysqlBtn = createSecondaryButton("Browse");
        browseMysqlBtn.addActionListener(e -> {
            browseMysqlExe();
            scheduleBackupSave();
        });
        mysqlPanel.add(mysqlPathField, BorderLayout.CENTER);
        mysqlPanel.add(browseMysqlBtn, BorderLayout.EAST);
        formPanel.add(mysqlPanel, gbc);

        // Restore file
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createFieldLabel("Restore SQL File:"), gbc);
        gbc.gridx = 1;
        JPanel restoreFilePanel = new JPanel(new BorderLayout(8, 0));
        restoreFilePanel.setBackground(CARD_COLOR);
        restoreFileField = createTextField();
        restoreFileField.setToolTipText("Select the .sql backup file to restore");
        JButton browseRestoreBtn = createSecondaryButton("Browse");
        browseRestoreBtn.addActionListener(e -> browseRestoreFile());
        restoreFilePanel.add(restoreFileField, BorderLayout.CENTER);
        restoreFilePanel.add(browseRestoreBtn, BorderLayout.EAST);
        formPanel.add(restoreFilePanel, gbc);

        // New database name
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createFieldLabel("New Database Name:"), gbc);
        gbc.gridx = 1;
        newDbNameField = createTextField();
        newDbNameField.setToolTipText("e.g., restored_db");
        formPanel.add(newDbNameField, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        buttonPanel.setBackground(CARD_COLOR);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        

        
        
        JButton restoreButton = createDangerButton("Restore Now");
        restoreButton.addActionListener(e -> restoreDatabase());
        
restoreButton.addActionListener(e -> {
            saveConfig();

        });
        
        buttonPanel.add(restoreButton);

        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(contentPanel, BorderLayout.NORTH);
        return panel;
    }

    // Auto-save functionality
    private void addAutoSaveListener(JComponent component, String configType) {
        component.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (configType.equals("db")) {
                    scheduleDBSave();
                } else if (configType.equals("backup")) {
                    scheduleBackupSave();
                }
            }
        });
    }

    private void scheduleDBSave() {
        needsDBSave = true;
        Timer timer = new Timer(1000, e -> {
            if (needsDBSave) {
                saveDBConfig();
                needsDBSave = false;
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void scheduleBackupSave() {
        needsBackupSave = true;
        Timer timer = new Timer(1000, e -> {
            if (needsBackupSave) {
                saveConfig();
                needsBackupSave = false;

            }
        });
        timer.setRepeats(false);
        timer.start();
    }


    // UI Helper methods
    private JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(25, 25, 25, 25)
        ));
        return card;
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(PRIMARY_COLOR);
        return label;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        styleTextField(field);
        return field;
    }

    private void styleTextField(JTextField field) {
        field.setPreferredSize(new Dimension(300, 35));
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(0, 8, 0, 8)
        ));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    private JButton createPrimaryButton(String text) {
        return createButton(text, SECONDARY_COLOR, Color.WHITE);
    }

    private JButton createSecondaryButton(String text) {
        return createButton(text, new Color(108, 117, 125), Color.WHITE);
    }

    private JButton createSuccessButton(String text) {
        return createButton(text, SUCCESS_COLOR, Color.WHITE);
    }

    private JButton createDangerButton(String text) {
        return createButton(text, DANGER_COLOR, Color.WHITE);
    }

    private JButton createButton(String text, Color backgroundColor, Color foregroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(foregroundColor);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 38));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(darkenColor(backgroundColor, 0.1f));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }

    private Color darkenColor(Color color, float factor) {
        return new Color(
            Math.max((int)(color.getRed() * (1 - factor)), 0),
            Math.max((int)(color.getGreen() * (1 - factor)), 0),
            Math.max((int)(color.getBlue() * (1 - factor)), 0)
        );
    }

    // File browsing methods
    private void browseMysqlExe() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select mysql.exe");
        fc.setFileFilter(new FileNameExtensionFilter("Executable Files", "exe"));
        if (fc.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (file.getName().equalsIgnoreCase("mysql.exe")) {
                mysqlPathField.setText(file.getAbsolutePath());
            } else {
                showAlert("Please select a valid mysql.exe file.", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void browseMysqldump() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select mysqldump.exe");
        fc.setFileFilter(new FileNameExtensionFilter("Executable Files", "exe"));
        if (fc.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (file.getName().equalsIgnoreCase("mysqldump.exe")) {
                mysqldumpPathField.setText(file.getAbsolutePath());
            } else {
                showAlert("Please select a valid mysqldump.exe file.", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void browseBackupFolder() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select Backup Folder");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            File folder = fc.getSelectedFile();
            backupFolderField.setText(folder.getAbsolutePath());
        }
    }

    private void browseRestoreFile() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select .sql Backup File");
        fc.setFileFilter(new FileNameExtensionFilter("SQL Files", "sql"));
        if (fc.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            restoreFileField.setText(file.getAbsolutePath());
        }
    }

    // Configuration methods
    private void saveConfig() {
        Properties props = new Properties();
        props.setProperty("mysqldumpPath", mysqldumpPathField.getText().trim());
        props.setProperty("backupFolder", backupFolderField.getText().trim());
        props.setProperty("mysqlPath", mysqlPathField.getText().trim());

        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "Backup and Restore Config");
            // No alert for auto-save
        } catch (IOException e) {
            showAlert("Failed to save backup/restore settings: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveDBConfig() {
        Properties props = new Properties();
        props.setProperty("dbHost", dbHostField.getText().trim());
        props.setProperty("dbPort", dbPortField.getText().trim());
        props.setProperty("dbName", dbNameField.getText().trim());
        props.setProperty("dbUsername", dbUsernameField.getText().trim());
        props.setProperty("dbPassword", new String(dbPasswordField.getPassword()));

        try (FileOutputStream fos = new FileOutputStream(DB_CONFIG_FILE)) {
            props.store(fos, "Database Connection Config");
            // No alert for auto-save
        } catch (IOException e) {
            showAlert("Failed to save database settings: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadConfig() {
        File config = new File(CONFIG_FILE);
        if (!config.exists()) return;

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(config)) {
            props.load(fis);
            mysqldumpPathField.setText(props.getProperty("mysqldumpPath", ""));
            backupFolderField.setText(props.getProperty("backupFolder", ""));
            mysqlPathField.setText(props.getProperty("mysqlPath", ""));
        } catch (IOException e) {
            showAlert("Failed to load backup/restore settings: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDBConfig() {
        File config = new File(DB_CONFIG_FILE);
        if (!config.exists()) return;

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(config)) {
            props.load(fis);
            dbHostField.setText(props.getProperty("dbHost", "localhost"));
            dbPortField.setText(props.getProperty("dbPort", "3306"));
            dbNameField.setText(props.getProperty("dbName", "enterDB"));
            dbUsernameField.setText(props.getProperty("dbUsername", "root"));
            dbPasswordField.setText(props.getProperty("dbPassword", ""));
        } catch (IOException e) {
            showAlert("Failed to load database settings: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    // Database operations
    private void testDBConnection() {
        String host = dbHostField.getText().trim();
        String port = dbPortField.getText().trim();
        String db = dbNameField.getText().trim();
        String user = dbUsernameField.getText().trim();
        String pass = new String(dbPasswordField.getPassword());

        if (host.isEmpty() || port.isEmpty() || db.isEmpty() || user.isEmpty()) {
            showAlert("Please enter all required database connection fields!", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (testConnection(host, port, user, pass, db)) {
            showAlert("Connected to database successfully.", JOptionPane.INFORMATION_MESSAGE);
        } else {
            showAlert("Connection failed. Check your connection settings!", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void backupDatabase() {
        String dumpPath = mysqldumpPathField.getText().trim();
        String backupFolder = backupFolderField.getText().trim();
        String host = dbHostField.getText().trim();
        String port = dbPortField.getText().trim();
        String username = dbUsernameField.getText().trim();
        String password = new String(dbPasswordField.getPassword());
        String dbName = dbNameField.getText().trim();

        if (dumpPath.isEmpty() || backupFolder.isEmpty() || host.isEmpty() || 
            port.isEmpty() || username.isEmpty() || dbName.isEmpty()) {
            showAlert("Please fill in all required fields!", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File mysqldump = new File(dumpPath);
        if (!mysqldump.exists()) {
            showAlert("mysqldump.exe not found!", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File destFolder = new File(backupFolder);
        if (!destFolder.exists() || !destFolder.isDirectory()) {
            showAlert("Invalid backup folder!", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!testConnection(host, port, username, password, dbName)) {
            showAlert("Cannot connect to database!", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String outputFile = backupFolder + File.separator + dbName + "_backup_" + timestamp + ".sql";

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    dumpPath,
                    "--host=" + host,
                    "--port=" + port,
                    "--user=" + username,
                    "--password=" + password,
                    dbName,
                    "--result-file=" + outputFile
            );
            pb.redirectErrorStream(true);

            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                showAlert("Backup completed successfully!\nFile: " + outputFile, JOptionPane.INFORMATION_MESSAGE);
            } else {
                showAlert("Backup failed. Exit code: " + exitCode + "\n" + output, JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            showAlert("Exception during backup: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void restoreDatabase() {
        String mysqlExePath = mysqlPathField.getText().trim();
        String sqlFilePath = restoreFileField.getText().trim();
        String newDbName = newDbNameField.getText().trim();
        String host = dbHostField.getText().trim();
        String port = dbPortField.getText().trim();
        String user = dbUsernameField.getText().trim();
        String pass = new String(dbPasswordField.getPassword());

        if (mysqlExePath.isEmpty() || sqlFilePath.isEmpty() || newDbName.isEmpty() || 
            host.isEmpty() || port.isEmpty() || user.isEmpty()) {
            showAlert("Please fill all fields and select a .sql file!", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File mysqlExe = new File(mysqlExePath);
        if (!mysqlExe.exists()) {
            showAlert("mysql.exe not found!", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File sqlFile = new File(sqlFilePath);
        if (!sqlFile.exists() || !sqlFile.getName().endsWith(".sql")) {
            showAlert("Please select a valid .sql file.", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 1. Create the new database via JDBC
        String url = "jdbc:mysql://" + host + ":" + port + "/?useSSL=false&allowPublicKeyRetrieval=true";
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             java.sql.Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + newDbName + "`");
        } catch (Exception e) {
            showAlert("Failed to create database: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Restore the SQL dump into the new database using mysql.exe
        try {
            List<String> command = new ArrayList<>();
            command.add(mysqlExePath);
            command.add("-h" + host);
            command.add("-P" + port);
            command.add("-u" + user);
            if (!pass.isEmpty()) command.add("-p" + pass);
            command.add(newDbName);

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);

            Process process = pb.start();

            try (OutputStream os = process.getOutputStream();
                 BufferedReader br = new BufferedReader(new FileReader(sqlFile))) {

                String line;
                while ((line = br.readLine()) != null) {
                    os.write((line + "\n").getBytes());
                }
                os.flush();
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                showAlert("Database restored successfully to '" + newDbName + "'.", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showAlert("Restore failed! Exit code: " + exitCode, JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            showAlert("Restore failed: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean testConnection(String host, String port, String user, String pass, String db) {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&allowPublicKeyRetrieval=true";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {            
            return false;
        }
    }

    private void showAlert(String message, int messageType) {
        JOptionPane.showMessageDialog(mainFrame, message, "Database Handler", messageType);
    }
}