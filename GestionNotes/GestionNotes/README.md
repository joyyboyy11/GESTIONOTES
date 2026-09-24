# GestionNotes — Application Java Swing de gestion des notes scolaires

Projet de fin de module POO. Application de bureau (Java Swing) permettant à un
professeur de saisir, calculer, modifier et supprimer les notes des étudiants,
et à un étudiant de consulter ses notes/moyennes et d'envoyer des réclamations.

## Prérequis

- **JDK 17 ou supérieur** installé (testé avec JDK 21).
  Vérifier avec : `java -version` et `javac -version`

## Structure du projet

```
GestionNotes/
├── src/
│   ├── model/        Classes métier (Personne, Etudiant, Professeur, Ecole,
│   │                 ClasseEtude, Matiere, Note, Reclamation)
│   ├── controller/   Logique applicative (authentification, notes,
│   │                 réclamations, stockage fichier)
│   ├── view/         Interfaces graphiques Swing (login, page professeur,
│   │                 page étudiant, thème visuel)
│   ├── main/         Point d'entrée (Main.java)
│   └── images/       Images utilisées par l'application (logo, photos par
│                     défaut, fond d'écran de connexion)
├── tests/
│   └── TestControleurs.java   Tests unitaires (48 assertions)
├── run.sh            Script de compilation + lancement (Linux/macOS)
├── run.bat           Script de compilation + lancement (Windows)
├── test.sh           Script pour lancer les tests unitaires (Linux/macOS)
├── test.bat          Script pour lancer les tests unitaires (Windows)
└── README.md         Ce fichier
```

## Compiler et lancer l'application

### Linux / macOS

```bash
chmod +x run.sh
./run.sh
```

### Windows

Double-cliquer sur `run.bat`, ou dans une invite de commandes :

```
run.bat
```

### Manuellement (si besoin)

```bash
# Compilation
javac -encoding UTF-8 -d out $(find src -name "*.java")   # Linux/macOS
# ou sous Windows (PowerShell) :
#   javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java src | % { $_.FullName })

# Copier les images dans le dossier compilé
cp -r src/images out/images        # Linux/macOS
# xcopy /E /I src\images out\images   (Windows)

# Lancement
java -cp out main.Main
```

## Comptes de démonstration

L'application inclut des données de démonstration (3 établissements, classes,
matières, étudiants et un professeur) afin de pouvoir tester immédiatement
toutes les fonctionnalités.

| Rôle        | Identifiant | Mot de passe | Nom                |
|-------------|-------------|--------------|--------------------|
| Professeur  | `prof`      | `1234`       | Ibrahima Diallo    |
| Étudiant    | `etu`       | `1234`       | Awa Ndiaye (ET001) |

Les autres étudiants de démonstration se connectent avec leur **matricule en
minuscules** (ex. `et002`) et le mot de passe `1234`.

## Fonctionnalités principales

**Côté professeur :**
- Filtrage en cascade École → Classe → Matière → Semestre
- Recherche d'étudiant locale (dans la classe filtrée) ou globale (toutes écoles)
- Saisie des notes (devoir/examen) avec calcul automatique de la moyenne
- Surlignage en rouge des saisies invalides (non numériques ou hors 0–20)
- Enregistrement, modification et suppression des notes (avec confirmation)
- Consultation et traitement des réclamations reçues des étudiants

**Côté étudiant :**
- Consultation du profil (avec photo modifiable)
- Consultation des notes par semestre
- Calcul de la moyenne générale avec mention
- Envoi de réclamations et consultation de leur historique/statut

## Persistance des données

Les notes et réclamations sont sauvegardées dans des fichiers texte au format
UTF-8, dans un dossier `data/` créé automatiquement à côté de l'application
(le chemin peut être personnalisé via l'option `-Dnotes.data=chemin`).

## Tests

Un jeu de 48 tests unitaires couvre les calculs de moyennes, la validation des
notes, l'authentification, la recherche insensible à la casse/aux accents et
la persistance (notes et réclamations).

```bash
./test.sh      # Linux/macOS
test.bat       # Windows
```

## Notes techniques

- Toutes les images (logo, façade d'école, avatars par défaut) ont été
  générées spécifiquement pour ce projet — aucune image tierce protégée par
  droit d'auteur n'est utilisée.
- L'apparence graphique s'adapte au look and feel natif du système
  d'exploitation (`UIManager.setLookAndFeel` cross-platform).
