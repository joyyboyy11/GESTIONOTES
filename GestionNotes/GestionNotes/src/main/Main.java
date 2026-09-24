package main;

import controller.ContexteApplication;
import view.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Point d'entrée de l'application : crée les contrôleurs puis ouvre la fenêtre de connexion. */
public class Main {

    public static void main(String[] args) {
        // Texte lissé sur tous les systèmes
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            // « Look and feel » identique sur Windows, macOS et Linux
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ex) {
            // on garde le look and feel par défaut
        }

        SwingUtilities.invokeLater(() -> {
            ContexteApplication contexte = new ContexteApplication();
            new LoginFrame(contexte).setVisible(true);
        });
    }
}
