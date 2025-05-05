package be.helha.poo3.views;

import be.helha.poo3.services.PasswordService;
import be.helha.poo3.utils.UserSession;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.screen.Screen;

import java.util.List;

/**
 * Classe représentant la vue du profil utilisateur.
 */
public class ProfileView {
    private final WindowBasedTextGUI gui;
    private final Screen screen;

    public ProfileView(WindowBasedTextGUI gui, Screen screen) {
        this.gui = gui;
        this.screen = screen;
    }

    public void show() {
        // Nouvelle fenêtre centrée
        BasicWindow profileWindow = new BasicWindow("Mon Profil");
        profileWindow.setHints(List.of(Window.Hint.CENTERED));

        // Panneau vertical
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        panel.setPreferredSize(new TerminalSize(40, 12));

        // Informations du profil
        panel.addComponent(new Label("Profil de : " + UserSession.getUsername()));
        panel.addComponent(new EmptySpace());

        // Bouton "Changer mot de passe"
        panel.addComponent(new Button("Changer mot de passe", () -> openPasswordChangeDialog(profileWindow)));

        panel.addComponent(new EmptySpace());

        // Bouton "Retour" pour revenir au menu principal
        panel.addComponent(new Button("Retour", () -> {
            profileWindow.close();
            new MainMenuView(gui, screen).show();
        }));

        // Affichage
        profileWindow.setComponent(panel);
        gui.addWindowAndWait(profileWindow);
    }

    /**
     * Ouvre un popup permettant à l'utilisateur de changer son mot de passe.
     */
    private void openPasswordChangeDialog(BasicWindow profileWindow) {
        final BasicWindow passwordWindow = new BasicWindow("Changer mot de passe");
        passwordWindow.setHints(List.of(Window.Hint.CENTERED));

        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        panel.setPreferredSize(new TerminalSize(40, 10));

        TextBox newPasswordBox = new TextBox().setMask('*').setPreferredSize(new TerminalSize(30, 1));
        TextBox confirmPasswordBox = new TextBox().setMask('*').setPreferredSize(new TerminalSize(30, 1));

        panel.addComponent(new Label("Nouveau mot de passe :"));
        panel.addComponent(newPasswordBox);
        panel.addComponent(new Label("Confirmer mot de passe :"));
        panel.addComponent(confirmPasswordBox);

        panel.addComponent(new EmptySpace());

        Panel buttonPanel = new Panel(new GridLayout(2));

        // Bouton "Valider"
        buttonPanel.addComponent(new Button("Valider", () -> {
            String newPassword = newPasswordBox.getText().trim();
            String confirmPassword = confirmPasswordBox.getText().trim();

            // Vérification que tous les champs sont remplis
            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                MessageDialog.showMessageDialog(gui, "Erreur", "Tous les champs doivent être remplis.");
                return;
            }

            // Vérification que les mots de passe correspondent
            if (!newPassword.equals(confirmPassword)) {
                MessageDialog.showMessageDialog(gui, "Erreur", "Les mots de passe ne correspondent pas.");
                newPasswordBox.setText("");
                confirmPasswordBox.setText("");
                return;
            }

            // Tentative de changement du mot de passe
            if (PasswordService.changePassword(newPassword)) {
                MessageDialog.showMessageDialog(gui, "Succès", "Mot de passe modifié avec succès !");
                passwordWindow.close();
                profileWindow.close();
                new MainMenuView(gui, screen).show();
            } else {
                MessageDialog.showMessageDialog(gui, "Erreur", "Erreur lors de la mise à jour du mot de passe.");
            }
        }));

        // Bouton "Annuler"
        buttonPanel.addComponent(new Button("Annuler", passwordWindow::close));

        panel.addComponent(buttonPanel);

        passwordWindow.setComponent(panel);
        gui.addWindow(passwordWindow);
    }
}
