package view;

import controller.ContexteApplication;
import model.Etudiant;
import model.Personne;
import model.Professeur;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** Fenêtre de connexion : image de fond (école), logo, login et mot de passe. */
public class LoginFrame extends JFrame {

    private final ContexteApplication ctx;
    private final JTextField txtLogin = new JTextField(20);
    private final JPasswordField txtMotDePasse = new JPasswordField(20);
    private final JLabel lblErreur = new JLabel(" ", SwingConstants.CENTER);

    public LoginFrame(ContexteApplication ctx) {
        super("Gestion des notes scolaires - Connexion");
        this.ctx = ctx;

        Image icone = Theme.chargerImage("logo.png");
        if (icone != null) {
            setIconImage(icone);
        }
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Image de fond de l'école + léger voile marine pour unifier avec la carte de connexion
        Theme.PanneauImage fond = new Theme.PanneauImage(Theme.chargerImage("ecole.jpg"), new Color(10, 20, 45, 55));
        fond.setLayout(new GridBagLayout());
        fond.add(construireCarte());
        setContentPane(fond);

        setMinimumSize(new Dimension(700, 640));
        setSize(1000, 700);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                txtLogin.requestFocusInWindow();
            }
        });
    }

    private Theme.Carte construireCarte() {
        Theme.Carte carte = new Theme.Carte(new GridBagLayout(), Color.WHITE, 16);
        carte.setBorder(new EmptyBorder(34, 48, 28, 48));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(3, 0, 3, 0);
        int ligne = 0;

        // Logo de l'établissement
        JLabel logo = new JLabel(Theme.icone("logo.png", 110, 110), SwingConstants.CENTER);
        c.gridy = ligne++;
        carte.add(logo, c);

        JLabel titre = Theme.etiquette("CONNEXION", Theme.POLICE_TITRE, Theme.PRIMAIRE);
        titre.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = ligne++;
        c.insets = new Insets(6, 0, 0, 0);
        carte.add(titre, c);

        JLabel sousTitre = Theme.etiquette("Application de gestion des notes scolaires", Theme.POLICE_PETITE, Theme.TEXTE_CLAIR);
        sousTitre.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = ligne++;
        c.insets = new Insets(0, 0, 10, 0);
        carte.add(sousTitre, c);

        JLabel filet = new JLabel(" ") {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                g.setColor(Theme.ACCENT);
                int w = 46;
                g.fillRect((getWidth() - w) / 2, getHeight() / 2, w, 2);
            }
        };
        filet.setPreferredSize(new Dimension(10, 12));
        c.gridy = ligne++;
        c.insets = new Insets(0, 0, 14, 0);
        carte.add(filet, c);

        // Login
        c.gridy = ligne++;
        c.insets = new Insets(4, 0, 2, 0);
        carte.add(Theme.etiquette("Login", Theme.POLICE_GRAS, Theme.TEXTE), c);
        Theme.styliserChamp(txtLogin);
        txtLogin.setPreferredSize(new Dimension(300, 40));
        c.gridy = ligne++;
        c.insets = new Insets(0, 0, 8, 0);
        carte.add(txtLogin, c);

        // Mot de passe
        c.gridy = ligne++;
        c.insets = new Insets(4, 0, 2, 0);
        carte.add(Theme.etiquette("Mot de passe", Theme.POLICE_GRAS, Theme.TEXTE), c);
        Theme.styliserChamp(txtMotDePasse);
        txtMotDePasse.setPreferredSize(new Dimension(300, 40));
        c.gridy = ligne++;
        c.insets = new Insets(0, 0, 2, 0);
        carte.add(txtMotDePasse, c);

        final JCheckBox afficher = new JCheckBox("Afficher le mot de passe");
        afficher.setOpaque(false);
        afficher.setFont(Theme.POLICE_PETITE);
        afficher.setForeground(Theme.TEXTE_CLAIR);
        afficher.setFocusPainted(false);
        final char echoParDefaut = txtMotDePasse.getEchoChar();
        afficher.addActionListener(e -> txtMotDePasse.setEchoChar(afficher.isSelected() ? (char) 0 : echoParDefaut));
        c.gridy = ligne++;
        c.insets = new Insets(0, 0, 4, 0);
        carte.add(afficher, c);

        // Message d'erreur
        lblErreur.setFont(Theme.POLICE_PETITE);
        lblErreur.setForeground(Theme.DANGER);
        c.gridy = ligne++;
        c.insets = new Insets(2, 0, 6, 0);
        carte.add(lblErreur, c);

        // Bouton
        JButton btnConnexion = Theme.bouton("SE CONNECTER", Theme.PRIMAIRE);
        btnConnexion.addActionListener(e -> seConnecter());
        getRootPane().setDefaultButton(btnConnexion); // la touche Entrée valide le formulaire
        c.gridy = ligne++;
        c.insets = new Insets(0, 0, 12, 0);
        carte.add(btnConnexion, c);

        JLabel aide = Theme.etiquette("Professeur : prof / 1234     Étudiant : etu / 1234", Theme.POLICE_PETITE, Theme.TEXTE_CLAIR);
        aide.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = ligne;
        c.insets = new Insets(0, 0, 0, 0);
        carte.add(aide, c);

        return carte;
    }

    private void seConnecter() {
        String login = txtLogin.getText().trim();
        String motDePasse = new String(txtMotDePasse.getPassword());

        if (login.isEmpty() || motDePasse.isEmpty()) {
            lblErreur.setText("Veuillez saisir votre login et votre mot de passe.");
            return;
        }
        Personne utilisateur = ctx.getAuthentification().authentifier(login, motDePasse);
        if (utilisateur == null) {
            lblErreur.setText("Login ou mot de passe incorrect.");
            txtMotDePasse.setText("");
            txtMotDePasse.requestFocusInWindow();
            return;
        }

        // Navigation vers l'espace correspondant au type d'utilisateur
        dispose();
        if (utilisateur instanceof Professeur) {
            new ProfesseurFrame(ctx, (Professeur) utilisateur).setVisible(true);
        } else if (utilisateur instanceof Etudiant) {
            new EtudiantFrame(ctx, (Etudiant) utilisateur).setVisible(true);
        }
    }
}
