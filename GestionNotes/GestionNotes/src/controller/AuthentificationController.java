package controller;

import model.Etudiant;
import model.Personne;
import model.Professeur;

/** Vérifie les identifiants et mémorise l'utilisateur connecté. */
public class AuthentificationController {

    private final DonneesController donnees;
    private Personne utilisateurConnecte;

    public AuthentificationController(DonneesController donnees) {
        this.donnees = donnees;
    }

    /**
     * Tente de connecter un utilisateur.
     *
     * @return le Professeur ou l'Etudiant correspondant, ou null si les identifiants sont incorrects
     */
    public Personne authentifier(String login, String motDePasse) {
        Professeur professeur = donnees.getProfesseur();
        if (professeur.verifierIdentifiants(login, motDePasse)) {
            utilisateurConnecte = professeur;
            return professeur;
        }
        for (Etudiant etudiant : donnees.getTousLesEtudiants()) {
            if (etudiant.verifierIdentifiants(login, motDePasse)) {
                utilisateurConnecte = etudiant;
                return etudiant;
            }
        }
        utilisateurConnecte = null;
        return null;
    }

    public Personne getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public void deconnecter() {
        utilisateurConnecte = null;
    }
}
