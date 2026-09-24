package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Un établissement scolaire qui regroupe plusieurs classes. */
public class Ecole {

    private String nom;
    private String ville;
    private final List<ClasseEtude> classes = new ArrayList<>();

    public Ecole(String nom, String ville) {
        this.nom = nom;
        this.ville = ville;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public List<ClasseEtude> getClasses() {
        return Collections.unmodifiableList(classes);
    }

    public void ajouterClasse(ClasseEtude classe) {
        classes.add(classe);
        classe.setEcole(this);
    }

    /** Affiché dans les listes déroulantes. */
    @Override
    public String toString() {
        return nom;
    }
}
