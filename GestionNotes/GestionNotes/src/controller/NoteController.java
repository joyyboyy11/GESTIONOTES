package controller;

import model.Note;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Gère les notes : ajout, modification, suppression, consultation, calcul des moyennes
 * et sauvegarde dans le fichier texte  data/notes.txt.
 * Les méthodes d'ajout / modification / suppression ne sauvegardent pas toutes seules :
 * appeler sauvegarder() une fois les changements effectués.
 */
public class NoteController {

    private final List<Note> notes = new ArrayList<>();
    private final File fichier = Stockage.fichier("notes.txt");

    public NoteController() {
        if (fichier.exists()) {
            charger();
        } else {
            genererNotesDemonstration();
            sauvegarder();
        }
    }

    // ------------------------------------------------------------------
    // Consultation
    // ------------------------------------------------------------------

    public Note trouverNote(String matricule, String matiere, String semestre) {
        for (Note n : notes) {
            if (n.getMatricule().equalsIgnoreCase(matricule)
                    && n.getMatiere().equals(matiere)
                    && n.getSemestre().equals(semestre)) {
                return n;
            }
        }
        return null;
    }

    /** Toutes les notes d'un étudiant pour un semestre. */
    public List<Note> notesDeEtudiant(String matricule, String semestre) {
        List<Note> resultat = new ArrayList<>();
        for (Note n : notes) {
            if (n.getMatricule().equalsIgnoreCase(matricule) && n.getSemestre().equals(semestre)) {
                resultat.add(n);
            }
        }
        return resultat;
    }

    /**
     * Moyenne générale = somme des moyennes des matières / nombre de matières notées.
     *
     * @return null si l'étudiant n'a encore aucune note pour ce semestre
     */
    public Double moyenneGenerale(String matricule, String semestre) {
        List<Note> notesEtudiant = notesDeEtudiant(matricule, semestre);
        if (notesEtudiant.isEmpty()) {
            return null;
        }
        double somme = 0;
        for (Note n : notesEtudiant) {
            somme += n.getMoyenne();
        }
        return somme / notesEtudiant.size();
    }

    public int nombreNotes() {
        return notes.size();
    }

    // ------------------------------------------------------------------
    // Modification
    // ------------------------------------------------------------------

    /** Ajoute une note. Retourne false si une note existe déjà pour ce triplet étudiant/matière/semestre. */
    public boolean ajouterNote(Note note) {
        if (trouverNote(note.getMatricule(), note.getMatiere(), note.getSemestre()) != null) {
            return false;
        }
        notes.add(note);
        return true;
    }

    /** Modifie une note existante. Retourne false si elle n'existe pas. */
    public boolean modifierNote(String matricule, String matiere, String semestre, double devoir, double examen) {
        Note note = trouverNote(matricule, matiere, semestre);
        if (note == null) {
            return false;
        }
        note.setDevoir(devoir);
        note.setExamen(examen);
        return true;
    }

    /** Supprime une note. Retourne false si elle n'existe pas. */
    public boolean supprimerNote(String matricule, String matiere, String semestre) {
        Note note = trouverNote(matricule, matiere, semestre);
        return note != null && notes.remove(note);
    }

    // ------------------------------------------------------------------
    // Fichier texte
    // ------------------------------------------------------------------

    /** Format d'une ligne :  matricule;matiere;semestre;devoir;examen */
    public boolean sauvegarder() {
        List<String> lignes = new ArrayList<>();
        lignes.add("# matricule;matiere;semestre;devoir;examen");
        for (Note n : notes) {
            lignes.add(String.format(Locale.ROOT, "%s;%s;%s;%.2f;%.2f",
                    n.getMatricule(), n.getMatiere(), n.getSemestre(), n.getDevoir(), n.getExamen()));
        }
        try {
            Stockage.ecrire(fichier, lignes);
            return true;
        } catch (IOException ex) {
            System.err.println("Sauvegarde des notes impossible : " + ex.getMessage());
            return false;
        }
    }

    private void charger() {
        try {
            for (String ligne : Stockage.lire(fichier)) {
                if (ligne.trim().isEmpty() || ligne.startsWith("#")) {
                    continue;
                }
                String[] p = ligne.split(";");
                if (p.length != 5) {
                    continue;
                }
                try {
                    notes.add(new Note(p[0], p[1], p[2], Double.parseDouble(p[3]), Double.parseDouble(p[4])));
                } catch (NumberFormatException ignoree) {
                    // ligne mal formée : on l'ignore
                }
            }
        } catch (IOException ex) {
            System.err.println("Lecture des notes impossible : " + ex.getMessage());
        }
    }

    /** Quelques notes de départ pour que l'espace étudiant ne soit pas vide au premier lancement. */
    private void genererNotesDemonstration() {
        String s1 = "Semestre 1";
        ajouterNote(new Note("ET001", "Java", s1, 14, 16));
        ajouterNote(new Note("ET001", "Base de données", s1, 12, 13.5));
        ajouterNote(new Note("ET001", "Réseaux", s1, 9, 11));
        ajouterNote(new Note("ET001", "Programmation", s1, 15, 17));
        ajouterNote(new Note("ET002", "Java", s1, 10, 12));
        ajouterNote(new Note("ET002", "Base de données", s1, 8, 9.5));
        ajouterNote(new Note("ET002", "Réseaux", s1, 13, 14));
        ajouterNote(new Note("ET003", "Java", s1, 16, 15));
    }
}
