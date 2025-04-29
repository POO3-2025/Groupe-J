package be.helha.poo3.views;

import be.helha.poo3.services.AuthService;
import be.helha.poo3.utils.UserSession;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;

import java.util.List;

/**
 * Classe représentant le menu principal affiché après la connexion d'un utilisateur.
 */
public class MainMenuView {
    private final WindowBasedTextGUI gui;
    private final Screen screen;

    public MainMenuView(WindowBasedTextGUI gui, Screen screen) {
        this.gui = gui;
        this.screen = screen;
    }

    public void show() {
        // Crée une nouvelle fenêtre centrée
        BasicWindow menuWindow = new BasicWindow("Menu Principal");
        menuWindow.setHints(List.of(Window.Hint.CENTERED));

        // Crée un panneau vertical contenant tous les composants
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        panel.setPreferredSize(new TerminalSize(40, 10));

        // Message de bienvenue avec le nom d'utilisateur stocké en session
        panel.addComponent(new Label("Bienvenue " + UserSession.getUsername() + " !"));
        panel.addComponent(new EmptySpace());

        // Bouton de déconnexion → retourne à l'écran de connexion
        panel.addComponent(new Button("Déconnexion", () -> {
            AuthService.logout();               // Nettoie la session (token, username)
            menuWindow.close();                 // Ferme cette fenêtre
            new LoginView(gui, screen).show();  // Réouvre la fenêtre de connexion
        }));

        // Bouton de sortie complète de l'application
        panel.addComponent(new Button("Quitter", () -> {
            AuthService.logout(); // Nettoyage session
            try {
                screen.stopScreen(); // Stoppe le terminal proprement
                System.exit(0); // Ferme l'application
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        // Attache le panneau à la fenêtre et affiche le tout
        menuWindow.setComponent(panel);
        gui.addWindowAndWait(menuWindow);
    }
}