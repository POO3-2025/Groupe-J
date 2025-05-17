package be.helha.poo3.views;

import be.helha.poo3.models.CharacterDTO;
import be.helha.poo3.models.CharacterWithPos;
import be.helha.poo3.models.Item;
import be.helha.poo3.models.RoomDTOClient;
import be.helha.poo3.services.CharacterService;
import be.helha.poo3.services.ExplorationService;
import be.helha.poo3.utils.LanternaUtils;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.screen.Screen;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExplorationView {
    private final WindowBasedTextGUI gui;
    private final Screen screen;
    private final CharacterService characterService = new CharacterService();
    private final LanternaUtils lanternaUtils;
    private final ExplorationService explorationService = new ExplorationService();
    private final BasicWindow mainWindow = new BasicWindow("Exploration");
    private CharacterWithPos character;

    public ExplorationView(WindowBasedTextGUI gui, Screen screen,CharacterWithPos character) throws IOException{
        this.gui = gui;
        this.screen = screen;
        this.character = character;
        this.lanternaUtils = new LanternaUtils(gui, screen);
    }

    public ExplorationView(WindowBasedTextGUI gui, Screen screen) throws IOException{
        this.gui = gui;
        this.screen = screen;
        this.lanternaUtils = new LanternaUtils(gui, screen);
    }


    public void show() throws IOException{
        mainWindow.setHints(List.of(Window.Hint.CENTERED));
        mainWindow.setTitle("Exploration");

        //initialise la vue pour la première fois
        updateMainWindowContent();
        gui.addWindowAndWait(mainWindow);
    }

    public void openChest() throws IOException {
        Panel chestContent = new Panel(new LinearLayout(Direction.VERTICAL));

        Item item = explorationService.openChest();

        chestContent.addComponent(new Label("Le coffre contient un(e) : " + item.getName()));
        chestContent.addComponent(new Button("Voir les détails", () -> {
            lanternaUtils.openMessagePopup("Détails", "Description : " + item.getDescription() + "\nRareté : " + item.getRarity());
        }));

        chestContent.addComponent(new Button("Prendre l'objet", () -> {
            try {
                boolean success = explorationService.getLootFromChest();
                if(success){
                    // Rafraîchit l'état de la salle courante
                    RoomDTOClient updatedRoom = explorationService.getCurrentRoom();
                }else {
                    lanternaUtils.openMessagePopup("Inventaire pleins","Votre inventaire est pleins, veuillez le vider.");
                }
                updateMainWindowContent();

            } catch (IOException e) {
                System.err.println("Erreur lors de la récupération: " + e.getMessage());
                e.printStackTrace();
                try {
                    // En cas d'erreur, on essaye de revenir à la vue principale
                    updateMainWindowContent();
                } catch (IOException ex) {
                    System.err.println("Erreur lors de la mise à jour après erreur: " + ex.getMessage());
                }
            }
        }));

        chestContent.addComponent(new Button("Retour", () -> {
            try {
                updateMainWindowContent();
            } catch (IOException e) {
                System.err.println("Erreur lors du retour: " + e.getMessage());
                e.printStackTrace();
            }
        }));

        mainWindow.setComponent(chestContent);
        gui.updateScreen();
    }

    public void move(String direction) throws IOException {
        System.out.println("Déplacement demandé vers: " + direction);
        System.out.println("Position avant déplacement: " + this.character.getPosition());

        RoomDTOClient room = explorationService.move(direction);

        // Mise à jour du personnage après le déplacement
        String position = room.getId();
        String[] pos = position.split(":");
        int x = Integer.parseInt(pos[0]);
        int y = Integer.parseInt(pos[1]);
        this.character = characterService.getInGameCharacter();
        this.character.setPosition(new Point(x,y));

        updateMainWindowContent();
    }

    public void leave() {
        try {
            if (characterService.leaveGame()) {
                lanternaUtils.openMessagePopup("Information", "Sortie effectuée");
            } else {
                lanternaUtils.openMessagePopup("Information", "Sortie non effectuée");
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la sortie: " + e.getMessage());
            e.printStackTrace();
        }
        mainWindow.close();
    }


    //méthode mettant à jour l'affichage
    private void updateMainWindowContent() throws IOException {
        // Vider et reconstruire complètement le panel principal
        Panel mainPanel = new Panel(new LinearLayout(Direction.VERTICAL));

        // Récupérer l'état actuel de la salle
        RoomDTOClient room = explorationService.getCurrentRoom();

        mainPanel.addComponent(new Label("Tu arrives dans une salle sombre, que fais-tu ?"));

        // Ajout de boutons conditionnels selon l'état de la salle

        if (room.isHasMonster()) {
            mainPanel.addComponent(new Label("Il y a un " + room.getMonster().getType()));
            mainPanel.addComponent(new Button("Attaquer le monstre", () -> {
                try {
                    new PvMFightView(gui, screen).mainWindow(null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        }

        if (room.isHasChest()) {

            mainPanel.addComponent(new Button("Ouvrir le coffre", () -> {
                try {
                    openChest();
                } catch (IOException e) {
                    System.err.println("Erreur lors de l'ouverture du coffre: " + e.getMessage());
                    e.printStackTrace();
                }
            }));
        }

        for (String direction : room.getExits()) {
            String buttonText;
            switch (direction.toLowerCase()) {
                case "north":
                    buttonText = "Aller au nord";
                    break;
                case "south":
                    buttonText = "Aller au sud";
                    break;
                case "east":
                    buttonText = "Aller à l'est";
                    break;
                case "west":
                    buttonText = "Aller à l'ouest";
                    break;
                default:
                    buttonText = "Aller " + direction;
                    break;
            }

            final String dir = direction;
            mainPanel.addComponent(new Button(buttonText, () -> {
                try {
                    move(dir);
                } catch (IOException e) {
                    System.err.println("Erreur lors du déplacement: " + e.getMessage());
                    e.printStackTrace();
                }
            }));
        }

        mainPanel.addComponent(new Button("Voir l'inventaire", () -> {
            new InventoryView(gui, screen).show();
        }));

        mainPanel.addComponent(new Button("Quitter", this::leave));

        // Met à jour la fenêtre principale avec le nouveau panel
        mainWindow.setComponent(mainPanel);

        // Force la mise à jour de l'écran
        gui.updateScreen();
    }
}
