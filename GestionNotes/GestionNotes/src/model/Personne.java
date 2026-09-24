package model;

/**
 * Classe mère de tous les utilisateurs de l'application.
 * Illustre l'ENCAPSULATION (attributs privés + accesseurs) et l'HÉRITAGE
 * (Etudiant et Professeur héritent de cette classe).
 */
public abstract class Personne {

    private String nom;
    private String prenom;
    private String login;
    private String motDePasse;

    protected Personne(String nom, String prenom, String login, String motDePasse) {
        this.nom = nom;
        this.prenom = prenom;
        this.login = login;
        this.motDePasse = motDePasse;
    }

    /** Rôle de la personne (polymorphisme : redéfini dans chaque sous-classe). */
    public abstract String getRole();

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    /** Prénom + nom, par exemple « Awa Ndiaye ». */
    public String getNomComplet() {
        return prenom + " " + nom;
    }

    /** Vérifie le couple login / mot de passe (login insensible à la casse). */
    public boolean verifierIdentifiants(String login, String motDePasse) {
        return login != null && motDePasse != null
                && this.login.equalsIgnoreCase(login.trim())
                && this.motDePasse.equals(motDePasse);
    }

    @Override
    public String toString() {
        return getNomComplet();
    }
}
