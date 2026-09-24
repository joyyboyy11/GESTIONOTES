package controller;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Petit utilitaire de lecture / écriture de fichiers texte (UTF-8).
 * Les données sont rangées dans le dossier « data » (à côté de l'application).
 * Le dossier peut être changé avec l'option JVM  -Dnotes.data=chemin  (utile pour les tests).
 */
public final class Stockage {

    private static final File DOSSIER = new File(System.getProperty("notes.data", "data"));

    private Stockage() {
    }

    public static File fichier(String nom) {
        return new File(DOSSIER, nom);
    }

    /** Lit toutes les lignes d'un fichier (liste vide si le fichier n'existe pas). */
    public static List<String> lire(File fichier) throws IOException {
        if (!fichier.exists()) {
            return new ArrayList<>();
        }
        return Files.readAllLines(fichier.toPath(), StandardCharsets.UTF_8);
    }

    /** Écrit les lignes dans le fichier (création du dossier si besoin, écriture sécurisée). */
    public static void ecrire(File fichier, List<String> lignes) throws IOException {
        File parent = fichier.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Impossible de créer le dossier " + parent);
        }
        File temporaire = new File(parent, fichier.getName() + ".tmp");
        Files.write(temporaire.toPath(), lignes, StandardCharsets.UTF_8);
        Files.move(temporaire.toPath(), fichier.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
