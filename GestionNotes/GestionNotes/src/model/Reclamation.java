package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Une réclamation envoyée par un étudiant au sujet d'une note. */
public class Reclamation {

    public enum Statut {
        EN_ATTENTE("En attente"),
        TRAITEE("Traitée");

        private final String libelle;

        Statut(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    private final int id;
    private final String matricule;
    private final String nomEtudiant;
    private final String matiere;
    private final String semestre;
    private final String message;
    private final LocalDateTime date;
    private Statut statut;

    public Reclamation(int id, String matricule, String nomEtudiant, String matiere,
                       String semestre, String message, LocalDateTime date, Statut statut) {
        this.id = id;
        this.matricule = matricule;
        this.nomEtudiant = nomEtudiant;
        this.matiere = matiere;
        this.semestre = semestre;
        this.message = message;
        this.date = date;
        this.statut = statut;
    }

    public int getId() {
        return id;
    }

    public String getMatricule() {
        return matricule;
    }

    public String getNomEtudiant() {
        return nomEtudiant;
    }

    public String getMatiere() {
        return matiere;
    }

    public String getSemestre() {
        return semestre;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getDate() {
        return date;
    }

    /** Date au format jj/mm/aaaa hh:mm. */
    public String getDateFormatee() {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    /** Date au format jj/mm/aaaa. */
    public String getDateCourte() {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }
}
