package model;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Les notes d'un étudiant dans une matière pour un semestre :
 * une note de devoir et une note d'examen.
 */
public class Note {

    public static final double NOTE_MIN = 0;
    public static final double NOTE_MAX = 20;

    private static final Pattern FORMAT_NOTE = Pattern.compile("\\d{1,2}([.,]\\d{1,2})?");

    private final String matricule;
    private final String matiere;
    private final String semestre;
    private double devoir;
    private double examen;

    public Note(String matricule, String matiere, String semestre, double devoir, double examen) {
        this.matricule = matricule;
        this.matiere = matiere;
        this.semestre = semestre;
        this.devoir = devoir;
        this.examen = examen;
    }

    public String getMatricule() {
        return matricule;
    }

    public String getMatiere() {
        return matiere;
    }

    public String getSemestre() {
        return semestre;
    }

    public double getDevoir() {
        return devoir;
    }

    public void setDevoir(double devoir) {
        this.devoir = devoir;
    }

    public double getExamen() {
        return examen;
    }

    public void setExamen(double examen) {
        this.examen = examen;
    }

    /** Moyenne de la matière = (devoir + examen) / 2. */
    public double getMoyenne() {
        return calculerMoyenne(devoir, examen);
    }

    // ------------------------------------------------------------------
    // Utilitaires (calcul, validation, conversion texte <-> nombre)
    // ------------------------------------------------------------------

    public static double calculerMoyenne(double devoir, double examen) {
        return (devoir + examen) / 2.0;
    }

    public static boolean estValide(double valeur) {
        return valeur >= NOTE_MIN && valeur <= NOTE_MAX;
    }

    /**
     * Convertit un texte saisi (« 12 », « 12,5 » ou « 12.5 ») en nombre.
     * Retourne null si le texte est vide ou mal formé.
     */
    public static Double parser(String texte) {
        if (texte == null) {
            return null;
        }
        String t = texte.trim();
        if (!FORMAT_NOTE.matcher(t).matches()) {
            return null;
        }
        return Double.valueOf(t.replace(',', '.'));
    }

    /** Affichage à la française avec 2 décimales : 12,50. */
    public static String formater(double valeur) {
        return String.format(Locale.FRANCE, "%.2f", valeur);
    }
}
