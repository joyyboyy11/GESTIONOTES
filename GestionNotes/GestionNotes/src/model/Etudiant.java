package model;

/** Un étudiant, identifié par son matricule et rattaché à une classe. */
public class Etudiant extends Personne {

    private final String matricule;
    private ClasseEtude classe;
    private String cheminPhoto; // photo de profil (facultative)

    public Etudiant(String matricule, String nom, String prenom, String login, String motDePasse) {
        super(nom, prenom, login, motDePasse);
        this.matricule = matricule;
    }

    @Override
    public String getRole() {
        return "ETUDIANT";
    }

    public String getMatricule() {
        return matricule;
    }

    public ClasseEtude getClasse() {
        return classe;
    }

    /** Appelé par ClasseEtude.ajouterEtudiant(). */
    void setClasse(ClasseEtude classe) {
        this.classe = classe;
    }

    public String getCheminPhoto() {
        return cheminPhoto;
    }

    public void setCheminPhoto(String cheminPhoto) {
        this.cheminPhoto = cheminPhoto;
    }
}
