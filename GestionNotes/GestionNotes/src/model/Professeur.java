package model;

/** Un professeur, autorisé à saisir les notes et à consulter les réclamations. */
public class Professeur extends Personne {

    private String specialite;

    public Professeur(String nom, String prenom, String login, String motDePasse, String specialite) {
        super(nom, prenom, login, motDePasse);
        this.specialite = specialite;
    }

    @Override
    public String getRole() {
        return "PROFESSEUR";
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }
}
