package be.helha.poo3;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;

import java.io.IOException;
import java.util.Map;

public class ConfigEditor {
    public static void main(String[] args) throws IOException {


        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();

        terminalFactory.setInitialTerminalSize(new TerminalSize(45, 16));

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
            Table<String> table = new Table<>("Type", "Nom", "Host", "Port", "User");

            if (config.getDatabases() != null) {
                addDatabaseEntries(config.getDatabases().getMysql(), "MySQL", table);
                addDatabaseEntries(config.getDatabases().getMongoDB(), "MongoDB", table);
            }
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

    private static void addDatabaseEntries(Map<String, ConfigurationDB.Databases.Details> dbMap, String type, Table<String> table) {
        if (dbMap != null) {
            dbMap.forEach((name, details) -> table.getTableModel().addRow(
                    type, name, details.getHost(), String.valueOf(details.getPort()), details.getUser()
            ));
        }
    }

    private static void editSelectedEntry(MultiWindowTextGUI gui, Table<String> table, ConfigurationDB config) {
        table.takeFocus();
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) return;
        System.out.println(selectedRow);

        String type = table.getTableModel().getCell(0, selectedRow);
        String name = table.getTableModel().getCell(1, selectedRow);
        ConfigurationDB.Databases.Details details = (type.equals("MySQL")) ?
                config.getDatabases().getMysql().get(name) :
                config.getDatabases().getMongoDB().get(name);

        if (details == null) {
            System.out.println(name + " not found");
            return;
        }

        BasicWindow editWindow = new BasicWindow("Edit Configuration");
        Panel editPanel = new Panel(new GridLayout(2));

        TextBox nameBox = new TextBox(details.getDatabase());
        TextBox hostBox = new TextBox(details.getHost());
        TextBox portBox = new TextBox(String.valueOf(details.getPort()));
        TextBox userBox = new TextBox(details.getUser());
        TextBox passBox = new TextBox(details.getPassword());

        editPanel.addComponent(new Label("Name:"));
        editPanel.addComponent(nameBox);
        editPanel.addComponent(new Label("Host:"));
        editPanel.addComponent(hostBox);
        editPanel.addComponent(new Label("Port:"));
        editPanel.addComponent(portBox);
        editPanel.addComponent(new Label("User:"));
        editPanel.addComponent(userBox);
        editPanel.addComponent(new Label("Password:"));
        editPanel.addComponent(passBox);

        Button saveButton = new Button("Save", () -> {
            details.setHost(hostBox.getText());
            details.setPort(Integer.parseInt(portBox.getText()));
            details.setUser(userBox.getText());
            details.setPassword(passBox.getText());
            editWindow.close();
            table.getTableModel().setCell(1, selectedRow, details.getDatabase());
            table.getTableModel().setCell(2, selectedRow, details.getHost());
            table.getTableModel().setCell(3, selectedRow, String.valueOf(details.getPort()));
            table.getTableModel().setCell(4, selectedRow, details.getUser());
            table.setSelectAction(() -> editSelectedEntry(gui, table, config));
            System.out.println(ConfigReader.writeFile(config));

        });
        editPanel.addComponent(saveButton);

        editWindow.setComponent(editPanel);
        gui.addWindowAndWait(editWindow);
    }
}