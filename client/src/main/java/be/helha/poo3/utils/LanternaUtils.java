package be.helha.poo3.utils;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
        mainPanel.setPreferredSize(new TerminalSize(message.length(), 12));
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
        mainPanel.setPreferredSize(new TerminalSize(message.length(), 5));
        mainPanel.addComponent(new Label(message));

        Panel buttonsPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        buttonsPanel.addComponent(new EmptySpace());
        buttonsPanel.addComponent(new Button("Ok", menuWindow::close));
        mainPanel.addComponent(buttonsPanel);
        menuWindow.setComponent(mainPanel);
        gui.addWindowAndWait(menuWindow);
    }

    public String openPopupWithInput(String title, String message) {
        AtomicReference<String> result = new AtomicReference<>(null);
        BasicWindow menuWindow = new BasicWindow(title);
        menuWindow.setHints(List.of(Window.Hint.CENTERED));
        Panel mainPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        mainPanel.setPreferredSize(new TerminalSize(message.length(), 5));
        mainPanel.addComponent(new Label(message));

        TextBox input = new TextBox()
                .setPreferredSize(new TerminalSize(20, 1));

        mainPanel.addComponent(input);
        mainPanel.addComponent(new EmptySpace());

        Panel buttonsPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        buttonsPanel.addComponent(new Button("Ok", () -> {
            result.set(input.getText());
            menuWindow.close();
        }));

        buttonsPanel.addComponent(new EmptySpace(new TerminalSize(2, 1)));

        buttonsPanel.addComponent(new Button("Non", () -> {
            result.set(null);
            menuWindow.close();
        }));
        mainPanel.addComponent(buttonsPanel);

        menuWindow.setComponent(mainPanel);
        gui.addWindowAndWait(menuWindow);
        return result.get();

    }

    public static void refresh(BasicWindow view, Runnable viewOpeningMethod) {
        view.close();
        viewOpeningMethod.run();
    }
}
