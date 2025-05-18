package be.helha.poo3;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

public class ConfigEditor {
    public static void main(String[] args) throws IOException {


        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();

        terminalFactory.setInitialTerminalSize(new TerminalSize(60, 16));

        SwingTerminalFrame terminal = terminalFactory.createSwingTerminal();

        terminal.setVisible(true);

        terminal.setResizable(false);

        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();

        MultiWindowTextGUI gui = new MultiWindowTextGUI(screen);
        BasicWindow window = new BasicWindow("Configuration Editor");
        Panel mainPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        try {
            ConfigurationDB config = ConfigReader.readFile();
            Table<String> table = new Table<>("Target", "Host", "Port", "Database", "User");
            addDatabaseEntries(table, config);

            table.setSelectAction(() -> editSelectedEntry(gui, table, config));

            mainPanel.addComponent(table);

            Button exitButton = new Button("Exit", () -> {
                try {
                    screen.stopScreen();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            mainPanel.addComponent(exitButton);

            window.setComponent(mainPanel);
            gui.addWindowAndWait(window);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


    }

    private static void addDatabaseEntries(Table<String> table, ConfigurationDB config) {
        config.getDatabases().getMysql().forEach((env, details) ->
                table.getTableModel().addRow("MySQL " + env, details.getHost(), String.valueOf(details.getPort()), details.getDatabase(), details.getUser()));

        config.getDatabases().getMongoDB().forEach((env, details) ->
                table.getTableModel().addRow("Mongo " + env, details.getHost(), String.valueOf(details.getPort()), details.getDatabase(), details.getUser()));
    }


    private static void editSelectedEntry(MultiWindowTextGUI gui, Table<String> table, ConfigurationDB config) {
        table.takeFocus();
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) return;
        System.out.println(selectedRow);

        String target = table.getTableModel().getCell(0, selectedRow);
        String[] parts = target.split("\\s+", 2);
        if (parts.length != 2) return;
        String platform = parts[0];
        String env = parts[1];

        Map<String, ConfigurationDB.Databases.Details> map =
                platform.equalsIgnoreCase("MySQL") ? config.getDatabases().getMysql()
                        : config.getDatabases().getMongoDB();
        ConfigurationDB.Databases.Details details = map.get(env);
        if (details == null) {
            MessageDialog.showMessageDialog(gui, "Error", target + " not found", MessageDialogButton.OK);
            return;
        }

        BasicWindow editWindow = new BasicWindow("Edit: " + target);
        Panel editPanel = new Panel();
        editPanel.setLayoutManager(new GridLayout(2));


        // Editable fields
        editPanel.addComponent(new Label("Host:"));
        TextBox hostBox = new TextBox(new TerminalSize(20, 1)).setText(details.getHost());
        editPanel.addComponent(hostBox);

        editPanel.addComponent(new Label("Port:"));
        TextBox portBox = new TextBox(new TerminalSize(6, 1));
        portBox.setValidationPattern(Pattern.compile("^[0-9]{1,5}$"));
        portBox.setText(String.valueOf(details.getPort()));
        editPanel.addComponent(portBox);

        editPanel.addComponent(new Label("Database:"));
        TextBox dbBox = new TextBox(new TerminalSize(20, 1)).setText(details.getDatabase());
        editPanel.addComponent(dbBox);

        editPanel.addComponent(new Label("User:"));
        TextBox userBox = new TextBox(new TerminalSize(20, 1)).setText(details.getUser());
        editPanel.addComponent(userBox);

        editPanel.addComponent(new Label("Password:"));
        TextBox pwdBox = new TextBox(new TerminalSize(20, 1)).setMask('*').setText(details.getPassword());
        editPanel.addComponent(pwdBox);

        editPanel.addComponent(new EmptySpace(new TerminalSize(0, 0)));

        Button saveButton = new Button("Save", () -> {
            details.setHost(hostBox.getText());
            details.setPort(Integer.parseInt(portBox.getText()));
            details.setDatabase(dbBox.getText());
            details.setUser(userBox.getText());
            details.setPassword(pwdBox.getText());

            table.getTableModel().setCell(1, selectedRow, details.getHost());
            table.getTableModel().setCell(2, selectedRow, String.valueOf(details.getPort()));
            table.getTableModel().setCell(3, selectedRow, details.getDatabase());
            table.getTableModel().setCell(4, selectedRow, details.getUser());

            try {
                ConfigReader.writeFile(config);
            } catch (IOException e) {
                e.printStackTrace();
            }
            gui.removeWindow(editWindow);
        });
        editPanel.addComponent(saveButton);

        editWindow.setComponent(editPanel);
        gui.addWindowAndWait(editWindow);
    }
}