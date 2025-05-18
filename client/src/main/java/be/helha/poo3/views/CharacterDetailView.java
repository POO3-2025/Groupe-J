package be.helha.poo3.views;

import be.helha.poo3.models.CharacterWithPos;
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

/**
 * Vue qui affiche les détails d'un personnage sélectionné, avec options de renommage,
 * suppression ou sélection pour jouer.
 */
public class CharacterDetailView {

    private final WindowBasedTextGUI gui;
    private final Screen screen;
    private final GameCharacter character;           // personnage courant
    private final CharacterService characterService = new CharacterService();
    private final LanternaUtils lanternaUtils;

    public CharacterDetailView(WindowBasedTextGUI gui, Screen screen, GameCharacter character) {
        this.gui = gui;
        this.screen = screen;
        this.character = character;
        this.lanternaUtils = new LanternaUtils(gui, screen);
    }

    /**
     * Affiche la fenêtre principale de détails du personnage.
     * Permet le renommage, la suppression ou la sélection pour le jeu.
     */
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

        buttonPanel.addComponent(new Button("Jouer", ()->{
            this.choiceCharacter(window);
        }));
        buttonPanel.addComponent(new Button("Retour", window::close));
        buttonPanel.addComponent(new Button("Supprimer", ()->{
            this.deleteCharacter(window);
        }));

        main.addComponent(buttonPanel);

        window.setComponent(main);
        gui.addWindowAndWait(window);
    }

    /**
     * Ouvre une popup pour renommer le personnage.
     * @param currentWindow La fenêtre actuellement affichée, à rafraîchir si renommage effectué.
     */
    private void renameCharacter(BasicWindow currentWindow) {
        String newName = lanternaUtils.openPopupWithInput("Renommer", "Nouveau nom :");
        if (newName == null || newName.isBlank() || newName.equals(character.getName())) {
            return;
        }

        try {
            if(characterService.updateCharacterName(character.getIdCharacter(), newName)){
                character.setName(newName);
            } else {
                lanternaUtils.openMessagePopup("Erreur lors du changement de nom", "Erreur de renommer");
            }

            LanternaUtils.refresh(currentWindow,this::mainWindow);
        } catch (IOException e) {
            lanternaUtils.openMessagePopup("Erreur", "Impossible de renommer : " + e.getMessage());
        }
    }

    /**
     * Supprime le personnage après confirmation utilisateur.
     * @param currentWindow La fenêtre active à fermer si suppression réussie.
     */
    private void deleteCharacter(BasicWindow currentWindow) {
        boolean confirm = lanternaUtils.openConfirmationPopup("Confirmation", "Voulez vous supprimer ce personnage?");
        if (confirm) {
            try {
                if(characterService.deleteCharacter(this.character.getIdCharacter())){
                    currentWindow.close();
                } else {
                    lanternaUtils.openMessagePopup("Erreur", "Impossible de supprimer ce personnage?");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Sélectionne le personnage courant pour démarrer une session de jeu.
     * @param parent La fenêtre à fermer lors de la transition vers l'exploration.
     */
    private void choiceCharacter(BasicWindow parent) {
        try {
            CharacterWithPos character = characterService.choiceCharacter(this.character.getIdCharacter());
            if (character != null) {
                System.out.println(character);
                parent.close();
                new ExplorationView(gui,screen,character).show();
            } else {
                lanternaUtils.openMessagePopup("Erreur", "Impossible de récupérer le personnage");
            }

        } catch (IOException | RuntimeException e) {
            lanternaUtils.openMessagePopup("Erreur", e.getMessage());
        }

    }
}
