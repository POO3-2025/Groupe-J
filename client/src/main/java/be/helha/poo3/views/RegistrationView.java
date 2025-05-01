package be.helha.poo3.views;

import be.helha.poo3.services.RegistrationService;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.screen.Screen;

import java.util.List;

/**
 * Classe représentant la vue d'enregistrement d'un nouvel utilisateur.
 */
public class RegistrationView {
    private final WindowBasedTextGUI gui;
    private final Screen screen;

    public RegistrationView(WindowBasedTextGUI gui, Screen screen) {
        this.gui = gui;
        this.screen = screen;
    }

    public void show() {
        BasicWindow registerWindow = new BasicWindow("Inscription");
        registerWindow.setHints(List.of(Window.Hint.CENTERED));

        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        panel.setPreferredSize(new TerminalSize(40, 18));

        // Champs de saisie
        TextBox usernameBox = new TextBox().setPreferredSize(new TerminalSize(30, 1));
        TextBox passwordBox = new TextBox().setMask('*').setPreferredSize(new TerminalSize(30, 1));
        TextBox confirmPasswordBox = new TextBox().setMask('*').setPreferredSize(new TerminalSize(30, 1));

        panel.addComponent(new Label("Nom d'utilisateur :"));
        panel.addComponent(usernameBox);

        panel.addComponent(new Label("Mot de passe :"));
        panel.addComponent(passwordBox);

        panel.addComponent(new Label("Confirmer mot de passe :"));
        panel.addComponent(confirmPasswordBox);

        panel.addComponent(new EmptySpace());

        Panel buttonPanel = new Panel(new GridLayout(2));

        // Bouton "S'inscrire"
        buttonPanel.addComponent(new Button("S'inscrire", () -> {
            String username = usernameBox.getText();
            String password = passwordBox.getText();
            String confirmPassword = confirmPasswordBox.getText();

            // Vérifie que tous les champs sont remplis
            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                MessageDialog.showMessageDialog(gui, "Champs manquants", "Tous les champs doivent être remplis.");
                return;
            }

            // Vérifie si les mots de passe correspondent
            if (!password.equals(confirmPassword)) {
                MessageDialog.showMessageDialog(gui, "Erreur", "Les mots de passe ne correspondent pas.");
                passwordBox.setText("");
                confirmPasswordBox.setText("");
                return;
            }

            // Tente de s'inscrire
            String result = RegistrationService.register(username, password);

            switch (result) {
                case "success":
                    MessageDialog.showMessageDialog(gui, "Succès", "Compte créé avec succès !");
                    registerWindow.close();
                    new LoginView(gui, screen).show();
                    break;
                case "username_taken":
                    MessageDialog.showMessageDialog(gui, "Erreur", "Ce nom d'utilisateur est déjà utilisé.");
                    usernameBox.setText("");
                    break;
                default:
                    MessageDialog.showMessageDialog(gui, "Erreur", "Erreur lors de la création du compte.");
                    passwordBox.setText("");
                    confirmPasswordBox.setText("");
                    break;
            }
        }));

        // Bouton "Retour"
        buttonPanel.addComponent(new Button("Retour", () -> {
            registerWindow.close();
            new LoginView(gui, screen).show();
        }));

        panel.addComponent(buttonPanel);
        registerWindow.setComponent(panel);
        gui.addWindowAndWait(registerWindow);
    }
}
