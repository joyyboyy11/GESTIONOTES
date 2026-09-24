package view;

import model.Etudiant;
import model.Note;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Couleurs, polices et petits composants graphiques réutilisés par toutes les fenêtres.
 * Ce fichier regroupe tout ce qui concerne « l'interface moderne avec couleurs ».
 */
public final class Theme {

    // ---- Couleurs (palette « corporate » sobre : marine + or mat) ----
    public static final Color PRIMAIRE = new Color(0x18274F);
    public static final Color PRIMAIRE_FONCE = new Color(0x0E1B38);
    public static final Color ACCENT = new Color(0xC49B4A);
    public static final Color SUCCES = new Color(0x1E8E5A);
    public static final Color DANGER = new Color(0xB3392B);
    public static final Color AVERTISSEMENT = new Color(0xB9791F);
    public static final Color INACTIF = new Color(0x8792A8);
    public static final Color FOND = new Color(0xF4F6FA);
    public static final Color TEXTE = new Color(0x1E293B);
    public static final Color TEXTE_CLAIR = new Color(0x64748B);
    public static final Color BORDURE = new Color(0xE1E6EF);
    public static final Color LIGNE_ALTERNEE = new Color(0xF7F9FC);
    public static final Color SELECTION = new Color(0xE3E9F7);
    public static final Color OMBRE = new Color(0x0A1830);

    // ---- Polices (polices logiques Java : disponibles sur tous les systèmes) ----
    public static final Font POLICE = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    public static final Font POLICE_GRAS = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    public static final Font POLICE_PETITE = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    public static final Font POLICE_TITRE = new Font(Font.SANS_SERIF, Font.BOLD, 22);
    public static final Font POLICE_SOUS_TITRE = new Font(Font.SANS_SERIF, Font.BOLD, 16);

    private Theme() {
    }

    // =====================================================================
    // Images
    // =====================================================================

    /** Charge une image du dossier « images » (dans le classpath). Retourne null si elle est introuvable. */
    public static BufferedImage chargerImage(String nom) {
        URL url = Theme.class.getResource("/images/" + nom);
        if (url == null) {
            return null;
        }
        try {
            return ImageIO.read(url);
        } catch (IOException ex) {
            return null;
        }
    }

    /** Image redimensionnée pour tenir dans la boîte donnée (proportions conservées). */
    public static ImageIcon icone(String nom, int largeurMax, int hauteurMax) {
        BufferedImage image = chargerImage(nom);
        if (image == null) {
            return null;
        }
        double echelle = Math.min((double) largeurMax / image.getWidth(), (double) hauteurMax / image.getHeight());
        int w = Math.max(1, (int) Math.round(image.getWidth() * echelle));
        int h = Math.max(1, (int) Math.round(image.getHeight() * echelle));
        return new ImageIcon(redimensionner(image, w, h));
    }

    /** Photo de l'étudiant (celle qu'il a choisie, sinon l'avatar par défaut). */
    public static BufferedImage photoEtudiant(Etudiant etudiant) {
        String chemin = etudiant.getCheminPhoto();
        if (chemin != null) {
            try {
                BufferedImage photo = ImageIO.read(new File(chemin));
                if (photo != null) {
                    return photo;
                }
            } catch (IOException ex) {
                // on retombe sur l'image par défaut
            }
        }
        return chargerImage("etudiant.png");
    }

    /** Image recadrée en cercle (photo de profil). */
    public static ImageIcon photoRonde(BufferedImage source, int diametre) {
        BufferedImage cercle = new BufferedImage(diametre, diametre, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = cercle.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.fill(new Ellipse2D.Float(0, 0, diametre, diametre));
        if (source != null) {
            double echelle = Math.max((double) diametre / source.getWidth(), (double) diametre / source.getHeight());
            int w = Math.max(diametre, (int) Math.round(source.getWidth() * echelle));
            int h = Math.max(diametre, (int) Math.round(source.getHeight() * echelle));
            BufferedImage reduite = redimensionner(source, w, h);
            g.setComposite(AlphaComposite.SrcIn);
            g.drawImage(reduite, (diametre - w) / 2, (diametre - h) / 2, null);
            g.setComposite(AlphaComposite.SrcOver);
        }
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(3f));
        g.draw(new Ellipse2D.Float(1.5f, 1.5f, diametre - 3f, diametre - 3f));
        g.dispose();
        return new ImageIcon(cercle);
    }

    /** Réduction en plusieurs passes pour garder une bonne qualité. */
    private static BufferedImage redimensionner(BufferedImage source, int w, int h) {
        BufferedImage courant = source;
        int cw = source.getWidth();
        int ch = source.getHeight();
        while (cw / 2 >= w && ch / 2 >= h) {
            cw /= 2;
            ch /= 2;
            courant = unePasse(courant, cw, ch);
        }
        return unePasse(courant, w, h);
    }

    private static BufferedImage unePasse(BufferedImage source, int w, int h) {
        BufferedImage sortie = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sortie.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(source, 0, 0, w, h, null);
        g.dispose();
        return sortie;
    }

    // =====================================================================
    // Fabriques de composants
    // =====================================================================

    public static JLabel etiquette(String texte, Font police, Color couleur) {
        JLabel label = new JLabel(texte);
        label.setFont(police);
        label.setForeground(couleur);
        return label;
    }

    public static BoutonArrondi bouton(String texte, Color couleur) {
        return new BoutonArrondi(texte, couleur);
    }

    public static void styliserChamp(final JTextComponent champ) {
        champ.setFont(POLICE);
        champ.setForeground(TEXTE);
        champ.setBorder(bordureChamp(BORDURE));
        champ.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                champ.setBorder(bordureChamp(PRIMAIRE));
            }

            @Override
            public void focusLost(FocusEvent e) {
                champ.setBorder(bordureChamp(BORDURE));
            }
        });
    }

    private static javax.swing.border.Border bordureChamp(Color couleur) {
        return BorderFactory.createCompoundBorder(new LineBorder(couleur, 1, true), new EmptyBorder(6, 10, 6, 10));
    }

    public static void styliserCombo(JComboBox<?> combo) {
        combo.setFont(POLICE);
        combo.setBackground(Color.WHITE);
        combo.setForeground(TEXTE);
        combo.setPreferredSize(new Dimension(combo.getPreferredSize().width, 34));
    }

    public static void styliserTable(JTable table) {
        table.setRowHeight(32);
        table.setFont(POLICE);
        table.setForeground(TEXTE);
        table.setGridColor(BORDURE);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(SELECTION);
        table.setSelectionForeground(TEXTE);
        table.setFillsViewportHeight(true);
        JTableHeader entete = table.getTableHeader();
        entete.setReorderingAllowed(false);
        entete.setPreferredSize(new Dimension(0, 36));
        entete.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valeur, boolean sel, boolean focus,
                                                          int ligne, int colonne) {
                super.getTableCellRendererComponent(t, valeur, false, false, ligne, colonne);
                setOpaque(true);
                setBackground(PRIMAIRE);
                setForeground(Color.WHITE);
                setFont(POLICE_GRAS);
                setHorizontalAlignment(CENTER);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
    }

    /** Bandeau du haut (dégradé bleu + liseré doré) avec logo, titre et informations sur l'utilisateur connecté. */
    public static JPanel entete(String titre, String sousTitre, Icon avatar, String nomUtilisateur,
                                String role, JButton boutonDeconnexion) {
        JPanel bandeau = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, PRIMAIRE_FONCE, getWidth(), 0, PRIMAIRE));
                g2.fillRect(0, 0, getWidth(), getHeight());
                // léger assombrissement du bord inférieur (relief discret)
                for (int i = 0; i < 5; i++) {
                    g2.setColor(new Color(0, 0, 0, 10 - i * 2));
                    g2.fillRect(0, getHeight() - 1 - i, getWidth(), 1);
                }
                // fin liseré or, discret, tout en bas du bandeau
                g2.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 200));
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
                g2.dispose();
            }
        };
        bandeau.setBorder(new EmptyBorder(12, 24, 12, 24));

        JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        gauche.setOpaque(false);
        ImageIcon logo = icone("logo.png", 48, 48);
        if (logo != null) {
            gauche.add(new JLabel(logo));
        }
        JPanel titres = new JPanel();
        titres.setOpaque(false);
        titres.setLayout(new BoxLayout(titres, BoxLayout.Y_AXIS));
        titres.add(etiquette(titre, POLICE_TITRE, Color.WHITE));
        titres.add(etiquette(sousTitre, POLICE_PETITE, new Color(0xC9D4F2)));
        gauche.add(titres);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        droite.setOpaque(false);
        if (avatar != null) {
            droite.add(new JLabel(avatar));
        }
        JPanel identite = new JPanel();
        identite.setOpaque(false);
        identite.setLayout(new BoxLayout(identite, BoxLayout.Y_AXIS));
        identite.add(etiquette(nomUtilisateur, POLICE_GRAS, Color.WHITE));
        identite.add(etiquette(role, POLICE_PETITE, new Color(0xC9D4F2)));
        droite.add(identite);
        droite.add(boutonDeconnexion);

        bandeau.add(gauche, BorderLayout.WEST);
        bandeau.add(droite, BorderLayout.EAST);

        // fine ombre sous le bandeau pour lui donner du relief (élévation « material »)
        JPanel ombre = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                for (int i = 0; i < getHeight(); i++) {
                    int alpha = Math.max(0, 26 - (int) (i * (26.0 / getHeight())));
                    g2.setColor(new Color(0, 0, 0, alpha));
                    g2.fillRect(0, i, getWidth(), 1);
                }
                g2.dispose();
            }
        };
        ombre.setOpaque(false);
        ombre.setPreferredSize(new Dimension(0, 5));

        JPanel conteneur = new JPanel(new BorderLayout());
        conteneur.add(bandeau, BorderLayout.CENTER);
        conteneur.add(ombre, BorderLayout.SOUTH);
        return conteneur;
    }

    // =====================================================================
    // Composants personnalisés
    // =====================================================================

    /** Bouton coloré aux coins arrondis, avec effet au survol. Indépendant du « look and feel » du système. */
    public static class BoutonArrondi extends JButton {
        private Color base;
        private boolean survol;

        public BoutonArrondi(String texte, Color base) {
            super(texte);
            this.base = base;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(POLICE_GRAS);
            setMargin(new Insets(0, 0, 0, 0));
            setBorder(new EmptyBorder(9, 20, 9, 20));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    survol = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    survol = false;
                    repaint();
                }
            });
        }

        public void setCouleur(Color couleur) {
            this.base = couleur;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fond;
            if (!isEnabled()) {
                fond = new Color(0xD5DAE3);
            } else if (getModel().isPressed()) {
                fond = base.darker().darker();
            } else if (survol) {
                fond = base.darker();
            } else {
                fond = base;
            }
            g2.setColor(fond);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Panneau « carte » : fond uni aux coins arrondis, fine bordure et légère ombre portée. */
    public static class Carte extends JPanel {
        private final Color fond;
        private final int rayon;
        private final boolean ombre;
        private static final int MARGE_OMBRE = 6;

        public Carte(LayoutManager layout, Color fond, int rayon) {
            this(layout, fond, rayon, true);
        }

        public Carte(LayoutManager layout, Color fond, int rayon, boolean ombre) {
            super(layout);
            this.fond = fond;
            this.rayon = rayon;
            this.ombre = ombre;
            setOpaque(false);
            if (ombre) {
                setBorder(new EmptyBorder(0, 0, MARGE_OMBRE, 0));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int largeur = getWidth() - 1;
            int hauteur = getHeight() - 1 - (ombre ? MARGE_OMBRE : 0);
            if (ombre) {
                for (int i = MARGE_OMBRE; i >= 1; i--) {
                    int alpha = Math.max(2, 14 - i * 2);
                    g2.setColor(new Color(OMBRE.getRed(), OMBRE.getGreen(), OMBRE.getBlue(), alpha));
                    g2.fillRoundRect(0, i, largeur, hauteur, rayon, rayon);
                }
            }
            g2.setColor(fond);
            g2.fillRoundRect(0, 0, largeur, hauteur, rayon, rayon);
            g2.setColor(BORDURE);
            g2.drawRoundRect(0, 0, largeur, hauteur, rayon, rayon);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Panneau avec une image de fond qui remplit toute la surface (et un voile coloré par-dessus). */
    public static class PanneauImage extends JPanel {
        private final BufferedImage image;
        private final Color voile;

        public PanneauImage(BufferedImage image, Color voile) {
            this.image = image;
            this.voile = voile;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int largeur = getWidth();
            int hauteur = getHeight();
            if (image != null) {
                double echelle = Math.max((double) largeur / image.getWidth(), (double) hauteur / image.getHeight());
                int w = (int) Math.ceil(image.getWidth() * echelle);
                int h = (int) Math.ceil(image.getHeight() * echelle);
                g2.drawImage(image, (largeur - w) / 2, (hauteur - h) / 2, w, h, null);
            } else {
                g2.setPaint(new GradientPaint(0, 0, PRIMAIRE_FONCE, 0, hauteur, PRIMAIRE));
                g2.fillRect(0, 0, largeur, hauteur);
            }
            g2.setColor(voile);
            g2.fillRect(0, 0, largeur, hauteur);
            g2.dispose();
        }
    }

    /**
     * Rendu des cellules d'un tableau : lignes alternées, colonnes centrées,
     * moyennes en vert (≥ 10) ou en rouge (&lt; 10) et statuts colorés.
     */
    public static class RenduTable extends DefaultTableCellRenderer {
        private final int colonneMoyenne;
        private final int colonneStatut;
        private final Set<Integer> centrees = new HashSet<>();
        private final Set<Integer> saisies = new HashSet<>();

        /** Colonnes de saisie de notes : une valeur invalide (abc, 25, ...) y est surlignée en rouge. */
        public RenduTable validerColonnes(int... colonnes) {
            for (int c : colonnes) {
                saisies.add(c);
            }
            return this;
        }

        /** Mettre -1 pour une colonne inexistante. */
        public RenduTable(int colonneMoyenne, int colonneStatut, int... colonnesCentrees) {
            this.colonneMoyenne = colonneMoyenne;
            this.colonneStatut = colonneStatut;
            for (int c : colonnesCentrees) {
                centrees.add(c);
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object valeur, boolean selectionne,
                                                      boolean focus, int ligne, int colonne) {
            super.getTableCellRendererComponent(table, valeur, selectionne, false, ligne, colonne);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            setFont(POLICE);
            setHorizontalAlignment(centrees.contains(colonne) ? CENTER : LEFT);
            if (selectionne) {
                setBackground(SELECTION);
            } else {
                setBackground(ligne % 2 == 0 ? Color.WHITE : LIGNE_ALTERNEE);
            }
            setForeground(TEXTE);
            String texte = valeur == null ? "" : valeur.toString();
            if (colonne == colonneMoyenne) {
                Double moyenne = Note.parser(texte);
                if (moyenne != null) {
                    setForeground(moyenne < 10 ? DANGER : SUCCES);
                    setFont(POLICE_GRAS);
                }
            }
            if (saisies.contains(colonne) && !selectionne && !texte.trim().isEmpty()) {
                Double saisie = Note.parser(texte);
                if (saisie == null || !Note.estValide(saisie)) {
                    setBackground(new Color(0xFDECEA));
                    setForeground(DANGER);
                    setFont(POLICE_GRAS);
                }
            }
            if (colonne == colonneStatut) {
                setFont(POLICE_GRAS);
                switch (texte) {
                    case "Enregistré":
                    case "Traitée":
                        setForeground(SUCCES);
                        break;
                    case "Modifié":
                    case "En attente":
                        setForeground(AVERTISSEMENT);
                        break;
                    default:
                        setForeground(TEXTE_CLAIR);
                }
            }
            return this;
        }
    }
}
