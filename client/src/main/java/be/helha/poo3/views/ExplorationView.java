package be.helha.poo3.views;

import be.helha.poo3.models.Item;
import be.helha.poo3.models.RoomDTOClient;
import be.helha.poo3.services.CharacterService;
import be.helha.poo3.services.ExplorationService;
import be.helha.poo3.utils.LanternaUtils;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExplorationView {
    private final WindowBasedTextGUI gui;
    private final Screen screen;
    private final CharacterService characterService = new CharacterService();
    private final LanternaUtils lanternaUtils;
    private final ExplorationService explorationService = new ExplorationService();

    public ExplorationView(WindowBasedTextGUI gui, Screen screen) {
        this.gui = gui;
        this.screen = screen;
        this.lanternaUtils = new LanternaUtils(gui, screen);
    }

    public void show(){
        BasicWindow menuWindow = new BasicWindow("Exploration");
        menuWindow.setHints(List.of(Window.Hint.CENTERED));
        menuWindow.setTitle("Exploration");

        Panel mainPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        RoomDTOClient room;
        //affichage des différents boutons en fonction de la salle
        try {
            room = explorationService.getCurrentRoom();
        } catch (IOException e) {
            lanternaUtils.openMessagePopup("Erreur", e.getMessage());
            e.printStackTrace();
            menuWindow.close();
            return;
        }

        Boolean hasChest = room.isHasChest() ;
        Boolean hasMonsters = room.isHasMonster();
        List<String> directions = room.getExits();

        new Label("Tu arrives dans une salle sombre, que fais-tu ?  ");

        if(hasChest){
            mainPanel.addComponent(new Button("Ouvrir le coffre", ()->{
                try {
                    this.openChest(menuWindow);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        if(hasMonsters){
            mainPanel.addComponent(new Button("Attquer le monstre", ()->{
                menuWindow.close();
                new PvMFightView(gui, screen).mainWindow(null);
                show();
            }));
        }

        mainPanel.addComponent(new Button("Quitter", ()->{
            this.leave(menuWindow);
        }));

        menuWindow.setComponent(mainPanel);
        gui.addWindowAndWait(menuWindow);
    }

    public void openChest(BasicWindow parent) throws IOException {
        Panel ChestContent = new Panel(new LinearLayout(Direction.VERTICAL));

        Item item = explorationService.openChest();

        Panel chestContent = new Panel(new LinearLayout(Direction.VERTICAL));

        chestContent.addComponent(new Label("Le coffre contient un(e) : "+item.getName()));
        chestContent.addComponent(new Button("Voir les détails",()->{
                    try {
                        this.showObjectDetails(parent,item);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
        );

        parent.setComponent(chestContent);
        gui.addWindowAndWait(parent);

    }

    public void showObjectDetails(BasicWindow parent,Item item) throws IOException {
        Panel objectDetails = new Panel(new LinearLayout(Direction.VERTICAL));
        objectDetails.addComponent(new Label(item.getDescription()));
        objectDetails.addComponent(new Label(item.getType()));
        objectDetails.addComponent(new Label(item.getRarity().toString()));
        objectDetails.addComponent(new Label(item.getAdditionalAttributes().toString()));

        objectDetails.addComponent(new Button("Retour",() -> {
           // try {
                parent.close(); // Ferme la fenêtre actuelle
                this.show();    // Rouvre la vue principale
           /* } catch (IOException e) {
                throw new RuntimeException(e);
            }*/
        }));

        parent.setComponent(objectDetails);
        gui.addWindowAndWait(parent);
    }

    public void goBack(BasicWindow parent) throws IOException {

    }

    public void leave(BasicWindow parent){
        LanternaUtils lanternaUtils = new LanternaUtils(gui, screen);
        try {
            if(characterService.leaveGame()){
                lanternaUtils.openMessagePopup("Information","Sortie effectuée");
            } else {
                lanternaUtils.openMessagePopup("Information","Sortie non effectuée");
            }
        } catch (IOException e) {
            lanternaUtils.openMessagePopup("Error", e.getMessage());
        }
        parent.close();

    }
}
