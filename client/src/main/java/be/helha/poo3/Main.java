package be.helha.poo3;

import be.helha.poo3.views.LoginView;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            // Utilisation de DefaultTerminalFactory pour créer un terminal Swing
            DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
            // Spécifiez les dimensions ici
            terminalFactory.setInitialTerminalSize(new TerminalSize(80, 24));
            // Ajout du SwingTerminal dans un SwingTerminalFrame
            SwingTerminalFrame terminal = terminalFactory.createSwingTerminal();
            terminal.setVisible(true);
            // Désactiver la redimension
            terminal.setResizable(false);
            // Création de l'écran à partir du terminal
            Screen screen = new TerminalScreen(terminal);
            screen.startScreen(); // Démarre l'écran du terminal
            // Création de l'interface utilisateur Lanterna (avec screen)
            WindowBasedTextGUI gui = new MultiWindowTextGUI(screen);
            // Lancer la vue de connexion
            new LoginView(gui, screen).show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
