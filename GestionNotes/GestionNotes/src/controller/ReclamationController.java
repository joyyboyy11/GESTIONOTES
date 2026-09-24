package controller;

import model.Etudiant;
import model.Reclamation;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Gère les réclamations : envoi par les étudiants, consultation par le professeur,
 * et sauvegarde dans le fichier texte  data/reclamations.txt.
 */
public class ReclamationController {

    private final List<Reclamation> reclamations = new ArrayList<>();
    private final File fichier = Stockage.fichier("reclamations.txt");

    public ReclamationController() {
        charger();
    }

    /** Enregistre une nouvelle réclamation (statut « En attente ») et sauvegarde le fichier. */
    public Reclamation envoyer(Etudiant etudiant, String matiere, String semestre, String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Le message de la réclamation est vide.");
        }
        int id = 1;
        for (Reclamation r : reclamations) {
            id = Math.max(id, r.getId() + 1);
        }
        Reclamation r = new Reclamation(id, etudiant.getMatricule(), etudiant.getNomComplet(), matiere,
                semestre, message.trim(), LocalDateTime.now().withNano(0), Reclamation.Statut.EN_ATTENTE);
        reclamations.add(r);
        sauvegarder();
        return r;
    }

    /** Toutes les réclamations, les plus récentes en premier. */
    public List<Reclamation> toutes() {
        List<Reclamation> copie = new ArrayList<>(reclamations);
        copie.sort((a, b) -> b.getDate().compareTo(a.getDate()) != 0
                ? b.getDate().compareTo(a.getDate())
                : Integer.compare(b.getId(), a.getId()));
        return copie;
    }

    public List<Reclamation> pourEtudiant(String matricule) {
        List<Reclamation> resultat = new ArrayList<>();
        for (Reclamation r : toutes()) {
            if (r.getMatricule().equalsIgnoreCase(matricule)) {
                resultat.add(r);
            }
        }
        return resultat;
    }

    public int nombreEnAttente() {
        int n = 0;
        for (Reclamation r : reclamations) {
            if (r.getStatut() == Reclamation.Statut.EN_ATTENTE) {
                n++;
            }
        }
        return n;
    }

    /** Marque une réclamation comme traitée. Retourne false si elle n'existe pas. */
    public boolean marquerTraitee(int id) {
        for (Reclamation r : reclamations) {
            if (r.getId() == id) {
                r.setStatut(Reclamation.Statut.TRAITEE);
                sauvegarder();
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Fichier texte : une réclamation par ligne, champs séparés par une tabulation
    // id  matricule  nom  matiere  semestre  date  statut  message
    // ------------------------------------------------------------------

    private boolean sauvegarder() {
        List<String> lignes = new ArrayList<>();
        lignes.add("# id\tmatricule\tnom\tmatiere\tsemestre\tdate\tstatut\tmessage");
        for (Reclamation r : reclamations) {
            lignes.add(r.getId() + "\t" + echapper(r.getMatricule()) + "\t" + echapper(r.getNomEtudiant()) + "\t"
                    + echapper(r.getMatiere()) + "\t" + echapper(r.getSemestre()) + "\t" + r.getDate() + "\t"
                    + r.getStatut().name() + "\t" + echapper(r.getMessage()));
        }
        try {
            Stockage.ecrire(fichier, lignes);
            return true;
        } catch (IOException ex) {
            System.err.println("Sauvegarde des réclamations impossible : " + ex.getMessage());
            return false;
        }
    }

    private void charger() {
        try {
            for (String ligne : Stockage.lire(fichier)) {
                if (ligne.trim().isEmpty() || ligne.startsWith("#")) {
                    continue;
                }
                String[] p = ligne.split("\t", -1);
                if (p.length != 8) {
                    continue;
                }
                try {
                    reclamations.add(new Reclamation(Integer.parseInt(p[0]), retablir(p[1]), retablir(p[2]),
                            retablir(p[3]), retablir(p[4]), retablir(p[7]), LocalDateTime.parse(p[5]),
                            Reclamation.Statut.valueOf(p[6])));
                } catch (RuntimeException ignoree) {
                    // ligne mal formée : on l'ignore
                }
            }
        } catch (IOException ex) {
            System.err.println("Lecture des réclamations impossible : " + ex.getMessage());
        }
    }

    /** Protège les tabulations et retours à la ligne pour tenir sur une seule ligne de fichier. */
    private static String echapper(String texte) {
        StringBuilder sb = new StringBuilder();
        for (char c : texte.toCharArray()) {
            switch (c) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\r':
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String retablir(String texte) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < texte.length(); i++) {
            char c = texte.charAt(i);
            if (c == '\\' && i + 1 < texte.length()) {
                char suivant = texte.charAt(++i);
                sb.append(suivant == 'n' ? '\n' : suivant == 't' ? '\t' : suivant);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
