package be.helha.poo3.views;

import be.helha.poo3.models.GameCharacter;
import be.helha.poo3.services.CharacterService;
import be.helha.poo3.utils.LanternaUtils;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;

import java.io.IOException;
import java.util.List;

public class CharactersManagementView {
    private final WindowBasedTextGUI gui;
    private final Screen screen;
    private final CharacterService characterService = new CharacterService();

    public CharactersManagementView(WindowBasedTextGUI gui, Screen screen) {
        this.gui = gui;
        this.screen = screen;
    }

    public void mainWindow(){
        BasicWindow menuWindow = new BasicWindow("Menu des personnages");
        menuWindow.setHints(List.of(Window.Hint.CENTERED));
        menuWindow.setTitle("Menu des personnages");

        Panel mainPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        mainPanel.setPreferredSize(new TerminalSize(40, 16));

        ActionListBox listBox = new ActionListBox(new TerminalSize(38, 10));

        try {
            List<GameCharacter> characterList = characterService.getUserCharacter();
            if(characterList.isEmpty()){
                listBox.addItem("<Aucun personnage>", ()->{});
            }else{
                for(GameCharacter character : characterList){
                    listBox.addItem(character.getName(), ()-> openCharacterActionWindow(character, menuWindow));
                }
            }
        } catch (IOException e) {
            new LanternaUtils(gui, screen).openMessagePopup("Error", e.getMessage());
            return;
        }

        mainPanel.addComponent(listBox);
        mainPanel.addComponent(new EmptySpace());

        mainPanel.addComponent(new Button("Retour", ()->{
            menuWindow.close();
            new MainMenuView(gui, screen).show();
        }));

        mainPanel.addComponent(new Button("Créer un personnage", ()->{
            this.openCharacterCreationView(menuWindow);
        }));

        menuWindow.setComponent(mainPanel);

        listBox.takeFocus();
        gui.addWindowAndWait(menuWindow);

    }

    public void openCharacterActionWindow(GameCharacter character, BasicWindow parent){
        parent.close();
        new CharacterDetailView(this.gui,this.screen,character).mainWindow();
        mainWindow();
    }

    public void openCharacterCreationView(BasicWindow parent){
        parent.close();
        new CharacterCreationView(gui,screen).show();
        mainWindow();
    }


}
