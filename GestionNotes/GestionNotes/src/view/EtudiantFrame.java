package view;

import controller.ContexteApplication;
import controller.DonneesController;
import model.Etudiant;
import model.Matiere;
import model.Note;
import model.Reclamation;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;

/**
 * Espace étudiant : photo, notes par matière, moyennes, moyenne générale et envoi de réclamations.
 */
public class EtudiantFrame extends JFrame {

    private static final String AUTRE = "Autre / Général";

    private final ContexteApplication ctx;
    private final Etudiant etudiant;

    private final JLabel lblPhoto = new JLabel();
    private final JComboBox<String> cbSemestre = new JComboBox<>(DonneesController.SEMESTRES);

    private final DefaultTableModel modeleNotes = new DefaultTableModel(
            new String[]{"Matière", "Devoir", "Examen", "Moyenne"}, 0) {
        @Override
        public boolean isCellEditable(int ligne, int colonne) {
            return false;
        }
    };
    private final JTable tableNotes = new JTable(modeleNotes);
    private final JLabel lblMoyenneGenerale = new JLabel("-", SwingConstants.CENTER);
    private final JLabel lblMention = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel lblDetailMoyenne = new JLabel(" ", SwingConstants.CENTER);

    private final JComboBox<String> cbMatiereReclamation = new JComboBox<>();
    private final JTextArea txtReclamation = new JTextArea(4, 20);
    private final DefaultTableModel modeleReclamations = new DefaultTableModel(
            new String[]{"Date", "Matière", "Statut"}, 0) {
        @Override
        public boolean isCellEditable(int ligne, int colonne) {
            return false;
        }
    };
    private final JTable tableReclamations = new JTable(modeleReclamations);

    public EtudiantFrame(ContexteApplication ctx, Etudiant etudiant) {
        super("Gestion des notes - Espace étudiant");
        this.ctx = ctx;
        this.etudiant = etudiant;

        Image icone = Theme.chargerImage("logo.png");
        if (icone != null) {
            setIconImage(icone);
        }
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel racine = new JPanel(new BorderLayout());
        racine.setBackground(Theme.FOND);
        racine.add(construireEntete(), BorderLayout.NORTH);
        racine.add(construireCorps(), BorderLayout.CENTER);
        setContentPane(racine);

        rafraichirPhoto();
        chargerNotes();
        chargerMesReclamations();

        cbSemestre.addActionListener(e -> chargerNotes());

        setMinimumSize(new Dimension(980, 660));
        setSize(1100, 720);
        setLocationRelativeTo(null);
    }

    // =====================================================================
    // Construction de l'interface
    // =====================================================================

    private JPanel construireEntete() {
        JButton btnDeconnexion = Theme.bouton("Déconnexion", Theme.DANGER);
        btnDeconnexion.addActionListener(e -> deconnecter());
        return Theme.entete("GESTION DES NOTES", "Espace étudiant",
                Theme.photoRonde(Theme.photoEtudiant(etudiant), 46),
                etudiant.getNomComplet(), etudiant.getMatricule(), btnDeconnexion);
    }

    private JPanel construireCorps() {
        JPanel corps = new JPanel(new BorderLayout(16, 0));
        corps.setOpaque(false);
        corps.setBorder(new EmptyBorder(16, 20, 18, 20));
        corps.add(construireProfil(), BorderLayout.WEST);

        JPanel centre = new JPanel(new GridBagLayout());
        centre.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.gridy = 0;
        c.weighty = 0.52;
        c.insets = new Insets(0, 0, 14, 0);
        centre.add(construireCarteNotes(), c);
        c.gridy = 1;
        c.weighty = 0.48;
        c.insets = new Insets(0, 0, 0, 0);
        centre.add(construireCarteReclamation(), c);
        corps.add(centre, BorderLayout.CENTER);
        return corps;
    }

    /** Colonne de gauche : photo, nom, classe, école. */
    private Theme.Carte construireProfil() {
        Theme.Carte carte = new Theme.Carte(null, Color.WHITE, 18);
        carte.setLayout(new BoxLayout(carte, BoxLayout.Y_AXIS));
        carte.setBorder(new EmptyBorder(22, 18, 18, 18));
        carte.setPreferredSize(new Dimension(250, 0));

        lblPhoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        carte.add(lblPhoto);
        carte.add(espace(12));

        carte.add(centre(Theme.etiquette(etudiant.getNomComplet(),
                new Font(Font.SANS_SERIF, Font.BOLD, 18), Theme.PRIMAIRE)));
        carte.add(espace(2));
        carte.add(centre(Theme.etiquette("Matricule : " + etudiant.getMatricule(), Theme.POLICE_PETITE, Theme.TEXTE_CLAIR)));
        carte.add(espace(16));

        carte.add(champProfil("Nom", etudiant.getNom()));
        carte.add(champProfil("Prénom", etudiant.getPrenom()));
        carte.add(champProfil("Classe", etudiant.getClasse().getNom()));
        carte.add(champProfil("École", etudiant.getClasse().getEcole().getNom()));

        carte.add(javax.swing.Box.createVerticalGlue());
        JButton btnPhoto = Theme.bouton("Changer la photo", Theme.PRIMAIRE);
        btnPhoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPhoto.addActionListener(e -> choisirPhoto());
        carte.add(btnPhoto);
        return carte;
    }

    private JPanel champProfil(String libelle, String valeur) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel l = Theme.etiquette(libelle, Theme.POLICE_PETITE, Theme.TEXTE_CLAIR);
        JLabel v = Theme.etiquette("<html><body style='width:190px'>" + valeur + "</body></html>", Theme.POLICE_GRAS, Theme.TEXTE);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        v.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(v);
        p.add(espace(8));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height + 20));
        return p;
    }

    private static Component espace(int hauteur) {
        return javax.swing.Box.createVerticalStrut(hauteur);
    }

    private static JLabel centre(JLabel label) {
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    /** Tableau des notes + moyenne générale. */
    private Theme.Carte construireCarteNotes() {
        Theme.Carte carte = new Theme.Carte(new BorderLayout(0, 10), Color.WHITE, 18);
        carte.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Titre + choix du semestre
        JPanel haut = new JPanel(new BorderLayout());
        haut.setOpaque(false);
        haut.add(Theme.etiquette("Mes notes", Theme.POLICE_SOUS_TITRE, Theme.PRIMAIRE), BorderLayout.WEST);
        Theme.styliserCombo(cbSemestre);
        cbSemestre.setPreferredSize(new Dimension(160, 34));
        JPanel droite = new JPanel(new BorderLayout(8, 0));
        droite.setOpaque(false);
        droite.add(Theme.etiquette("Semestre :", Theme.POLICE_GRAS, Theme.TEXTE), BorderLayout.WEST);
        droite.add(cbSemestre, BorderLayout.CENTER);
        haut.add(droite, BorderLayout.EAST);
        carte.add(haut, BorderLayout.NORTH);

        // Tableau
        Theme.styliserTable(tableNotes);
        tableNotes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableNotes.setDefaultRenderer(Object.class, new Theme.RenduTable(3, -1, 1, 2, 3));
        tableNotes.getColumnModel().getColumn(0).setPreferredWidth(320);
        JScrollPane defilement = new JScrollPane(tableNotes);
        defilement.setBorder(BorderFactory.createLineBorder(Theme.BORDURE));
        defilement.getViewport().setBackground(Color.WHITE);
        carte.add(defilement, BorderLayout.CENTER);

        // Moyenne générale
        JPanel bas = new JPanel();
        bas.setOpaque(false);
        bas.setLayout(new BoxLayout(bas, BoxLayout.Y_AXIS));
        JLabel titreMoyenne = Theme.etiquette("Moyenne Générale", Theme.POLICE_GRAS, Theme.TEXTE_CLAIR);
        titreMoyenne.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblMoyenneGenerale.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        lblMoyenneGenerale.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblMention.setFont(Theme.POLICE_GRAS);
        lblMention.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblDetailMoyenne.setFont(Theme.POLICE_PETITE);
        lblDetailMoyenne.setForeground(Theme.TEXTE_CLAIR);
        lblDetailMoyenne.setAlignmentX(Component.CENTER_ALIGNMENT);
        bas.add(titreMoyenne);
        bas.add(lblMoyenneGenerale);
        bas.add(lblMention);
        bas.add(lblDetailMoyenne);
        carte.add(bas, BorderLayout.SOUTH);
        return carte;
    }

    /** Formulaire de réclamation + historique des réclamations envoyées. */
    private Theme.Carte construireCarteReclamation() {
        Theme.Carte carte = new Theme.Carte(new GridLayout(1, 2, 18, 0), Color.WHITE, 18);
        carte.setBorder(new EmptyBorder(14, 16, 14, 16));

        // --- Formulaire ---
        JPanel formulaire = new JPanel(new BorderLayout(0, 8));
        formulaire.setOpaque(false);
        JPanel entete = new JPanel();
        entete.setOpaque(false);
        entete.setLayout(new BoxLayout(entete, BoxLayout.Y_AXIS));
        JLabel titreReclamation = Theme.etiquette("Réclamation", Theme.POLICE_SOUS_TITRE, Theme.PRIMAIRE);
        JLabel infoMatiere = Theme.etiquette("Matière concernée", Theme.POLICE_PETITE, Theme.TEXTE_CLAIR);
        for (Matiere m : etudiant.getClasse().getMatieres()) {
            cbMatiereReclamation.addItem(m.getNom());
        }
        cbMatiereReclamation.addItem(AUTRE);
        Theme.styliserCombo(cbMatiereReclamation);
        cbMatiereReclamation.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        titreReclamation.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoMatiere.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbMatiereReclamation.setAlignmentX(Component.LEFT_ALIGNMENT);
        entete.add(titreReclamation);
        entete.add(espace(6));
        entete.add(infoMatiere);
        entete.add(espace(2));
        entete.add(cbMatiereReclamation);
        formulaire.add(entete, BorderLayout.NORTH);

        txtReclamation.setLineWrap(true);
        txtReclamation.setWrapStyleWord(true);
        txtReclamation.setFont(Theme.POLICE);
        txtReclamation.setForeground(Theme.TEXTE);
        txtReclamation.setBorder(new EmptyBorder(6, 8, 6, 8));
        JScrollPane defilement = new JScrollPane(txtReclamation);
        defilement.setBorder(BorderFactory.createLineBorder(Theme.BORDURE));
        formulaire.add(defilement, BorderLayout.CENTER);

        JButton btnEnvoyer = Theme.bouton("ENVOYER", Theme.SUCCES);
        btnEnvoyer.addActionListener(e -> envoyerReclamation());
        JPanel boutons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        boutons.setOpaque(false);
        boutons.add(btnEnvoyer);
        formulaire.add(boutons, BorderLayout.SOUTH);

        // --- Historique ---
        JPanel historique = new JPanel(new BorderLayout(0, 8));
        historique.setOpaque(false);
        historique.add(Theme.etiquette("Mes réclamations envoyées", Theme.POLICE_SOUS_TITRE, Theme.PRIMAIRE), BorderLayout.NORTH);
        Theme.styliserTable(tableReclamations);
        tableReclamations.setDefaultRenderer(Object.class, new Theme.RenduTable(-1, 2, 0, 2));
        tableReclamations.getColumnModel().getColumn(0).setPreferredWidth(100);
        tableReclamations.getColumnModel().getColumn(1).setPreferredWidth(140);
        tableReclamations.getColumnModel().getColumn(2).setPreferredWidth(100);
        JScrollPane defHistorique = new JScrollPane(tableReclamations);
        defHistorique.setBorder(BorderFactory.createLineBorder(Theme.BORDURE));
        defHistorique.getViewport().setBackground(Color.WHITE);
        historique.add(defHistorique, BorderLayout.CENTER);

        carte.add(formulaire);
        carte.add(historique);
        return carte;
    }

    // =====================================================================
    // Données
    // =====================================================================

    private void chargerNotes() {
        String semestre = (String) cbSemestre.getSelectedItem();
        modeleNotes.setRowCount(0);
        for (Matiere matiere : etudiant.getClasse().getMatieres()) {
            Note note = ctx.getNotes().trouverNote(etudiant.getMatricule(), matiere.getNom(), semestre);
            if (note != null) {
                modeleNotes.addRow(new Object[]{matiere.getNom(), Note.formater(note.getDevoir()),
                        Note.formater(note.getExamen()), Note.formater(note.getMoyenne())});
            } else {
                modeleNotes.addRow(new Object[]{matiere.getNom(), "-", "-", "-"});
            }
        }

        // Moyenne générale = somme des moyennes des matières / nombre de matières
        Double generale = ctx.getNotes().moyenneGenerale(etudiant.getMatricule(), semestre);
        int notees = ctx.getNotes().notesDeEtudiant(etudiant.getMatricule(), semestre).size();
        int total = etudiant.getClasse().getMatieres().size();
        if (generale == null) {
            lblMoyenneGenerale.setText("-");
            lblMoyenneGenerale.setForeground(Theme.TEXTE_CLAIR);
            lblMention.setText(" ");
            lblDetailMoyenne.setText("Aucune note disponible pour ce semestre.");
        } else {
            lblMoyenneGenerale.setText(Note.formater(generale) + " / 20");
            lblMoyenneGenerale.setForeground(generale < 10 ? Theme.DANGER : Theme.SUCCES);
            lblMention.setText(mention(generale));
            lblMention.setForeground(generale < 10 ? Theme.DANGER : Theme.SUCCES);
            lblDetailMoyenne.setText("Calculée sur " + notees + " matière(s) notée(s) sur " + total);
        }
    }

    private static String mention(double moyenne) {
        if (moyenne < 10) {
            return "Insuffisant";
        } else if (moyenne < 12) {
            return "Passable";
        } else if (moyenne < 14) {
            return "Assez bien";
        } else if (moyenne < 16) {
            return "Bien";
        }
        return "Très bien";
    }

    private void chargerMesReclamations() {
        modeleReclamations.setRowCount(0);
        for (Reclamation r : ctx.getReclamations().pourEtudiant(etudiant.getMatricule())) {
            modeleReclamations.addRow(new Object[]{r.getDateCourte(), r.getMatiere(), r.getStatut().getLibelle()});
        }
    }

    private void envoyerReclamation() {
        String message = txtReclamation.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez écrire votre réclamation avant de l'envoyer.",
                    "Réclamation vide", JOptionPane.WARNING_MESSAGE);
            txtReclamation.requestFocusInWindow();
            return;
        }
        String matiere = (String) cbMatiereReclamation.getSelectedItem();
        String semestre = (String) cbSemestre.getSelectedItem();
        ctx.getReclamations().envoyer(etudiant, matiere, semestre, message);
        txtReclamation.setText("");
        chargerMesReclamations();
        JOptionPane.showMessageDialog(this, "Votre réclamation a bien été envoyée au professeur.",
                "Réclamation envoyée", JOptionPane.INFORMATION_MESSAGE);
    }

    // =====================================================================
    // Photo de profil
    // =====================================================================

    private void rafraichirPhoto() {
        lblPhoto.setIcon(Theme.photoRonde(Theme.photoEtudiant(etudiant), 130));
    }

    private void choisirPhoto() {
        JFileChooser choix = new JFileChooser();
        choix.setDialogTitle("Choisir une photo de profil");
        choix.setFileFilter(new FileNameExtensionFilter("Images (png, jpg, gif)", "png", "jpg", "jpeg", "gif"));
        if (choix.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        if (ctx.getDonnees().definirPhoto(etudiant, choix.getSelectedFile())) {
            rafraichirPhoto();
        } else {
            JOptionPane.showMessageDialog(this, "Ce fichier n'est pas une image valide.",
                    "Photo de profil", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =====================================================================
    // Déconnexion
    // =====================================================================

    private void deconnecter() {
        ctx.getAuthentification().deconnecter();
        dispose();
        new LoginFrame(ctx).setVisible(true);
    }
}
