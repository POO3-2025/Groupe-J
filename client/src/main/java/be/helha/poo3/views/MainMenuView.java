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
        panel.setPreferredSize(new TerminalSize(40, 12)); // Ajuste légèrement la taille

        // Message de bienvenue
        panel.addComponent(new Label("Bienvenue " + UserSession.getUsername() + " !"));
        panel.addComponent(new EmptySpace());


        panel.addComponent(new Button("Voir mes personnages", () -> {
            menuWindow.close();
            new CharactersManagementView(gui, screen).mainWindow();
            show();
        }));

        // Bouton "Voir Profil"
        panel.addComponent(new Button("Voir Profil", () -> {
            menuWindow.close();
            new ProfileView(gui, screen).show();
        }));




        panel.addComponent(new EmptySpace());

        // Bouton "Déconnexion"
        panel.addComponent(new Button("Déconnexion", () -> {
            AuthService.logout();
            menuWindow.close();
            new LoginView(gui, screen).show();
        }));

        // Bouton "Quitter"
        panel.addComponent(new Button("Quitter", () -> {
            AuthService.logout();
            try {
                screen.stopScreen();
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        // Affiche la fenêtre
        menuWindow.setComponent(panel);
        gui.addWindowAndWait(menuWindow);
    }
}
