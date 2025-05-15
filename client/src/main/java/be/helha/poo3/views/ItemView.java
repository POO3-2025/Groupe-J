package be.helha.poo3.views;

import be.helha.poo3.services.InventoryService;
import be.helha.poo3.utils.LanternaUtils;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;

import java.util.List;
import java.util.Map;

/**
 * Classe représentant la vue de détail d’un item dans l’inventaire.
 * Permet d’afficher ses caractéristiques et d’interagir avec celui-ci
 * (utilisation, suppression ou équipement selon le type).
 */
public class ItemView {
    private final WindowBasedTextGUI gui;
    private final Screen screen;
    private final LanternaUtils lanternaUtils;
    private final InventoryService inventoryService = new InventoryService();

    /**
     * Constructeur de ItemView.
     *
     * @param gui    L'interface utilisateur textuelle.
     * @param screen L'écran associé.
     */
    public ItemView(WindowBasedTextGUI gui, Screen screen) {
        this.gui = gui;
        this.screen = screen;
        this.lanternaUtils = new LanternaUtils(gui, screen);
    }

    /**
     * Affiche la vue de détail d’un item et permet d’effectuer des actions dessus.
     *
     * @param item            Les données de l’objet à afficher.
     * @param //inventoryWindow La fenêtre d’inventaire précédente.
     */
    public void show(Map<String, Object> item/*, Window inventoryWindow*/) {
        BasicWindow itemWindow = new BasicWindow("Détail de l'item");
        itemWindow.setHints(List.of(Window.Hint.CENTERED));

        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        panel.addComponent(new Label("===== Informations ====="));
        addLabelIfExists(panel, "Nom", item.get("name"));
        addLabelIfExists(panel, "Type", item.get("type"));
        addLabelIfExists(panel, "Sous-type", item.get("subType"));
        addLabelIfExists(panel, "Rareté", item.get("rarity"));
        addLabelIfExists(panel, "Description", item.get("description"));

        panel.addComponent(new Label("===== Statistiques ====="));
        addLabelIfExists(panel, "Dégâts", item.get("damage"));
        addLabelIfExists(panel, "Défense", item.get("defense"));
        addLabelIfExists(panel, "Puissance", item.get("power"));
        addLabelIfExists(panel, "Capacité max.", item.get("capacity"));
        addLabelIfExists(panel, "Capacité actuelle", item.get("currentCapacity"));
        addLabelIfExists(panel, "Effet", item.get("effect"));


        panel.addComponent(new EmptySpace());

        String subType = item.get("subType") != null ? item.get("subType").toString() : "";

        if (subType.equalsIgnoreCase("Consumable")) {
            panel.addComponent(new Button("Utiliser", () -> {
                String itemId = (String) item.get("_id");
                if (itemId == null) {
                    lanternaUtils.openMessagePopup("Erreur", "ID de l'objet manquant.");
                    return;
                }

                boolean success = inventoryService.consumeItem(itemId);
                if (success) {
                    Object raw = item.get("currentCapacity");
                    if (raw instanceof Number number) {
                        int cap = number.intValue() - 1;
                        if (cap <= 0) {
                            lanternaUtils.openMessagePopup("Info", "Objet consommé et épuisé !");
                            itemWindow.close();
                            new InventoryView(gui, screen).show(/*new BasicWindow()*/);
                            return;
                        } else {
                            item.put("currentCapacity", cap);
                        }
                    }

                    lanternaUtils.openMessagePopup("Succès", "Objet utilisé !");
                    itemWindow.close();
                    show(item/*, inventoryWindow*/);
                } else {
                    lanternaUtils.openMessagePopup("Erreur", "Échec de consommation de l'objet.");
                }
            }));

            panel.addComponent(new Button("Supprimer", () -> {
                String itemId = (String) item.get("_id");
                if (itemId == null) {
                    lanternaUtils.openMessagePopup("Erreur", "ID de l'objet manquant.");
                    return;
                }

                boolean success = inventoryService.deleteItem(itemId);
                if (success) {
                    lanternaUtils.openMessagePopup("Succès", "Objet supprimé.");
                    itemWindow.close();
                    new InventoryView(gui, screen).show(/*new BasicWindow()*/);
                } else {
                    lanternaUtils.openMessagePopup("Erreur", "Échec de la suppression.");
                }
            }));
        } else {
            panel.addComponent(new Button("Équiper", () -> {
                String itemId = (String) item.get("_id");
                if (itemId == null) {
                    lanternaUtils.openMessagePopup("Erreur", "ID de l'objet manquant.");
                    return;
                }

                String slot = determineSlot(subType);
                if (slot == null) {
                    lanternaUtils.openMessagePopup("Erreur", "Objet non équipable : " + subType);
                    return;
                }

                boolean success = inventoryService.equipItem(slot, itemId);
                if (success) {
                    lanternaUtils.openMessagePopup("Succès", "Objet équipé dans le slot " + slot);
                    itemWindow.close();
                    new InventoryView(gui, screen).show(/*inventoryWindow*/);
                } else {
                    lanternaUtils.openMessagePopup("Erreur", "Échec de l’équipement.");
                }
            }));

            panel.addComponent(new Button("Jeter", () -> {
                String itemId = (String) item.get("_id");
                if (itemId == null) {
                    lanternaUtils.openMessagePopup("Erreur", "ID de l'objet manquant.");
                    return;
                }

                boolean success = inventoryService.deleteItem(itemId);
                if (success) {
                    lanternaUtils.openMessagePopup("Succès", "Objet supprimé.");
                    itemWindow.close();
                    new InventoryView(gui, screen).show(/*new BasicWindow()*/);
                } else {
                    lanternaUtils.openMessagePopup("Erreur", "Échec de la suppression.");
                }
            }));
        }

        panel.addComponent(new EmptySpace());

        panel.addComponent(new Button("Retour", () -> {
            itemWindow.close();
            new InventoryView(gui, screen).show(/*new BasicWindow()*/);
        }));

        itemWindow.setComponent(panel);
        //inventoryWindow.setVisible(false);
        gui.addWindowAndWait(itemWindow);
    }

    /**
     * Ajoute un label à l'interface si la valeur associée existe.
     *
     * @param panel Le panneau cible.
     * @param label Le libellé du champ.
     * @param value La valeur à afficher.
     */
    private void addLabelIfExists(Panel panel, String label, Object value) {
        if (value != null) {
            panel.addComponent(new Label(label + " : " + value));
        }
    }

    /**
     * Détermine le slot d’équipement correspondant au sous-type de l’objet.
     *
     * @param subType Le sous-type de l'objet.
     * @return Le nom du slot d’équipement, ou null si inconnu.
     */
    private String determineSlot(String subType) {
        if (subType == null) return null;
        return switch (subType.toLowerCase()) {
            case "weapon" -> "main";
            case "secondary equipment", "secondary equipement" -> "second";
            case "armor" -> "armor";
            default -> null;
        };
    }
}
