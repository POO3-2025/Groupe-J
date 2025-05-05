package be.helha.poo3.views;

import be.helha.poo3.services.AuthService;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.screen.Screen;

import java.util.List;

/**
 * Cette classe représente l'interface de connexion utilisateur.
 */
public class LoginView {
    private final WindowBasedTextGUI gui;
    private final Screen screen;

    public LoginView(WindowBasedTextGUI gui, Screen screen) {
        this.gui = gui;
        this.screen = screen;
    }

    public void show() {
        // Création d'une fenêtre avec le titre "Connexion"
        BasicWindow loginWindow = new BasicWindow("Connexion");
        loginWindow.setHints(List.of(Window.Hint.CENTERED)); // Centrage automatique de la fenêtre

        // Panel principal en disposition verticale
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        panel.setPreferredSize(new TerminalSize(40, 16));

        // Champs de saisie
        TextBox usernameBox = new TextBox().setPreferredSize(new TerminalSize(30, 1));
        TextBox passwordBox = new TextBox().setMask('*').setPreferredSize(new TerminalSize(30, 1));

        // Ajout des composants dans le panel
        panel.addComponent(new Label("Nom d'utilisateur :"));
        panel.addComponent(usernameBox);
        panel.addComponent(new Label("Mot de passe :"));
        panel.addComponent(passwordBox);
        panel.addComponent(new EmptySpace());

        // Panel contenant les trois boutons côte à côte : Se connecter | S'inscrire | Quitter
        Panel buttons = new Panel(new GridLayout(3));

        buttons.addComponent(new Button("Se connecter", () -> {
            if (AuthService.authenticate(usernameBox.getText(), passwordBox.getText())) {
                loginWindow.close();
                new MainMenuView(gui, screen).show();
            } else {
                MessageDialog.showMessageDialog(gui, "Erreur", "Nom d'utilisateur et/ou mot de passe invalide.");
            }
        }));

        buttons.addComponent(new Button("S'inscrire", () -> {
            loginWindow.close();
            new RegistrationView(gui, screen).show();
        }));

        buttons.addComponent(new Button("Quitter", this::quit));

        // Ajout du panel des boutons au panel principal
        panel.addComponent(buttons);

        // Place le panel dans la fenêtre
        loginWindow.setComponent(panel);

        // Affiche la fenêtre et attend que l'utilisateur interagisse
        gui.addWindowAndWait(loginWindow);
    }

    private void quit() {
        AuthService.logout();
        try {
            screen.stopScreen();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
