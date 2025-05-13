package be.helha.poo3.utils;

import be.helha.poo3.services.AuthService;
import be.helha.poo3.views.LoginView;
import be.helha.poo3.views.ProfileView;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class LanternaUtils {

    private final WindowBasedTextGUI gui;
    private final Screen screen;

    public LanternaUtils(WindowBasedTextGUI gui, Screen screen) {
        this.gui = gui;
        this.screen = screen;
    }

    public boolean openConfirmationPopup(String title, String message) {
        final AtomicBoolean result = new AtomicBoolean(false);

        BasicWindow menuWindow = new BasicWindow(title);
        menuWindow.setHints(List.of(Window.Hint.CENTERED));

        Panel mainPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        mainPanel.setPreferredSize(new TerminalSize(40, 12));
        mainPanel.addComponent(new Label(message));
        mainPanel.addComponent(new EmptySpace());

        Panel buttonsPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        // Oui
        buttonsPanel.addComponent(new Button("Oui", () -> {
            result.set(true);
            menuWindow.close();
        }));

        buttonsPanel.addComponent(new EmptySpace());


        buttonsPanel.addComponent(new Button("Non", () -> {
            result.set(false);
            menuWindow.close();
        }));

        mainPanel.addComponent(buttonsPanel);
        menuWindow.setComponent(mainPanel);

        gui.addWindowAndWait(menuWindow);
        return result.get();
    }


    public void openMessagePopup(String title, String message) {
        BasicWindow menuWindow = new BasicWindow(title);
        menuWindow.setHints(List.of(Window.Hint.CENTERED));
        Panel mainPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        mainPanel.setPreferredSize(new TerminalSize(20, 5));
        mainPanel.addComponent(new Label(message));

        Panel buttonsPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        buttonsPanel.addComponent(new EmptySpace());
        buttonsPanel.addComponent(new Button("Ok", menuWindow::close));
        mainPanel.addComponent(buttonsPanel);
        menuWindow.setComponent(mainPanel);
        gui.addWindowAndWait(menuWindow);

    }
}
