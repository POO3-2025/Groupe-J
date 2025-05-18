package be.helha.poo3.views;

import be.helha.poo3.models.CharacterWithPos;
import be.helha.poo3.models.PVMFightDTO;
import be.helha.poo3.models.PvmTurnResult;
import be.helha.poo3.services.CharacterService;
import be.helha.poo3.services.PVMFightService;
import be.helha.poo3.utils.LanternaUtils;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Classe représentant l'interface de combat joueur contre monstre (PvM).
 * Elle gère les actions disponibles (attaquer, esquiver, bloquer) et affiche
 * l'état actuel du combat (PV du joueur et du monstre, actions effectuées, etc.).
 */
public class PvMFightView {
    private final WindowBasedTextGUI gui;
    private final Screen screen;
    private final PVMFightService fightService = new PVMFightService();
    private final LanternaUtils lanternaUtils;

    public PvMFightView(WindowBasedTextGUI gui, Screen screen) {
        this.gui = gui;
        this.screen = screen;
        lanternaUtils = new LanternaUtils(gui, screen);
    }

    /**
     * Affiche la fenêtre principale du combat.
     * Si le combat n'a pas encore commencé, il démarre un nouveau combat via PVMFightService.
     * Si un résultat est fourni, il met à jour l'état du combat.
     *
     * @param result Résultat du tour précédent.
     * @throws IOException en cas d'erreur lors du chargement ou d'affichage.
     */
    public void mainWindow(PvmTurnResult result) throws IOException{
        BasicWindow window = new BasicWindow("Combat");
        window.setHints(List.of(Window.Hint.CENTERED));
        window.setTitle("Combat");
        PVMFightDTO fight;
        if(result == null) {
            try {
                fight = fightService.startFight();
            } catch (IOException e) {
                lanternaUtils.openMessagePopup("Erreur", e.getMessage());
                window.close();
                new ExplorationView(gui,screen).show();
                return;
            }
        }else{
            fight = new PVMFightDTO(result.isFightEnd(), result.getMonsterHealth(), result.getMonsterMaxHealth(), result.getPlayerHealth(), result.getPlayerMaxHealth(), result.getMonster(), result.getMonsterAction(), result.getPlayer(), result.getPlayerAction());
        }


        Panel mainPanel = new Panel(new LinearLayout(Direction.VERTICAL));

        mainPanel.addComponent(new Label("============ Ennemi ============="));
        mainPanel.addComponent(new Label("Nom           : "+fight.getMonsterName()));
        mainPanel.addComponent(new Label("Points de vie : "+fight.getMonsterHp()+"/"+fight.getMonsterMaxHp()));
        if (result != null) {
            mainPanel.addComponent(new Label("Action        : " + result.getMonsterAction()));
            mainPanel.addComponent(new Label("Dégâts subis  : " + result.getDamageMonsterTake()));
        }
        mainPanel.addComponent(new Label("============ Vous   ============="));
        mainPanel.addComponent(new Label("Name          : "+fight.getPlayerName()));
        mainPanel.addComponent(new Label("Points de vie : "+fight.getPlayerHp()+"/"+fight.getPlayerMaxHp()));
        if (result != null) {
            mainPanel.addComponent(new Label("Action        : " + result.getPlayerAction()));
            mainPanel.addComponent(new Label("Dégâts subis  : " + result.getDamagePlayerTake()));
        }

        Panel buttonActionPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        if(result != null && result.isFightEnd()) {
            buttonActionPanel.addComponent(new EmptySpace());
            buttonActionPanel.addComponent(new Button("Finir le combat", () -> {
                try {
                    Map<String,Object> item = fightService.endFight();
                    window.close();

                    if (fight.getPlayerHp() > 0) {
                        lanternaUtils.openMessagePopup("Bravo", item.containsKey("name")? "Vous avez gagné " + item.get("name"): "Vous avez gagné !");
                        window.close();
                    } else {
                        lanternaUtils.openMessagePopup("RIP", "Vous y arriverez la prochaine fois");
                        new MainMenuView(gui, screen).show();
                    }

                } catch (Exception e) {
                    String message = e.getMessage();
                    if (message != null && message.toLowerCase().contains("inventory")) {
                        lanternaUtils.openMessagePopup("Inventaire plein", "Votre inventaire est plein. Libérez de l'espace et réessayez.");
                        CharacterWithPos character = null;
                        try {
                            character = new CharacterService().getInGameCharacter();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        try {
                            new ExplorationView(gui, screen, character).show();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    } else {
                        lanternaUtils.openMessagePopup("Erreur", message);
                        new MainMenuView(gui, screen).show();
                    }
                }
            }));
            buttonActionPanel.addComponent(new EmptySpace());
        } else {
            buttonActionPanel.addComponent(new Button("Attaquer", () -> {
                try {
                    PvmTurnResult newResult = fightService.playTurn("attack");
                    window.close();
                    mainWindow(newResult);
                } catch (IOException e) {
                    lanternaUtils.openMessagePopup("Erreur", e.getMessage());
                }

            }));
            buttonActionPanel.addComponent(new Button("Esquiver", () -> {
                try {
                    PvmTurnResult newResult = fightService.playTurn("dodge");
                    window.close();
                    mainWindow(newResult);
                } catch (IOException e) {
                    lanternaUtils.openMessagePopup("Erreur", e.getMessage());
                }
            }));
            buttonActionPanel.addComponent(new Button("Bloquer", () -> {
                try {
                    PvmTurnResult newResult = fightService.playTurn("block");
                    window.close();
                    mainWindow(newResult);
                } catch (IOException e) {
                    lanternaUtils.openMessagePopup("Erreur", e.getMessage());
                }
            }));
        }
        mainPanel.addComponent(buttonActionPanel);

        window.setComponent(mainPanel);
        gui.addWindowAndWait(window);
    }

    /**
     * Ajoute un label à un panel si la valeur n'est pas nulle.
     *
     * @param panel Panel dans lequel insérer le label.
     * @param label Texte du label.
     * @param value Valeur à afficher (si non nulle).
     */
    private void addLabelIfExists(Panel panel, String label, Object value) {
        if (value != null) {
            panel.addComponent(new Label(label + value));
        }
    }

}
