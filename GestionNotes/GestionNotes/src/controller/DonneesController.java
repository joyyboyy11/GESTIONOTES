package controller;

import model.ClasseEtude;
import model.Ecole;
import model.Etudiant;
import model.Matiere;
import model.Professeur;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Contient les données de référence : écoles, classes, matières, étudiants et professeur.
 * Gère aussi la recherche d'étudiants et les photos de profil.
 */
public class DonneesController {

    public static final String[] SEMESTRES = {"Semestre 1", "Semestre 2"};

    private static final String MOT_DE_PASSE = "1234";

    private final List<Ecole> ecoles = new ArrayList<>();
    private final Professeur professeur;
    private final Properties photos = new Properties();

    public DonneesController() {
        professeur = new Professeur("Diallo", "Ibrahima", "prof", MOT_DE_PASSE, "Informatique");
        initialiserDonnees();
        chargerPhotos();
    }

    // ------------------------------------------------------------------
    // Données initiales
    // ------------------------------------------------------------------

    private void initialiserDonnees() {
        // --- École 1 : informatique ---
        Ecole isi = new Ecole("Institut Supérieur d'Informatique", "Thiès");

        ClasseEtude l2 = new ClasseEtude("Licence 2 Informatique");
        ajouterMatieres(l2, "Java", "Base de données", "Réseaux", "Programmation");
        ajouterEtudiants(l2,
                new String[]{"ET001", "Ndiaye", "Awa"},
                new String[]{"ET002", "Diop", "Moussa"},
                new String[]{"ET003", "Sow", "Fatou"},
                new String[]{"ET004", "Fall", "Cheikh"},
                new String[]{"ET005", "Ba", "Aminata"},
                new String[]{"ET006", "Sarr", "Ibrahima"});
        isi.ajouterClasse(l2);

        ClasseEtude l3 = new ClasseEtude("Licence 3 Informatique");
        ajouterMatieres(l3, "Génie logiciel", "Sécurité informatique", "Intelligence artificielle", "Projet tutoré");
        ajouterEtudiants(l3,
                new String[]{"ET011", "Ndoye", "Mariama"},
                new String[]{"ET012", "Thiam", "Abdoulaye"},
                new String[]{"ET013", "Sy", "Khadija"});
        isi.ajouterClasse(l3);
        ecoles.add(isi);

        // --- École 2 : gestion ---
        Ecole esg = new Ecole("École Supérieure de Gestion", "Dakar");
        ClasseEtude g1 = new ClasseEtude("Licence 1 Gestion");
        ajouterMatieres(g1, "Comptabilité", "Marketing", "Économie", "Statistiques");
        ajouterEtudiants(g1,
                new String[]{"ET101", "Faye", "Khady"},
                new String[]{"ET102", "Mbaye", "Pape"},
                new String[]{"ET103", "Diagne", "Ndeye"});
        esg.ajouterClasse(g1);
        ecoles.add(esg);

        // --- École 3 : lycée ---
        Ecole lycee = new Ecole("Lycée Excellence", "Thiès");
        ClasseEtude tle = new ClasseEtude("Terminale S2");
        ajouterMatieres(tle, "Mathématiques", "Physique-Chimie", "SVT", "Français");
        ajouterEtudiants(tle,
                new String[]{"ET201", "Cissé", "Aliou"},
                new String[]{"ET202", "Seck", "Rokhaya"},
                new String[]{"ET203", "Camara", "Modou"});
        lycee.ajouterClasse(tle);
        ecoles.add(lycee);
    }

    private void ajouterMatieres(ClasseEtude classe, String... noms) {
        for (String nom : noms) {
            classe.ajouterMatiere(new Matiere(nom));
        }
    }

    /** Chaque tableau = {matricule, nom, prénom}. Login : « etu » pour ET001, sinon le matricule en minuscules. */
    private void ajouterEtudiants(ClasseEtude classe, String[]... donnees) {
        for (String[] d : donnees) {
            String login = d[0].equals("ET001") ? "etu" : d[0].toLowerCase(Locale.ROOT);
            classe.ajouterEtudiant(new Etudiant(d[0], d[1], d[2], login, MOT_DE_PASSE));
        }
    }

    // ------------------------------------------------------------------
    // Accès aux données
    // ------------------------------------------------------------------

    public Professeur getProfesseur() {
        return professeur;
    }

    public List<Ecole> getEcoles() {
        return ecoles;
    }

    public List<Etudiant> getTousLesEtudiants() {
        List<Etudiant> tous = new ArrayList<>();
        for (Ecole ecole : ecoles) {
            for (ClasseEtude classe : ecole.getClasses()) {
                tous.addAll(classe.getEtudiants());
            }
        }
        return tous;
    }

    public Etudiant trouverEtudiant(String matricule) {
        for (Etudiant e : getTousLesEtudiants()) {
            if (e.getMatricule().equalsIgnoreCase(matricule)) {
                return e;
            }
        }
        return null;
    }

    /** Recherche par matricule ou nom, sans tenir compte des majuscules ni des accents. */
    public List<Etudiant> rechercherEtudiants(String terme) {
        List<Etudiant> resultat = new ArrayList<>();
        String t = normaliser(terme);
        if (t.isEmpty()) {
            return resultat;
        }
        for (Etudiant e : getTousLesEtudiants()) {
            if (normaliser(e.getMatricule() + " " + e.getNomComplet()).contains(t)) {
                resultat.add(e);
            }
        }
        return resultat;
    }

    /** Minuscules + suppression des accents + espaces superflus supprimés. */
    public static String normaliser(String texte) {
        if (texte == null) {
            return "";
        }
        String sansAccents = Normalizer.normalize(texte, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return sansAccents.toLowerCase(Locale.ROOT).trim();
    }

    // ------------------------------------------------------------------
    // Photos de profil
    // ------------------------------------------------------------------

    private File fichierPhotos() {
        return Stockage.fichier("photos.properties");
    }

    private void chargerPhotos() {
        File f = fichierPhotos();
        if (!f.exists()) {
            return;
        }
        try (Reader lecteur = Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8)) {
            photos.load(lecteur);
        } catch (IOException ex) {
            System.err.println("Lecture des photos impossible : " + ex.getMessage());
            return;
        }
        for (Etudiant e : getTousLesEtudiants()) {
            String relatif = photos.getProperty(e.getMatricule());
            if (relatif != null) {
                File photo = Stockage.fichier(relatif);
                if (photo.exists()) {
                    e.setCheminPhoto(photo.getPath());
                }
            }
        }
    }

    /**
     * Définit la photo de profil d'un étudiant à partir d'une image choisie par l'utilisateur.
     * L'image est copiée (en PNG) dans data/photos/.
     *
     * @return true si l'image est valide et a bien été enregistrée
     */
    public boolean definirPhoto(Etudiant etudiant, File source) {
        try {
            BufferedImage image = ImageIO.read(source);
            if (image == null) {
                return false;
            }
            String relatif = "photos/" + etudiant.getMatricule() + ".png";
            File destination = Stockage.fichier(relatif);
            File dossier = destination.getAbsoluteFile().getParentFile();
            if (!dossier.exists() && !dossier.mkdirs()) {
                return false;
            }
            if (!ImageIO.write(image, "png", destination)) {
                return false;
            }
            photos.setProperty(etudiant.getMatricule(), relatif);
            try (Writer ecrivain = Files.newBufferedWriter(fichierPhotos().toPath(), StandardCharsets.UTF_8)) {
                photos.store(ecrivain, "Photos de profil des étudiants");
            }
            etudiant.setCheminPhoto(destination.getPath());
            return true;
        } catch (IOException ex) {
            System.err.println("Enregistrement de la photo impossible : " + ex.getMessage());
            return false;
        }
    }
}
