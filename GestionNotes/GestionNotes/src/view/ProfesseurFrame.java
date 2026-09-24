package view;

import controller.ContexteApplication;
import controller.DonneesController;
import model.ClasseEtude;
import model.Ecole;
import model.Etudiant;
import model.Matiere;
import model.Note;
import model.Professeur;
import model.Reclamation;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

/**
 * Espace professeur : choix de l'école / classe / matière / semestre, saisie des notes,
 * calcul des moyennes, enregistrement, modification, suppression et consultation des réclamations.
 */
public class ProfesseurFrame extends JFrame {

    private static final String STATUT_ENREGISTRE = "Enregistré";
    private static final String STATUT_NON_ENREGISTRE = "Non enregistré";
    private static final String STATUT_MODIFIE = "Modifié";

    private static final int COL_MATRICULE = 0;
    private static final int COL_NOM = 1;
    private static final int COL_DEVOIR = 2;
    private static final int COL_EXAMEN = 3;
    private static final int COL_MOYENNE = 4;
    private static final int COL_STATUT = 5;

    private static final String PAGE_NOTES = "notes";
    private static final String PAGE_RECLAMATIONS = "reclamations";

    private final ContexteApplication ctx;
    private final Professeur professeur;

    // Filtres
    private final JComboBox<Ecole> cbEcole = new JComboBox<>();
    private final JComboBox<ClasseEtude> cbClasse = new JComboBox<>();
    private final JComboBox<Matiere> cbMatiere = new JComboBox<>();
    private final JComboBox<String> cbSemestre = new JComboBox<>(DonneesController.SEMESTRES);
    private final JTextField txtRecherche = new JTextField(14);
    private boolean miseAJour = false; // true pendant le remplissage des listes (évite les rechargements en cascade)
    private boolean enChargement = false; // true pendant le remplissage du tableau

    // Tableau des notes
    private final DefaultTableModel modeleNotes = new DefaultTableModel(
            new String[]{"Matricule", "Nom", "Devoir", "Examen", "Moyenne", "Statut"}, 0) {
        @Override
        public boolean isCellEditable(int ligne, int colonne) {
            return colonne == COL_DEVOIR || colonne == COL_EXAMEN;
        }

        @Override
        public Class<?> getColumnClass(int colonne) {
            return String.class;
        }
    };
    private final JTable tableNotes = new JTable(modeleNotes);
    private final TableRowSorter<DefaultTableModel> tri = new TableRowSorter<>(modeleNotes);
    private final JLabel lblStatut = new JLabel(" ");

    // Réclamations
    private final DefaultTableModel modeleReclamations = new DefaultTableModel(
            new String[]{"Date", "Matricule", "Étudiant", "Matière", "Semestre", "Statut"}, 0) {
        @Override
        public boolean isCellEditable(int ligne, int colonne) {
            return false;
        }
    };
    private final JTable tableReclamations = new JTable(modeleReclamations);
    private final JTextArea txtDetail = new JTextArea();
    private final JLabel lblDetailTitre = new JLabel("Sélectionnez une réclamation pour lire le message.");
    private List<Reclamation> reclamationsAffichees = new ArrayList<>();

    // Navigation
    private final JPanel contenu = new JPanel(new CardLayout());
    private final Theme.BoutonArrondi btnNavNotes = Theme.bouton("Gestion des notes", Theme.PRIMAIRE);
    private final Theme.BoutonArrondi btnNavReclamations = Theme.bouton("Réclamations reçues", Theme.INACTIF);

    public ProfesseurFrame(ContexteApplication ctx, Professeur professeur) {
        super("Gestion des notes - Espace professeur");
        this.ctx = ctx;
        this.professeur = professeur;

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

        initialiserListes();
        chargerReclamations();

        setMinimumSize(new Dimension(980, 640));
        setSize(1200, 720);
        setLocationRelativeTo(null);
    }

    // =====================================================================
    // Construction de l'interface
    // =====================================================================

    private JPanel construireEntete() {
        JButton btnDeconnexion = Theme.bouton("Déconnexion", Theme.DANGER);
        btnDeconnexion.addActionListener(e -> deconnecter());
        return Theme.entete("GESTION DES NOTES", "Espace professeur",
                Theme.photoRonde(Theme.chargerImage("professeur.png"), 46),
                "Prof. " + professeur.getNomComplet(), professeur.getSpecialite(), btnDeconnexion);
    }

    private JPanel construireCorps() {
        JPanel corps = new JPanel(new BorderLayout(0, 12));
        corps.setOpaque(false);
        corps.setBorder(new EmptyBorder(14, 20, 16, 20));

        JPanel navigation = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        navigation.setOpaque(false);
        btnNavNotes.addActionListener(e -> afficherPage(PAGE_NOTES));
        btnNavReclamations.addActionListener(e -> {
            chargerReclamations();
            afficherPage(PAGE_RECLAMATIONS);
        });
        navigation.add(btnNavNotes);
        navigation.add(btnNavReclamations);
        corps.add(navigation, BorderLayout.NORTH);

        contenu.setOpaque(false);
        contenu.add(construirePageNotes(), PAGE_NOTES);
        contenu.add(construirePageReclamations(), PAGE_RECLAMATIONS);
        corps.add(contenu, BorderLayout.CENTER);
        return corps;
    }

    private void afficherPage(String page) {
        ((CardLayout) contenu.getLayout()).show(contenu, page);
        btnNavNotes.setCouleur(page.equals(PAGE_NOTES) ? Theme.PRIMAIRE : Theme.INACTIF);
        btnNavReclamations.setCouleur(page.equals(PAGE_RECLAMATIONS) ? Theme.PRIMAIRE : Theme.INACTIF);
    }

    // ---- Page « Gestion des notes » ----

    private JPanel construirePageNotes() {
        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setOpaque(false);
        page.add(construireFiltres(), BorderLayout.NORTH);
        page.add(construireTableNotes(), BorderLayout.CENTER);
        page.add(construireBarreActions(), BorderLayout.SOUTH);
        return page;
    }

    private Theme.Carte construireFiltres() {
        Theme.Carte carte = new Theme.Carte(new GridBagLayout(), Color.WHITE, 16);
        carte.setBorder(new EmptyBorder(12, 18, 14, 18));

        Theme.styliserCombo(cbEcole);
        Theme.styliserCombo(cbClasse);
        Theme.styliserCombo(cbMatiere);
        Theme.styliserCombo(cbSemestre);
        cbEcole.setPreferredSize(new Dimension(270, 34));
        cbClasse.setPreferredSize(new Dimension(210, 34));
        cbMatiere.setPreferredSize(new Dimension(195, 34));
        cbSemestre.setPreferredSize(new Dimension(125, 34));

        ajouterFiltre(carte, 0, "École", cbEcole, 1.35);
        ajouterFiltre(carte, 1, "Classe", cbClasse, 1.05);
        ajouterFiltre(carte, 2, "Matière", cbMatiere, 0.98);
        ajouterFiltre(carte, 3, "Semestre", cbSemestre, 0.63);

        // Recherche d'un étudiant
        Theme.styliserChamp(txtRecherche);
        txtRecherche.setToolTipText("Nom, prénom ou matricule (Entrée pour chercher dans toutes les écoles)");
        JButton btnRechercher = Theme.bouton("Chercher", Theme.PRIMAIRE);
        btnRechercher.setBorder(new EmptyBorder(7, 14, 7, 14));
        JPanel recherche = new JPanel(new BorderLayout(6, 0));
        recherche.setOpaque(false);
        recherche.add(txtRecherche, BorderLayout.CENTER);
        recherche.add(btnRechercher, BorderLayout.EAST);
        ajouterFiltre(carte, 4, "Rechercher un étudiant", recherche, 1.6);

        txtRecherche.addActionListener(e -> rechercher());
        btnRechercher.addActionListener(e -> rechercher());
        txtRecherche.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                appliquerFiltre();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                appliquerFiltre();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                appliquerFiltre();
            }
        });
        return carte;
    }

    private void ajouterFiltre(JPanel carte, int colonne, String libelle, java.awt.Component composant, double poids) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = colonne;
        c.weightx = poids;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, colonne < 4 ? 14 : 0);
        c.gridy = 0;
        carte.add(Theme.etiquette(libelle, Theme.POLICE_GRAS, Theme.TEXTE), c);
        c.gridy = 1;
        c.insets = new Insets(4, 0, 0, colonne < 4 ? 14 : 0);
        carte.add(composant, c);
    }

    private Theme.Carte construireTableNotes() {
        Theme.styliserTable(tableNotes);
        tableNotes.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // valide la cellule si on clique ailleurs
        tableNotes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tableNotes.setDefaultRenderer(String.class,
                new Theme.RenduTable(COL_MOYENNE, COL_STATUT, COL_MATRICULE, COL_DEVOIR, COL_EXAMEN, COL_MOYENNE, COL_STATUT)
                        .validerColonnes(COL_DEVOIR, COL_EXAMEN));

        // Éditeur des colonnes Devoir / Examen : champ centré, un seul clic pour commencer à saisir
        JTextField champ = new JTextField();
        champ.setHorizontalAlignment(JTextField.CENTER);
        champ.setFont(Theme.POLICE);
        DefaultCellEditor editeur = new DefaultCellEditor(champ);
        editeur.setClickCountToStart(1);
        tableNotes.getColumnModel().getColumn(COL_DEVOIR).setCellEditor(editeur);
        tableNotes.getColumnModel().getColumn(COL_EXAMEN).setCellEditor(editeur);

        int[] largeurs = {110, 300, 110, 110, 110, 150};
        for (int i = 0; i < largeurs.length; i++) {
            tableNotes.getColumnModel().getColumn(i).setPreferredWidth(largeurs[i]);
        }

        // Tri possible seulement sur le matricule et le nom
        tri.setSortable(COL_DEVOIR, false);
        tri.setSortable(COL_EXAMEN, false);
        tri.setSortable(COL_MOYENNE, false);
        tri.setSortable(COL_STATUT, false);
        tableNotes.setRowSorter(tri);

        modeleNotes.addTableModelListener(this::surModificationTable);

        JScrollPane defilement = new JScrollPane(tableNotes);
        defilement.setBorder(null);
        defilement.getViewport().setBackground(Color.WHITE);

        Theme.Carte carte = new Theme.Carte(new BorderLayout(), Color.WHITE, 16);
        carte.setBorder(new EmptyBorder(8, 8, 8, 8));
        carte.add(defilement, BorderLayout.CENTER);
        return carte;
    }

    private JPanel construireBarreActions() {
        JPanel barre = new JPanel(new BorderLayout(0, 8));
        barre.setOpaque(false);

        lblStatut.setFont(Theme.POLICE);
        lblStatut.setForeground(Theme.TEXTE_CLAIR);
        barre.add(lblStatut, BorderLayout.NORTH);

        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        boutons.setOpaque(false);
        JButton btnCalculer = Theme.bouton("CALCULER", Theme.PRIMAIRE);
        JButton btnEnregistrer = Theme.bouton("ENREGISTRER", Theme.SUCCES);
        JButton btnModifier = Theme.bouton("MODIFIER", Theme.AVERTISSEMENT);
        JButton btnSupprimer = Theme.bouton("SUPPRIMER", Theme.DANGER);
        btnCalculer.setToolTipText("Calcule la moyenne (devoir + examen) / 2 de chaque ligne");
        btnEnregistrer.setToolTipText("Enregistre les nouvelles notes saisies");
        btnModifier.setToolTipText("Applique les changements aux lignes sélectionnées (ou à toutes les lignes modifiées)");
        btnSupprimer.setToolTipText("Supprime les notes des lignes sélectionnées");
        btnCalculer.addActionListener(e -> calculer());
        btnEnregistrer.addActionListener(e -> enregistrer());
        btnModifier.addActionListener(e -> modifier());
        btnSupprimer.addActionListener(e -> supprimer());
        boutons.add(btnCalculer);
        boutons.add(btnEnregistrer);
        boutons.add(btnModifier);
        boutons.add(btnSupprimer);
        barre.add(boutons, BorderLayout.CENTER);
        return barre;
    }

    // ---- Page « Réclamations reçues » ----

    private JPanel construirePageReclamations() {
        JPanel page = new JPanel(new GridLayout(2, 1, 0, 12));
        page.setOpaque(false);

        // Liste des réclamations
        Theme.styliserTable(tableReclamations);
        tableReclamations.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableReclamations.setDefaultRenderer(Object.class, new Theme.RenduTable(-1, 5, 0, 1, 4, 5));
        int[] largeurs = {140, 100, 220, 180, 100, 110};
        for (int i = 0; i < largeurs.length; i++) {
            tableReclamations.getColumnModel().getColumn(i).setPreferredWidth(largeurs[i]);
        }
        tableReclamations.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                afficherDetail();
            }
        });
        JScrollPane defilement = new JScrollPane(tableReclamations);
        defilement.setBorder(null);
        defilement.getViewport().setBackground(Color.WHITE);
        Theme.Carte carteListe = new Theme.Carte(new BorderLayout(), Color.WHITE, 16);
        carteListe.setBorder(new EmptyBorder(8, 8, 8, 8));
        carteListe.add(defilement, BorderLayout.CENTER);

        // Détail du message
        txtDetail.setEditable(false);
        txtDetail.setLineWrap(true);
        txtDetail.setWrapStyleWord(true);
        txtDetail.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 15));
        txtDetail.setForeground(Theme.TEXTE);
        txtDetail.setBorder(new EmptyBorder(8, 10, 8, 10));
        JScrollPane defDetail = new JScrollPane(txtDetail);
        defDetail.setBorder(javax.swing.BorderFactory.createLineBorder(Theme.BORDURE));

        lblDetailTitre.setFont(Theme.POLICE_GRAS);
        lblDetailTitre.setForeground(Theme.PRIMAIRE);

        JButton btnTraitee = Theme.bouton("Marquer comme traitée", Theme.SUCCES);
        JButton btnActualiser = Theme.bouton("Actualiser", Theme.PRIMAIRE);
        btnTraitee.addActionListener(e -> marquerTraitee());
        btnActualiser.addActionListener(e -> chargerReclamations());
        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        boutons.setOpaque(false);
        boutons.add(btnActualiser);
        boutons.add(btnTraitee);

        Theme.Carte carteDetail = new Theme.Carte(new BorderLayout(0, 8), Color.WHITE, 16);
        carteDetail.setBorder(new EmptyBorder(12, 14, 12, 14));
        carteDetail.add(lblDetailTitre, BorderLayout.NORTH);
        carteDetail.add(defDetail, BorderLayout.CENTER);
        carteDetail.add(boutons, BorderLayout.SOUTH);

        page.add(carteListe);
        page.add(carteDetail);
        return page;
    }

    // =====================================================================
    // Listes déroulantes en cascade : École -> Classe -> Matière
    // =====================================================================

    private void initialiserListes() {
        for (Ecole ecole : ctx.getDonnees().getEcoles()) {
            cbEcole.addItem(ecole);
        }
        remplirClasses(null);

        cbEcole.addActionListener(e -> {
            if (!miseAJour) {
                remplirClasses(null);
            }
        });
        cbClasse.addActionListener(e -> {
            if (!miseAJour) {
                remplirMatieres();
            }
        });
        cbMatiere.addActionListener(e -> {
            if (!miseAJour) {
                chargerTable();
            }
        });
        cbSemestre.addActionListener(e -> {
            if (!miseAJour) {
                chargerTable();
            }
        });
    }

    private void remplirClasses(ClasseEtude preferee) {
        Ecole ecole = (Ecole) cbEcole.getSelectedItem();
        miseAJour = true;
        cbClasse.removeAllItems();
        if (ecole != null) {
            for (ClasseEtude classe : ecole.getClasses()) {
                cbClasse.addItem(classe);
            }
        }
        if (preferee != null) {
            cbClasse.setSelectedItem(preferee);
        }
        miseAJour = false;
        remplirMatieres();
    }

    private void remplirMatieres() {
        ClasseEtude classe = (ClasseEtude) cbClasse.getSelectedItem();
        miseAJour = true;
        cbMatiere.removeAllItems();
        if (classe != null) {
            for (Matiere matiere : classe.getMatieres()) {
                cbMatiere.addItem(matiere);
            }
        }
        miseAJour = false;
        chargerTable();
    }

    private void selectionnerClasse(ClasseEtude classe) {
        miseAJour = true;
        cbEcole.setSelectedItem(classe.getEcole());
        miseAJour = false;
        remplirClasses(classe);
    }

    /** Remplit le tableau avec les étudiants de la classe et les notes déjà enregistrées. */
    private void chargerTable() {
        arreterEdition();
        enChargement = true;
        modeleNotes.setRowCount(0);
        ClasseEtude classe = (ClasseEtude) cbClasse.getSelectedItem();
        Matiere matiere = (Matiere) cbMatiere.getSelectedItem();
        String semestre = (String) cbSemestre.getSelectedItem();
        int enregistrees = 0;
        if (classe != null && matiere != null) {
            for (Etudiant etudiant : classe.getEtudiants()) {
                Note note = ctx.getNotes().trouverNote(etudiant.getMatricule(), matiere.getNom(), semestre);
                if (note != null) {
                    enregistrees++;
                    modeleNotes.addRow(new Object[]{etudiant.getMatricule(), etudiant.getNomComplet(),
                            Note.formater(note.getDevoir()), Note.formater(note.getExamen()),
                            Note.formater(note.getMoyenne()), STATUT_ENREGISTRE});
                } else {
                    modeleNotes.addRow(new Object[]{etudiant.getMatricule(), etudiant.getNomComplet(),
                            "", "", "", STATUT_NON_ENREGISTRE});
                }
            }
            message(modeleNotes.getRowCount() + " étudiant(s) - " + enregistrees + " note(s) enregistrée(s) pour "
                    + matiere.getNom() + " (" + semestre + ")", Theme.TEXTE_CLAIR);
        } else {
            message("Aucune matière à afficher.", Theme.TEXTE_CLAIR);
        }
        enChargement = false;
        appliquerFiltre();
    }

    // =====================================================================
    // Recherche d'un étudiant
    // =====================================================================

    /** Filtre en direct les lignes du tableau (nom, prénom ou matricule, sans tenir compte des accents). */
    private void appliquerFiltre() {
        final String terme = DonneesController.normaliser(txtRecherche.getText());
        if (terme.isEmpty()) {
            tri.setRowFilter(null);
            return;
        }
        tri.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entree) {
                String texte = entree.getStringValue(COL_MATRICULE) + " " + entree.getStringValue(COL_NOM);
                return DonneesController.normaliser(texte).contains(terme);
            }
        });
    }

    /** Si l'étudiant n'est pas dans la classe affichée, on le cherche dans toutes les écoles. */
    private void rechercher() {
        String terme = txtRecherche.getText().trim();
        if (terme.isEmpty()) {
            message("Saisissez un nom ou un matricule à rechercher.", Theme.TEXTE_CLAIR);
            return;
        }
        appliquerFiltre();
        if (tableNotes.getRowCount() > 0) {
            tableNotes.setRowSelectionInterval(0, 0);
            message(tableNotes.getRowCount() + " étudiant(s) trouvé(s) dans cette classe.", Theme.SUCCES);
            return;
        }
        List<Etudiant> resultats = ctx.getDonnees().rechercherEtudiants(terme);
        if (resultats.isEmpty()) {
            message("Aucun étudiant trouvé pour « " + terme + " ».", Theme.DANGER);
            return;
        }
        Etudiant trouve = resultats.get(0);
        selectionnerClasse(trouve.getClasse());
        appliquerFiltre();
        if (tableNotes.getRowCount() > 0) {
            tableNotes.setRowSelectionInterval(0, 0);
        }
        message(trouve.getNomComplet() + " trouvé : " + trouve.getClasse().getEcole().getNom()
                + " / " + trouve.getClasse().getNom(), Theme.SUCCES);
    }

    // =====================================================================
    // Actions sur les notes
    // =====================================================================

    /** Ligne du tableau lue et validée. */
    private static class LigneSaisie {
        static final int VIDE = 0;
        static final int VALIDE = 1;
        static final int INVALIDE = 2;
        int etat;
        double devoir;
        double examen;
    }

    private String texte(int ligne, int colonne) {
        Object valeur = modeleNotes.getValueAt(ligne, colonne);
        return valeur == null ? "" : valeur.toString();
    }

    private LigneSaisie lire(int ligne) {
        LigneSaisie resultat = new LigneSaisie();
        String saisieDevoir = texte(ligne, COL_DEVOIR);
        String saisieExamen = texte(ligne, COL_EXAMEN);
        if (saisieDevoir.trim().isEmpty() && saisieExamen.trim().isEmpty()) {
            resultat.etat = LigneSaisie.VIDE;
            return resultat;
        }
        Double devoir = Note.parser(saisieDevoir);
        Double examen = Note.parser(saisieExamen);
        if (devoir == null || examen == null || !Note.estValide(devoir) || !Note.estValide(examen)) {
            resultat.etat = LigneSaisie.INVALIDE;
            return resultat;
        }
        resultat.etat = LigneSaisie.VALIDE;
        resultat.devoir = devoir;
        resultat.examen = examen;
        return resultat;
    }

    /** CALCULER : moyenne = (devoir + examen) / 2 pour chaque ligne complète. */
    private void calculer() {
        arreterEdition();
        int calculees = 0;
        int invalides = 0;
        for (int i = 0; i < modeleNotes.getRowCount(); i++) {
            LigneSaisie ligne = lire(i);
            if (ligne.etat == LigneSaisie.VALIDE) {
                modeleNotes.setValueAt(Note.formater(Note.calculerMoyenne(ligne.devoir, ligne.examen)), i, COL_MOYENNE);
                calculees++;
            } else {
                modeleNotes.setValueAt("", i, COL_MOYENNE);
                if (ligne.etat == LigneSaisie.INVALIDE) {
                    invalides++;
                }
            }
        }
        if (calculees == 0 && invalides == 0) {
            message("Saisissez d'abord des notes de devoir et d'examen.", Theme.AVERTISSEMENT);
        } else if (invalides > 0) {
            message(calculees + " moyenne(s) calculée(s) - " + invalides
                    + " ligne(s) incomplète(s) ou invalide(s) (notes entre 0 et 20).", Theme.AVERTISSEMENT);
        } else {
            message(calculees + " moyenne(s) calculée(s).", Theme.SUCCES);
        }
    }

    /** ENREGISTRER : crée les nouvelles notes saisies. */
    private void enregistrer() {
        arreterEdition();
        Matiere matiere = (Matiere) cbMatiere.getSelectedItem();
        String semestre = (String) cbSemestre.getSelectedItem();
        if (matiere == null) {
            message("Aucune matière sélectionnée.", Theme.DANGER);
            return;
        }
        int enregistrees = 0;
        int aModifier = 0;
        int invalides = 0;
        for (int i = 0; i < modeleNotes.getRowCount(); i++) {
            LigneSaisie ligne = lire(i);
            if (ligne.etat == LigneSaisie.VIDE) {
                continue;
            }
            if (ligne.etat == LigneSaisie.INVALIDE) {
                invalides++;
                continue;
            }
            String matricule = texte(i, COL_MATRICULE);
            if (ctx.getNotes().trouverNote(matricule, matiere.getNom(), semestre) != null) {
                if (STATUT_MODIFIE.equals(texte(i, COL_STATUT))) {
                    aModifier++; // déjà enregistrée : il faut passer par MODIFIER
                }
                continue;
            }
            ctx.getNotes().ajouterNote(new Note(matricule, matiere.getNom(), semestre, ligne.devoir, ligne.examen));
            mettreAJourLigne(i, ligne.devoir, ligne.examen);
            enregistrees++;
        }
        boolean sauvegardeOk = enregistrees == 0 || ctx.getNotes().sauvegarder();
        terminerAction(enregistrees, "enregistrée(s)", aModifier, invalides, sauvegardeOk,
                "Aucune nouvelle note à enregistrer.");
    }

    /** MODIFIER : met à jour les notes déjà enregistrées (lignes sélectionnées, sinon lignes modifiées). */
    private void modifier() {
        arreterEdition();
        Matiere matiere = (Matiere) cbMatiere.getSelectedItem();
        String semestre = (String) cbSemestre.getSelectedItem();
        if (matiere == null) {
            message("Aucune matière sélectionnée.", Theme.DANGER);
            return;
        }
        List<Integer> lignes = lignesSelectionnees();
        if (lignes.isEmpty()) {
            for (int i = 0; i < modeleNotes.getRowCount(); i++) {
                if (STATUT_MODIFIE.equals(texte(i, COL_STATUT))) {
                    lignes.add(i);
                }
            }
        }
        if (lignes.isEmpty()) {
            message("Rien à modifier : changez une note enregistrée, puis cliquez sur MODIFIER.", Theme.AVERTISSEMENT);
            return;
        }
        int modifiees = 0;
        int nonEnregistrees = 0;
        int invalides = 0;
        for (int i : lignes) {
            LigneSaisie ligne = lire(i);
            if (ligne.etat != LigneSaisie.VALIDE) {
                invalides++;
                continue;
            }
            String matricule = texte(i, COL_MATRICULE);
            if (ctx.getNotes().modifierNote(matricule, matiere.getNom(), semestre, ligne.devoir, ligne.examen)) {
                mettreAJourLigne(i, ligne.devoir, ligne.examen);
                modifiees++;
            } else {
                nonEnregistrees++; // pas encore enregistrée : il faut passer par ENREGISTRER
            }
        }
        boolean sauvegardeOk = modifiees == 0 || ctx.getNotes().sauvegarder();
        StringBuilder sb = new StringBuilder();
        if (modifiees > 0) {
            sb.append(modifiees).append(" note(s) modifiée(s). ");
        }
        if (nonEnregistrees > 0) {
            sb.append(nonEnregistrees).append(" ligne(s) pas encore enregistrée(s) : utilisez ENREGISTRER. ");
        }
        if (invalides > 0) {
            sb.append(invalides).append(" ligne(s) vide(s) ou invalide(s) ignorée(s) (notes entre 0 et 20). ");
        }
        afficherResultat(sb, modifiees > 0 && nonEnregistrees == 0 && invalides == 0, sauvegardeOk);
    }

    /** SUPPRIMER : supprime les notes des lignes sélectionnées (après confirmation). */
    private void supprimer() {
        arreterEdition();
        Matiere matiere = (Matiere) cbMatiere.getSelectedItem();
        String semestre = (String) cbSemestre.getSelectedItem();
        List<Integer> lignes = lignesSelectionnees();
        if (matiere == null || lignes.isEmpty()) {
            message("Sélectionnez une ou plusieurs lignes à supprimer.", Theme.AVERTISSEMENT);
            return;
        }
        int aSupprimer = 0;
        for (int i : lignes) {
            if (ctx.getNotes().trouverNote(texte(i, COL_MATRICULE), matiere.getNom(), semestre) != null) {
                aSupprimer++;
            }
        }
        if (aSupprimer > 0) {
            int choix = JOptionPane.showConfirmDialog(this,
                    "Supprimer définitivement " + aSupprimer + " note(s) de « " + matiere.getNom()
                            + " » (" + semestre + ") ?",
                    "Confirmation de suppression", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choix != JOptionPane.YES_OPTION) {
                return;
            }
        }
        int supprimees = 0;
        for (int i : lignes) {
            if (ctx.getNotes().supprimerNote(texte(i, COL_MATRICULE), matiere.getNom(), semestre)) {
                supprimees++;
            }
            modeleNotes.setValueAt("", i, COL_DEVOIR);
            modeleNotes.setValueAt("", i, COL_EXAMEN);
            modeleNotes.setValueAt("", i, COL_MOYENNE);
            modeleNotes.setValueAt(STATUT_NON_ENREGISTRE, i, COL_STATUT);
        }
        boolean sauvegardeOk = supprimees == 0 || ctx.getNotes().sauvegarder();
        if (!sauvegardeOk) {
            erreurSauvegarde();
        }
        message(supprimees + " note(s) supprimée(s), " + (lignes.size() - supprimees)
                + " saisie(s) effacée(s).", Theme.SUCCES);
    }

    private void terminerAction(int reussies, String verbe, int aModifier, int invalides,
                                boolean sauvegardeOk, String messageRien) {
        StringBuilder sb = new StringBuilder();
        if (reussies > 0) {
            sb.append(reussies).append(" note(s) ").append(verbe).append(". ");
        }
        if (aModifier > 0) {
            sb.append(aModifier).append(" ligne(s) déjà enregistrée(s) : utilisez MODIFIER. ");
        }
        if (invalides > 0) {
            sb.append(invalides).append(" ligne(s) incomplète(s) ou invalide(s) ignorée(s) (notes entre 0 et 20). ");
        }
        if (sb.length() == 0) {
            message(messageRien, Theme.AVERTISSEMENT);
            return;
        }
        afficherResultat(sb, reussies > 0 && aModifier == 0 && invalides == 0, sauvegardeOk);
    }

    private void afficherResultat(StringBuilder texte, boolean toutOk, boolean sauvegardeOk) {
        if (!sauvegardeOk) {
            erreurSauvegarde();
        }
        message(texte.toString().trim(), toutOk ? Theme.SUCCES : Theme.AVERTISSEMENT);
    }

    private void erreurSauvegarde() {
        JOptionPane.showMessageDialog(this,
                "Les notes n'ont pas pu être écrites dans le fichier data/notes.txt.\n"
                        + "Vérifiez que le dossier est accessible en écriture.",
                "Erreur de sauvegarde", JOptionPane.ERROR_MESSAGE);
    }

    /** Affiche la moyenne et le statut « Enregistré » sur une ligne. */
    private void mettreAJourLigne(int ligne, double devoir, double examen) {
        modeleNotes.setValueAt(Note.formater(devoir), ligne, COL_DEVOIR);
        modeleNotes.setValueAt(Note.formater(examen), ligne, COL_EXAMEN);
        modeleNotes.setValueAt(Note.formater(Note.calculerMoyenne(devoir, examen)), ligne, COL_MOYENNE);
        modeleNotes.setValueAt(STATUT_ENREGISTRE, ligne, COL_STATUT);
    }

    /** Lignes sélectionnées, converties en indices du modèle (le tableau peut être trié / filtré). */
    private List<Integer> lignesSelectionnees() {
        List<Integer> resultat = new ArrayList<>();
        for (int vue : tableNotes.getSelectedRows()) {
            resultat.add(tableNotes.convertRowIndexToModel(vue));
        }
        return resultat;
    }

    private void arreterEdition() {
        if (tableNotes.isEditing()) {
            tableNotes.getCellEditor().stopCellEditing();
        }
    }

    /**
     * Quand une note est modifiée à la main : la moyenne est effacée (à recalculer)
     * et le statut passe à « Modifié » si la valeur diffère de celle enregistrée.
     */
    private void surModificationTable(TableModelEvent e) {
        if (enChargement || e.getType() != TableModelEvent.UPDATE) {
            return;
        }
        if (e.getColumn() != COL_DEVOIR && e.getColumn() != COL_EXAMEN) {
            return;
        }
        Matiere matiere = (Matiere) cbMatiere.getSelectedItem();
        String semestre = (String) cbSemestre.getSelectedItem();
        for (int i = e.getFirstRow(); i <= e.getLastRow() && i < modeleNotes.getRowCount(); i++) {
            Note enregistree = matiere == null ? null
                    : ctx.getNotes().trouverNote(texte(i, COL_MATRICULE), matiere.getNom(), semestre);
            LigneSaisie ligne = lire(i);
            boolean identique = enregistree != null && ligne.etat == LigneSaisie.VALIDE
                    && Math.abs(ligne.devoir - enregistree.getDevoir()) < 0.005
                    && Math.abs(ligne.examen - enregistree.getExamen()) < 0.005;
            if (enregistree == null) {
                modeleNotes.setValueAt("", i, COL_MOYENNE);
                modeleNotes.setValueAt(STATUT_NON_ENREGISTRE, i, COL_STATUT);
            } else if (identique) {
                modeleNotes.setValueAt(Note.formater(enregistree.getMoyenne()), i, COL_MOYENNE);
                modeleNotes.setValueAt(STATUT_ENREGISTRE, i, COL_STATUT);
            } else {
                modeleNotes.setValueAt("", i, COL_MOYENNE);
                modeleNotes.setValueAt(STATUT_MODIFIE, i, COL_STATUT);
            }
        }
    }

    private void message(String texte, Color couleur) {
        lblStatut.setForeground(couleur);
        lblStatut.setText(texte);
    }

    // =====================================================================
    // Réclamations
    // =====================================================================

    private void chargerReclamations() {
        int idSelectionne = -1;
        int ligneSelectionnee = tableReclamations.getSelectedRow();
        if (ligneSelectionnee >= 0 && ligneSelectionnee < reclamationsAffichees.size()) {
            idSelectionne = reclamationsAffichees.get(ligneSelectionnee).getId();
        }
        reclamationsAffichees = ctx.getReclamations().toutes();
        modeleReclamations.setRowCount(0);
        int aSelectionner = -1;
        for (int i = 0; i < reclamationsAffichees.size(); i++) {
            Reclamation r = reclamationsAffichees.get(i);
            modeleReclamations.addRow(new Object[]{r.getDateFormatee(), r.getMatricule(), r.getNomEtudiant(),
                    r.getMatiere(), r.getSemestre(), r.getStatut().getLibelle()});
            if (r.getId() == idSelectionne) {
                aSelectionner = i;
            }
        }
        if (aSelectionner >= 0) {
            tableReclamations.setRowSelectionInterval(aSelectionner, aSelectionner);
        }
        afficherDetail();

        int enAttente = ctx.getReclamations().nombreEnAttente();
        btnNavReclamations.setText(enAttente > 0 ? "Réclamations reçues (" + enAttente + ")" : "Réclamations reçues");
    }

    private void afficherDetail() {
        int ligne = tableReclamations.getSelectedRow();
        if (ligne < 0 || ligne >= reclamationsAffichees.size()) {
            lblDetailTitre.setText(reclamationsAffichees.isEmpty()
                    ? "Aucune réclamation reçue pour le moment."
                    : "Sélectionnez une réclamation pour lire le message.");
            txtDetail.setText("");
            return;
        }
        Reclamation r = reclamationsAffichees.get(ligne);
        lblDetailTitre.setText(r.getNomEtudiant() + " (" + r.getMatricule() + ") - " + r.getMatiere()
                + ", " + r.getSemestre() + " - " + r.getDateFormatee());
        txtDetail.setText(r.getMessage());
        txtDetail.setCaretPosition(0);
    }

    private void marquerTraitee() {
        int ligne = tableReclamations.getSelectedRow();
        if (ligne < 0 || ligne >= reclamationsAffichees.size()) {
            JOptionPane.showMessageDialog(this, "Sélectionnez d'abord une réclamation.",
                    "Réclamations", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        ctx.getReclamations().marquerTraitee(reclamationsAffichees.get(ligne).getId());
        chargerReclamations();
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
