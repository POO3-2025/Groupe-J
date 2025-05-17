package be.helha.poo3.views;

import be.helha.poo3.models.*;
import be.helha.poo3.services.CharacterService;
import be.helha.poo3.services.ExplorationService;
import be.helha.poo3.services.PVPService;
import be.helha.poo3.utils.LanternaUtils;
import be.helha.poo3.utils.UserSession;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExplorationView {
    private final WindowBasedTextGUI gui;
    private final Screen screen;
    private final CharacterService characterService = new CharacterService();
    private final LanternaUtils lanternaUtils;
    private final ExplorationService explorationService = new ExplorationService();
    private final BasicWindow mainWindow = new BasicWindow("Exploration");
    private CharacterWithPos character;
    private ScheduledExecutorService scheduler;
    private final PVPService pvpService = new PVPService();
    private volatile boolean isPaused = false;

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
        //startChallengePolling();
        mainWindow.setHints(List.of(Window.Hint.CENTERED));
        mainWindow.setTitle("Exploration");

        //initialise la vue pour la première fois
        updateMainWindowContent();
        gui.addWindowAndWait(mainWindow);
        //  scheduler.shutdown();
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
                    mainWindow.setVisible(false);
                    isPaused = true;
                    new PvMFightView(gui, screen).mainWindow(null);
                    updateMainWindowContent();
                    mainWindow.setVisible(true);
                    isPaused = false;
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
            mainWindow.setVisible(false);
            new InventoryView(gui, screen).show();
            mainWindow.setVisible(true);
        }));

        mainPanel.addComponent(new Button("Quitter", this::leave));

        // Met à jour la fenêtre principale avec le nouveau panel
        mainWindow.setComponent(mainPanel);

        // Force la mise à jour de l'écran
        gui.updateScreen();
    }

    private void startChallengePolling(){
            scheduler = Executors.newSingleThreadScheduledExecutor();
            Runnable poll = ()->{
                System.out.println("Polling");
                if(isPaused) return;

                try {
                    ChallengeRequest request = pvpService.getChallengeToMe();
                    if (request != null && request.getStatus().equals("PENDING")) {
                        isPaused = true;
                        gui.getGUIThread().invokeLater(()-> handleIncomingChallenge(request));
                        isPaused = false;
                    } else {
                        PVPFight fight = pvpService.getCurrentFight();
                        if (fight != null) {
                            isPaused = true;
                            gui.getGUIThread().invokeLater(()-> openFightView(fight));
                            isPaused = false;
                        }

                        if (request != null) System.out.println(request.getStatus());
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            };
            scheduler.scheduleAtFixedRate(poll, 0, 3, TimeUnit.SECONDS);

    }

    private void handleIncomingChallenge(ChallengeRequest request) {
        String msg = "Le joueur " + request.getChallengerName() + " vous défie !\nAccepter ?";
        boolean accept = lanternaUtils.openConfirmationPopup("Défi reçu", msg);

        try {
            if (accept) {
                pvpService.acceptChallenge(request.getId());
                new PVPFightView(gui, screen, request.getId()).mainWindow(null,false);
            } else {
                pvpService.declineChallenge(request.getId());
            }
        } catch (IOException e) {
            lanternaUtils.openMessagePopup("Erreur réseau", e.getMessage());
        }
    }

    private void openFightView(PVPFight fight) {
            new PVPFightView(gui,screen, fight.getFightId()).mainWindow(fight,false);

    }
}
