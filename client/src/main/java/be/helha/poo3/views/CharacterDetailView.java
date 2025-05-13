package be.helha.poo3.views;

import be.helha.poo3.models.GameCharacter;
import be.helha.poo3.services.CharacterService;
import be.helha.poo3.utils.LanternaUtils;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.SGR;

import java.io.IOException;
import java.util.List;

public class CharacterDetailView {

    private final WindowBasedTextGUI gui;
    private final Screen screen;
    private final GameCharacter character;           // personnage courant
    private final CharacterService characterService = new CharacterService();

    public CharacterDetailView(WindowBasedTextGUI gui, Screen screen, GameCharacter character) {
        this.gui = gui;
        this.screen = screen;
        this.character = character;
    }

    public void mainWindow() {
        BasicWindow window = new BasicWindow("Détails de " + character.getName());
        window.setHints(List.of(Window.Hint.CENTERED));

        Panel main = new Panel(new LinearLayout(Direction.VERTICAL));
        main.setPreferredSize(new TerminalSize(40, 14));


        Button nameButton = new Button("Nom : " + character.getName(),
                () -> renameCharacter(window));

        nameButton.setRenderer(new InteractableRenderer<Button>() {

            @Override
            public TerminalPosition getCursorLocation(Button button) {
                return null;
            }

            @Override
            public TerminalSize getPreferredSize(Button component) {
                // largeur = longueur du libellé, hauteur = 1 ligne
                return new TerminalSize(component.getLabel().length(), 1);
            }

            @Override
            public void drawComponent(TextGUIGraphics graphics, Button component) {
                graphics.enableModifiers(SGR.UNDERLINE);           // souligné
                graphics.putString(0, 0, component.getLabel());    // texte
                graphics.disableModifiers(SGR.UNDERLINE);
            }
        });


        main.addComponent(nameButton);

        main.addComponent(new Label("HP       : " + character.getCurrentHP() + " / " + character.getMaxHP()));
        main.addComponent(new Label("Force    : " + character.getStrength()));
        main.addComponent(new Label("Dextérité: " + character.getDexterity()));
        main.addComponent(new Label("Constit. : " + character.getConstitution()));

        main.addComponent(new EmptySpace());
        Panel buttonPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));


        buttonPanel.addComponent(new Button("Retour", window::close));
        buttonPanel.addComponent(new Button("Supprimer", ()->{
            this.deleteCharacter(window);
        }));

        main.addComponent(buttonPanel);

        window.setComponent(main);
        gui.addWindowAndWait(window);
    }

    private void renameCharacter(BasicWindow currentWindow) {
        LanternaUtils lu = new LanternaUtils(gui, screen);
        String newName = lu.openPopupWithInput("Renommer", "Nouveau nom :");
        if (newName == null || newName.isBlank() || newName.equals(character.getName())) {
            return;
        }

        try {
            if(characterService.updateCharacterName(character.getIdCharacter(), newName)){
                character.setName(newName);
            } else {
                lu.openMessagePopup("Erreur lors du changement de nom", "Erreur de renommer");
            }

            LanternaUtils.refresh(currentWindow,this::mainWindow);
        } catch (IOException e) {
            lu.openMessagePopup("Erreur", "Impossible de renommer : " + e.getMessage());
        }
    }

    private void deleteCharacter(BasicWindow currentWindow) {
        LanternaUtils lu = new LanternaUtils(gui, screen);
        boolean confirm = lu.openConfirmationPopup("Confirmation", "Voulez vous supprimer ce personnage?");
        if (confirm) {
            try {
                if(characterService.deleteCharacter(this.character.getIdCharacter())){
                    currentWindow.close();
                } else {
                    lu.openMessagePopup("Erreur", "Impossible de supprimer ce personnage?");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else return;
    }
}
