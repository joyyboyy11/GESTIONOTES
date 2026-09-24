package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Une classe : ses étudiants et les matières qui y sont enseignées. */
public class ClasseEtude {

    private String nom;
    private Ecole ecole;
    private final List<Etudiant> etudiants = new ArrayList<>();
    private final List<Matiere> matieres = new ArrayList<>();

    public ClasseEtude(String nom) {
        this.nom = nom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Ecole getEcole() {
        return ecole;
    }

    /** Appelé par Ecole.ajouterClasse(). */
    void setEcole(Ecole ecole) {
        this.ecole = ecole;
    }

    public List<Etudiant> getEtudiants() {
        return Collections.unmodifiableList(etudiants);
    }

    public List<Matiere> getMatieres() {
        return Collections.unmodifiableList(matieres);
    }

    public void ajouterEtudiant(Etudiant etudiant) {
        etudiants.add(etudiant);
        etudiant.setClasse(this);
    }

    public void ajouterMatiere(Matiere matiere) {
        matieres.add(matiere);
    }

    @Override
    public String toString() {
        return nom;
    }
}
