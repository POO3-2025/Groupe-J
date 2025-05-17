package be.helha.poo3.views;

import be.helha.poo3.services.InventoryService;
import be.helha.poo3.utils.LanternaUtils;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Classe représentant la vue de l'inventaire dans l'interface utilisateur.
 * Permet d'afficher les objets et équipements du joueur et d'interagir avec ceux-ci.
 */
public class InventoryView {
    private final WindowBasedTextGUI gui;
    private final Screen screen;
    private final InventoryService inventoryService = new InventoryService();
    private final LanternaUtils lanternaUtils;

    /**
     * Constructeur de InventoryView.
     *
     * @param gui    L'interface utilisateur textuelle.
     * @param screen L'écran associé.
     */
    public InventoryView(WindowBasedTextGUI gui, Screen screen) {
        this.gui = gui;
        this.screen = screen;
        this.lanternaUtils = new LanternaUtils(gui, screen);
    }

    /**
     * Affiche la fenêtre d'inventaire avec les objets et les équipements du joueur.
     *
     * @param //previousWindow La fenêtre précédente à cacher pendant l'affichage.
     */
    public void show(/*Window previousWindow*/) {
        BasicWindow inventoryWindow = new BasicWindow("Inventaire");
        inventoryWindow.setHints(List.of(Window.Hint.CENTERED));

        Panel mainPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        mainPanel.addComponent(new Label("===== Contenu de l'inventaire ====="));

        Map<String, Object> fullInventory = inventoryService.getFullInventory();

        // Affichage des objets
        List<Map<String, Object>> items = (List<Map<String, Object>>) fullInventory.get("items");
        if (items == null || items.isEmpty()) {
            mainPanel.addComponent(new Label("Aucun objet trouvé."));
        } else {
            for (Map<String, Object> item : items) {
                String name = (String) item.getOrDefault("name", "Objet inconnu");
                mainPanel.addComponent(new Button(name, () -> {
                    inventoryWindow.close();
                    new ItemView(gui, screen).show(item/*, inventoryWindow*/);
                }));
            }
        }

        mainPanel.addComponent(new EmptySpace());
        mainPanel.addComponent(new Label("===== Contenu de l'équipement ====="));

        addSlot(mainPanel, "Main", fullInventory.get("mainSlot"));
        addSlot(mainPanel, "Armor", fullInventory.get("armorSlot"));
        addSlot(mainPanel, "Secondaire", fullInventory.get("secondSlot"));

        mainPanel.addComponent(new EmptySpace());

        // Boutons de déséquipement
        mainPanel.addComponent(new Button("Enlever équipement main", () -> handleUnequip("main", fullInventory.get("mainSlot"), inventoryWindow)));
        mainPanel.addComponent(new Button("Enlever équipement secondaire", () -> handleUnequip("second", fullInventory.get("secondSlot"), inventoryWindow)));
        mainPanel.addComponent(new Button("Enlever armure", () -> handleUnequip("armor", fullInventory.get("armorSlot"), inventoryWindow)));

        mainPanel.addComponent(new EmptySpace());
        mainPanel.addComponent(new Button("Retour", () -> {
            inventoryWindow.close();
            try {
                new ExplorationView(gui, screen).show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));

        inventoryWindow.setComponent(mainPanel);
        //previousWindow.setVisible(false);
        gui.addWindowAndWait(inventoryWindow);
    }

    /**
     * Gère le retrait d'un objet d'un slot spécifique.
     *
     * @param slot             Le nom du slot à vider.
     * @param slotData         Les données de l'objet dans le slot.
     * @param inventoryWindow  La fenêtre d'inventaire actuelle.
     */
    private void handleUnequip(String slot, Object slotData, BasicWindow inventoryWindow) {
        if (slotData == null) {
            lanternaUtils.openMessagePopup("Info", "Le slot " + slot + " est déjà vide.");
            return;
        }

        boolean success = inventoryService.unequipSlot(slot);
        if (success) {
            lanternaUtils.openMessagePopup("Succès", "L’objet a été retiré du slot " + slot + ".");
            inventoryWindow.close();
            this.show(/*new BasicWindow()*/); // Recharge la vue avec une nouvelle instance
        } else {
            lanternaUtils.openMessagePopup("Erreur", "Le retrait de l’objet a échoué.");
        }
    }

    /**
     * Ajoute à l'affichage la description d'un slot d'équipement.
     *
     * @param panel   Le panneau auquel ajouter l'information.
     * @param label   Le nom du slot.
     * @param slotData Les données de l'objet contenu dans le slot.
     */
    private void addSlot(Panel panel, String label, Object slotData) {
        String content = "Vide";
        try {
            if (slotData instanceof Map<?, ?>) {
                Map<?, ?> itemMap = (Map<?, ?>) slotData;
                Object name = itemMap.get("name");
                if (name != null) {
                    content = name.toString();
                }
            }
        } catch (Exception e) {
            content = "(Erreur lecture)";
        }
        panel.addComponent(new Label(label + " : " + content));
    }
}
