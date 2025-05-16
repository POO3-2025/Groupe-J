package be.helha.poo3.views;

import be.helha.poo3.models.PVMFightDTO;
import be.helha.poo3.models.PvmTurnResult;
import be.helha.poo3.services.PVMFightService;
import be.helha.poo3.utils.LanternaUtils;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    public void mainWindow(PvmTurnResult result) {
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
            buttonActionPanel.addComponent(new Button("Finir le combat", ()->{
                try {
                    System.out.println(fightService.endFight().toString());
                    if (fight.getPlayerHp() > 0){
                        lanternaUtils.openMessagePopup("Bravo","Vous avez gagnez ce combat");
                        window.close();
                        new ExplorationView(gui,screen).show();
                    } else {
                        lanternaUtils.openMessagePopup("RIP", "Vous y arriverez la prochaine fois");
                        window.close();
                        new MainMenuView(gui,screen).show();
                    }

                }catch (Exception e) {
                    lanternaUtils.openMessagePopup("Erreur", e.getMessage());
                    window.close();
                    new ExplorationView(gui,screen).show();
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

    private void addLabelIfExists(Panel panel, String label, Object value) {
        if (value != null) {
            panel.addComponent(new Label(label + value));
        }
    }

}
