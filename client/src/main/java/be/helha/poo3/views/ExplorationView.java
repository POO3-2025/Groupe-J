package be.helha.poo3.views;

import be.helha.poo3.services.CharacterService;
import be.helha.poo3.utils.LanternaUtils;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;

import java.io.IOException;
import java.util.List;

public class ExplorationView {
    private final WindowBasedTextGUI gui;
    private final Screen screen;
    private final CharacterService characterService = new CharacterService();
    private final LanternaUtils lanternaUtils;

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

        mainPanel.addComponent(new Button("Voir l'inventaire", () -> {
            menuWindow.close(); // ✅ Fermer proprement
            new InventoryView(gui, screen).show(menuWindow);
        }));


        mainPanel.addComponent(new Button("Quitter", ()->{
            this.leave(menuWindow);
        }));

        menuWindow.setComponent(mainPanel);
        gui.addWindowAndWait(menuWindow);
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
