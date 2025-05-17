package be.helha.poo3.views;

import be.helha.poo3.models.PVPCharacter;
import be.helha.poo3.models.PVPFight;
import be.helha.poo3.models.PvmTurnResult;
import be.helha.poo3.services.PVMFightService;
import be.helha.poo3.services.PVPService;
import be.helha.poo3.utils.LanternaUtils;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class PVPFightView {
    private final WindowBasedTextGUI gui;
    private final Screen screen;
    private final PVPService fightService = new PVPService();
    private final LanternaUtils lanternaUtils;
    private ScheduledExecutorService scheduler;
    private int turn;

    public PVPFightView(WindowBasedTextGUI gui, Screen screen) {
        this.gui = gui;
        this.screen = screen;
        this.lanternaUtils = new LanternaUtils(gui, screen);
    }

    public void mainWindow(PVPFight fight, boolean waiting) {

        if (fight == null) {
            try{
                fight = fightService.getCurrentFight();
            } catch (IOException e) {
                lanternaUtils.openMessagePopup("Erreur", e.getMessage());
            }
        }
        assert fight != null;
        this.turn = fight.getTurn();
        BasicWindow window = new BasicWindow("Combat");
        window.setHints(List.of(Window.Hint.CENTERED));
        window.setTitle("Combat");

        assert fight != null;
        PVPCharacter player = fight.getMyCharacter();
        PVPCharacter opponent = fight.getOpponentCharacter();

        Panel mainPanel = new Panel(new LinearLayout(Direction.VERTICAL));

        mainPanel.addComponent(new Label("============ Ennemi ============="));
        mainPanel.addComponent(new Label("Nom           : "+ opponent.getPlayerName()));
        mainPanel.addComponent(new Label("Points de vie : "+ opponent.getPlayerHp() + "/" + opponent.getPlayerMaxHP()));

        mainPanel.addComponent(new Label("Action        : " + opponent.getPlayerAction()));
        mainPanel.addComponent(new Label("Dégâts subis  : " + opponent.getPlayerDamageTaken()));

        mainPanel.addComponent(new Label("============ Vous   ============="));
        mainPanel.addComponent(new Label("Name          : " + player.getPlayerName()));
        mainPanel.addComponent(new Label("Points de vie : " + player.getPlayerHp() + "/" + player.getPlayerMaxHP()));

        mainPanel.addComponent(new Label("Action        : " + player.getPlayerAction()));
        mainPanel.addComponent(new Label("Dégâts subis  : " + player.getPlayerDamageTaken()));

        Panel buttonActionPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        if(fight.isEndFight()) {
            buttonActionPanel.addComponent(new EmptySpace());
            buttonActionPanel.addComponent(new Button("Quitter", ()->{
                if (player.getPlayerHp() > 0 && opponent.getPlayerHp() < 0) {
                    lanternaUtils.openMessagePopup("Bravo","Vous avez gagnez ce défi");
                } else if (opponent.getPlayerHp() > 0 && player.getPlayerHp() < 0){
                    lanternaUtils.openMessagePopup("RIP", "Vous y arriverez la prochaine fois");
                    window.close();
                    new MainMenuView(gui,screen).show();
                } else if (player.getPlayerHp() > 0 && opponent.getPlayerHp() > 0){
                    lanternaUtils.openMessagePopup("Match null","Vous être mort en même temps");
                } else if (player.getPlayerAction().equals("forfeit")){
                    lanternaUtils.openMessagePopup("Abandon","Vous avez abandonné");
                } else if (opponent.getPlayerAction().equals("forfeit")){
                    lanternaUtils.openMessagePopup("Abandon","Votre opposant a abandonné");
                }

                window.close();
                try {
                    new ExplorationView(gui,screen).show();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
            buttonActionPanel.addComponent(new EmptySpace());
        } else {
            buttonActionPanel.addComponent(new Button("Attaquer", () -> {
                try {
                    PVPFight newResult = fightService.action("attack");
                    window.close();
                    mainWindow(newResult, newResult == null);
                } catch (IOException e) {
                    lanternaUtils.openMessagePopup("Erreur", e.getMessage());
                }

            }));
            buttonActionPanel.addComponent(new Button("Esquiver", () -> {
                try {
                    PVPFight newResult = fightService.action("dodge");
                    window.close();
                    mainWindow(newResult, newResult == null);
                } catch (IOException e) {
                    lanternaUtils.openMessagePopup("Erreur", e.getMessage());
                }
            }));
            buttonActionPanel.addComponent(new Button("Bloquer", () -> {
                try {
                    PVPFight newResult = fightService.action("block");
                    window.close();
                    mainWindow(newResult, newResult == null);
                } catch (IOException e) {
                    lanternaUtils.openMessagePopup("Erreur", e.getMessage());
                }
            }));
            buttonActionPanel.addComponent(new Button("Abandonner", () -> {
                try {
                    PVPFight newResult = fightService.action("forfeit");
                    window.close();
                    mainWindow(newResult, newResult == null);
                } catch (IOException e) {
                    lanternaUtils.openMessagePopup("Erreur", e.getMessage());
                }
            }));
        }
        mainPanel.addComponent(buttonActionPanel);

        window.setComponent(mainPanel);
        gui.addWindowAndWait(window);


    }

    public void refreshFight(PVPFight fight, BasicWindow window) {
        try {
            window.close();
            PVPFight refreshedFight = fightService.getFightById(fight.getFightId());
            new PVPFightView(this.gui,this.screen).mainWindow(fight, refreshedFight.getTurn() == fight.getTurn());
        } catch ( IOException e ) {
            lanternaUtils.openMessagePopup("Erreur", e.getMessage());
        }


    }
}
