package be.helha.poo3.views;

import be.helha.poo3.services.AuthService;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.screen.Screen;

import java.util.List;

/**
 * Cette classe représente l'interface de connexion utilisateur.
 *
 * <p>
 * Elle affiche une fenêtre centrée contenant :
 * <ul>
 *     <li>Un champ pour le nom d'utilisateur</li>
 *     <li>Un champ pour le mot de passe (masqué)</li>
 *     <li>Un bouton pour se connecter (avec appel à AuthService)</li>
 *     <li>Un bouton pour quitter l'application</li>
 * </ul>
 * </p>
 *
 * En cas de succès, l'utilisateur est redirigé vers le {@link MainMenuView}.
 * En cas d'échec, une boîte de dialogue d'erreur est affichée.
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
        panel.setPreferredSize(new TerminalSize(40, 14)); // Taille fixe pour homogénéité

        // Champs de saisie
        TextBox usernameBox = new TextBox().setPreferredSize(new TerminalSize(30, 1));
        TextBox passwordBox = new TextBox().setMask('*').setPreferredSize(new TerminalSize(30, 1));

        // Ajout des composants dans le panel
        panel.addComponent(new Label("Nom d'utilisateur :"));
        panel.addComponent(usernameBox);
        panel.addComponent(new Label("Mot de passe :"));
        panel.addComponent(passwordBox);
        panel.addComponent(new EmptySpace());

        // Panel contenant les boutons "Se connecter" et "Quitter"
        Panel buttons = new Panel(new GridLayout(2));

        // Bouton "Se connecter" → tente d’authentifier l'utilisateur
        buttons.addComponent(new Button("Se connecter", () -> {
            // Appel à AuthService pour authentification
            if (AuthService.authenticate(usernameBox.getText(), passwordBox.getText())) {
                loginWindow.close(); // Ferme la fenêtre actuelle
                new MainMenuView(gui, screen).show(); // Affiche le menu principal
            } else {
                // Affiche une boîte de dialogue d'erreur si échec de l'authentification
                MessageDialog.showMessageDialog(gui, "Erreur", "Nom d'utilisateur et/ou mot de passe invalide.");
            }
        }));

        // Bouton "Quitter" -> ferme l'application proprement
        buttons.addComponent(new Button("Quitter", this::quit));

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
