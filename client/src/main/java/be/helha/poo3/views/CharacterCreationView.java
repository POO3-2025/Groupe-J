package be.helha.poo3.views;

import be.helha.poo3.models.CharacterDTO;
import be.helha.poo3.services.CharacterService;
import be.helha.poo3.utils.LanternaUtils;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class CharacterCreationView {

    private final WindowBasedTextGUI gui;
    private final Screen screen;
    private final CharacterService characterService = new CharacterService();

    private static final Pattern STAT_PATTERN = Pattern.compile("[0-5]?");

    public CharacterCreationView(WindowBasedTextGUI gui, Screen screen) {
        this.gui = gui;
        this.screen = screen;
    }


    public void show() {
        LanternaUtils lu = new LanternaUtils(gui, screen);
        BasicWindow window = new BasicWindow("Créer un personnage");
        window.setHints(List.of(Window.Hint.CENTERED));

        Panel main = new Panel(new GridLayout(2));
        main.setPreferredSize(new TerminalSize(32, 12));

        TextBox nameBox = new TextBox(new TerminalSize(18, 1));
        main.addComponent(new Label("Nom :"));
        main.addComponent(nameBox);

        List<String> classes = List.of("Iop","Xelor","Cra");
        ComboBox<String> classBox = new ComboBox<>(classes);
        classBox.setSelectedIndex(0);


        main.addComponent(new Label("Classe :"));
        main.addComponent(classBox);


        TextBox conBox = numericBox();
        TextBox dexBox = numericBox();
        TextBox strBox = numericBox();

        main.addComponent(new Label("Constitution :"));
        main.addComponent(conBox);
        main.addComponent(new Label("Dextérité    :"));
        main.addComponent(dexBox);
        main.addComponent(new Label("Force        :"));
        main.addComponent(strBox);

        Label remainingLbl = new Label("Points restants : 5");
        remainingLbl.setLayoutData(GridLayout.createLayoutData(
                GridLayout.Alignment.BEGINNING,
                GridLayout.Alignment.CENTER,
                false, false,
                2, 1));

        main.addComponent(remainingLbl);

        Runnable recalc = () -> {
            int total = parse(conBox) + parse(dexBox) + parse(strBox);
            remainingLbl.setText("Points restants : " + (5 - total));
        };
        conBox.setTextChangeListener((o, n) -> recalc.run());
        dexBox.setTextChangeListener((o, n) -> recalc.run());
        strBox.setTextChangeListener((o, n) -> recalc.run());

        Panel btnPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        Button createBtn = new Button("Créer", () -> {
            String name = nameBox.getText().trim();
            int con = parse(conBox);
            int dex = parse(dexBox);
            int str = parse(strBox);
            int total = con + dex + str;
            String classe;
            switch (classBox.getSelectedItem()){
                case "Iop":
                    classe = "warrior";
                    break;
                case "Xelor":
                    classe = "mage";
                    break;
                case "Cra":
                    classe = "hunter";
                    break;
                default:
                    classe = "warrior";
                    break;
            }


            if (classe == null) {
                lu.openMessagePopup("Erreur", "Sélectionnez une classe.");
                return;
            }

            if (name.isEmpty()) {
                lu.openMessagePopup("Erreur", "Le nom ne peut pas être vide");
                return;
            }
            if (total > 5) {
                lu.openMessagePopup("Erreur", "Vous dépassez le total autorisé (5)");
                return;
            }

            CharacterDTO dto = new CharacterDTO(name, con, dex, str, classe);
            try {
                if (characterService.addCharacter(dto)) {
                    lu.openMessagePopup("Succès", "Personnage créé !");
                    window.close();
                } else {
                    lu.openMessagePopup("Erreur", "Création refusée par le serveur");
                }
            } catch (IOException ex) {
                lu.openMessagePopup("Erreur", "Impossible de contacter le serveur : " + ex.getMessage());
            }
        });

        Button cancelBtn = new Button("Annuler", window::close);

        btnPanel.addComponent(createBtn);
        btnPanel.addComponent(new EmptySpace(new TerminalSize(2, 1)));
        btnPanel.addComponent(cancelBtn);

        btnPanel.setLayoutData(GridLayout.createLayoutData(
                GridLayout.Alignment.CENTER,
                GridLayout.Alignment.CENTER,
                false, false,
                2, 1));                      // occupe 2 colonnes

        main.addComponent(btnPanel);

        window.setComponent(main);
        nameBox.takeFocus();                 // curseur sur le champ Nom
        gui.addWindowAndWait(window);
    }

    private TextBox numericBox() {
        return new TextBox()
                .setValidationPattern(STAT_PATTERN)
                .setPreferredSize(new TerminalSize(2, 1));
    }

    private int parse(TextBox box) {
        try {
            return Integer.parseInt(box.getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
