import controller.AuthentificationController;
import controller.DonneesController;
import controller.NoteController;
import controller.ReclamationController;
import model.Etudiant;
import model.Note;
import model.Personne;
import model.Professeur;
import model.Reclamation;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tests de la logique métier (sans interface graphique).
 * Les fichiers sont écrits dans un dossier temporaire : les vraies données ne sont pas touchées.
 *
 * Lancement : voir le README (script tests.sh / tests.bat).
 */
public class TestControleurs {

    private static int reussis = 0;
    private static int echoues = 0;

    private static void verifier(String description, boolean condition) {
        if (condition) {
            reussis++;
            System.out.println("  [OK]     " + description);
        } else {
            echoues++;
            System.out.println("  [ECHEC]  " + description);
        }
    }

    private static boolean proche(double a, double b) {
        return Math.abs(a - b) < 0.0001;
    }

    public static void main(String[] args) throws Exception {
        Path dossier = Files.createTempDirectory("notes-test");
        System.setProperty("notes.data", dossier.toString()); // avant toute utilisation des contrôleurs

        System.out.println("== Modèle : calcul et conversion des notes ==");
        verifier("moyenne (14 + 16) / 2 = 15", proche(Note.calculerMoyenne(14, 16), 15));
        verifier("moyenne (9 + 10) / 2 = 9,5", proche(Note.calculerMoyenne(9, 10), 9.5));
        verifier("« 12,5 » est lu comme 12.5", Note.parser("12,5") != null && proche(Note.parser("12,5"), 12.5));
        verifier("« 12.75 » est lu comme 12.75", Note.parser("12.75") != null && proche(Note.parser("12.75"), 12.75));
        verifier("« abc » est refusé", Note.parser("abc") == null);
        verifier("« -3 » est refusé", Note.parser("-3") == null);
        verifier("« NaN » est refusé", Note.parser("NaN") == null);
        verifier("texte vide refusé", Note.parser("  ") == null);
        verifier("21 est hors limites", !Note.estValide(21));
        verifier("0 et 20 sont valides", Note.estValide(0) && Note.estValide(20));
        verifier("formatage à la française : 12,50", "12,50".equals(Note.formater(12.5)));

        System.out.println("== Authentification et héritage ==");
        DonneesController donnees = new DonneesController();
        AuthentificationController auth = new AuthentificationController(donnees);
        Personne p = auth.authentifier("prof", "1234");
        verifier("prof / 1234 -> Professeur", p instanceof Professeur && "PROFESSEUR".equals(p.getRole()));
        Personne e = auth.authentifier("etu", "1234");
        verifier("etu / 1234 -> Etudiant (Awa Ndiaye)", e instanceof Etudiant && "Awa Ndiaye".equals(e.getNomComplet()));
        verifier("etu est bien une Personne de rôle ETUDIANT", e instanceof Personne && "ETUDIANT".equals(e.getRole()));
        verifier("mauvais mot de passe refusé", auth.authentifier("prof", "0000") == null);
        verifier("login inconnu refusé", auth.authentifier("inconnu", "1234") == null);
        verifier("login vide refusé", auth.authentifier("", "") == null);
        verifier("utilisateur connecté mémorisé", auth.authentifier("prof", "1234") == auth.getUtilisateurConnecte());
        auth.deconnecter();
        verifier("déconnexion", auth.getUtilisateurConnecte() == null);
        Personne autre = auth.authentifier("et002", "1234");
        verifier("et002 / 1234 -> Moussa Diop", autre != null && "Moussa Diop".equals(autre.getNomComplet()));

        System.out.println("== Données et recherche ==");
        verifier("3 écoles", donnees.getEcoles().size() == 3);
        verifier("ET001 rattaché à Licence 2 Informatique",
                "Licence 2 Informatique".equals(donnees.trouverEtudiant("ET001").getClasse().getNom()));
        verifier("recherche « ndiaye » -> 1 résultat", donnees.rechercherEtudiants("ndiaye").size() == 1);
        verifier("recherche « FATOU » (majuscules)", donnees.rechercherEtudiants("FATOU").size() == 1);
        verifier("recherche « cisse » trouve Cissé (sans accent)", donnees.rechercherEtudiants("cisse").size() == 1);
        verifier("recherche « et1 » -> 3 étudiants (ET101..ET103)", donnees.rechercherEtudiants("et1").size() == 3);
        verifier("recherche introuvable", donnees.rechercherEtudiants("zzz").isEmpty());

        System.out.println("== Notes : consultation et moyennes ==");
        NoteController notes = new NoteController();
        verifier("notes de démonstration créées", notes.nombreNotes() == 8);
        Double generale = notes.moyenneGenerale("ET001", "Semestre 1");
        verifier("moyenne générale ET001 = 13,4375", generale != null && proche(generale, 13.4375));
        verifier("moyenne Java ET001 = 15", proche(notes.trouverNote("ET001", "Java", "Semestre 1").getMoyenne(), 15));
        verifier("pas de note en Semestre 2 -> moyenne générale null", notes.moyenneGenerale("ET001", "Semestre 2") == null);

        System.out.println("== Notes : ajout, modification, suppression ==");
        verifier("ajout d'une note", notes.ajouterNote(new Note("ET003", "Réseaux", "Semestre 1", 11, 13)));
        verifier("ajout en double refusé", !notes.ajouterNote(new Note("ET003", "Réseaux", "Semestre 1", 1, 2)));
        verifier("modification", notes.modifierNote("ET003", "Réseaux", "Semestre 1", 12, 14)
                && proche(notes.trouverNote("ET003", "Réseaux", "Semestre 1").getMoyenne(), 13));
        verifier("modification d'une note inexistante refusée", !notes.modifierNote("ET003", "Java", "Semestre 2", 1, 1));
        verifier("sauvegarde", notes.sauvegarder());
        NoteController rechargees = new NoteController();
        verifier("rechargement depuis le fichier : même nombre de notes", rechargees.nombreNotes() == notes.nombreNotes());
        verifier("rechargement : note modifiée conservée",
                proche(rechargees.trouverNote("ET003", "Réseaux", "Semestre 1").getDevoir(), 12));
        verifier("suppression", notes.supprimerNote("ET003", "Réseaux", "Semestre 1")
                && notes.trouverNote("ET003", "Réseaux", "Semestre 1") == null);
        verifier("suppression d'une note inexistante refusée", !notes.supprimerNote("ET003", "Réseaux", "Semestre 1"));

        System.out.println("== Réclamations ==");
        ReclamationController reclamations = new ReclamationController();
        Etudiant awa = donnees.trouverEtudiant("ET001");
        String message = "Ma note d'examen semble erronée.\nMerci de vérifier ; chemin C:\\notes\tfin";
        Reclamation r = reclamations.envoyer(awa, "Java", "Semestre 1", message);
        verifier("réclamation créée (id 1, en attente)", r.getId() == 1 && r.getStatut() == Reclamation.Statut.EN_ATTENTE);
        verifier("1 réclamation en attente", reclamations.nombreEnAttente() == 1);
        boolean vide = false;
        try {
            reclamations.envoyer(awa, "Java", "Semestre 1", "   ");
        } catch (IllegalArgumentException ex) {
            vide = true;
        }
        verifier("réclamation vide refusée", vide);
        ReclamationController rechargeesRec = new ReclamationController();
        verifier("rechargement : message identique (retours à la ligne, tabulation, \\)",
                rechargeesRec.toutes().size() == 1 && message.equals(rechargeesRec.toutes().get(0).getMessage()));
        verifier("pourEtudiant filtre par matricule", rechargeesRec.pourEtudiant("ET001").size() == 1
                && rechargeesRec.pourEtudiant("ET002").isEmpty());
        verifier("marquer comme traitée", rechargeesRec.marquerTraitee(1) && rechargeesRec.nombreEnAttente() == 0);
        verifier("statut « Traitée » conservé après rechargement",
                new ReclamationController().toutes().get(0).getStatut() == Reclamation.Statut.TRAITEE);
        verifier("marquer une réclamation inexistante refusé", !rechargeesRec.marquerTraitee(99));

        System.out.println();
        System.out.println("Résultat : " + reussis + " réussi(s), " + echoues + " échoué(s)");
        System.exit(echoues == 0 ? 0 : 1);
    }
}
