package controller;

/** Regroupe les contrôleurs partagés par toutes les fenêtres de l'application. */
public class ContexteApplication {

    private final DonneesController donnees = new DonneesController();
    private final NoteController notes = new NoteController();
    private final ReclamationController reclamations = new ReclamationController();
    private final AuthentificationController authentification = new AuthentificationController(donnees);

    public DonneesController getDonnees() {
        return donnees;
    }

    public NoteController getNotes() {
        return notes;
    }

    public ReclamationController getReclamations() {
        return reclamations;
    }

    public AuthentificationController getAuthentification() {
        return authentification;
    }
}
