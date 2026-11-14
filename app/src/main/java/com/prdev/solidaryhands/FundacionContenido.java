package com.prdev.solidaryhands;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ============================================================================
 *  CONTENIDO DE CADA FUNDACIÓN  (Novedades / Trabajos / Personas)
 * ============================================================================
 *
 *  AQUÍ ES DONDE PONES LOS LINKS DE YOUTUBE.
 *
 *  Cada elemento se crea así:
 *
 *      new Entrada("Título", "Categoría", "Fecha", "Descripción", "LINK_DE_YOUTUBE")
 *
 *  - Para poner un video: reemplaza "" por el link, por ejemplo:
 *        "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
 *        "https://youtu.be/dQw4w9WgXcQ"
 *  - Si NO quieres video en ese elemento, déjalo así: ""   (no se muestra nada).
 *  - Para agregar más elementos, copia una línea "x.add(new Entrada(...));" y pégala debajo.
 *
 *  Busca los carteles:   >>> AQUÍ VA EL LINK DE YOUTUBE <<<
 * ============================================================================
 */
public final class FundacionContenido {

    public static class Entrada {
        public final String titulo;
        public final String categoria;
        public final String fecha;
        public final String descripcion;
        public final String videoUrl;

        public Entrada(String titulo, String categoria, String fecha, String descripcion, String videoUrl) {
            this.titulo = titulo;
            this.categoria = categoria;
            this.fecha = fecha;
            this.descripcion = descripcion;
            this.videoUrl = videoUrl == null ? "" : videoUrl.trim();
        }

        public boolean tieneVideo() {
            return extraerIdYoutube(videoUrl) != null;
        }
    }

    public static class Contenido {
        public final List<Entrada> novedades = new ArrayList<>();
        public final List<Entrada> trabajos = new ArrayList<>();
        public final List<Entrada> personas = new ArrayList<>();
    }

    private FundacionContenido() {}

    /** Devuelve el contenido según el nombre de la fundación. */
    public static Contenido obtener(String nombreFundacion, String programas) {
        String n = nombreFundacion == null ? "" : nombreFundacion.toLowerCase();
        if (n.contains("alalay")) {
            return alalay();
        } else if (n.contains("pallini")) {
            return pallini();
        } else if (n.contains("world vision")) {
            return worldVision();
        }
        return generico(nombreFundacion, programas);
    }

    // =========================================================================
    //  FUNDACIÓN ALALAY
    // =========================================================================
    private static Contenido alalay() {
        Contenido c = new Contenido();

        // ---- NOVEDADES ----
        // >>> AQUÍ VA EL LINK DE YOUTUBE <<<  (último parámetro de cada línea)
        c.novedades.add(new Entrada(
                "Última novedad de la fundación",
                "NOVEDAD",
                "Noviembre 2026",
                "Edita este texto en FundacionContenido.java para mostrar las novedades reales.",
                "https://www.youtube.com/watch?v=Io0fBr1XBUA&list=RD3vjkh-acmTE&index=10"   // <-- AQUÍ VA EL LINK DE YOUTUBE (Alalay - Novedades)
        ));

        // ---- TRABAJOS ----
        // >>> AQUÍ VA EL LINK DE YOUTUBE <<<
        c.trabajos.add(new Entrada(
                "Protección infantil",
                "PROGRAMA",
                "",
                "Protección, desarrollo integral y nutrición de niños, adolescentes y familias.",
                "https://www.youtube.com/watch?v=Io0fBr1XBUA&list=RD3vjkh-acmTE&index=10"   // <-- AQUÍ VA EL LINK DE YOUTUBE (Alalay - Trabajos)
        ));

        // ---- PERSONAS ----
        // >>> AQUÍ VA EL LINK DE YOUTUBE <<<
        c.personas.add(new Entrada(
                "Equipo de la fundación",
                "PERSONAS",
                "",
                "Conoce a quienes hacen posible el trabajo de la fundación.",
                "https://www.youtube.com/watch?v=Io0fBr1XBUA&list=RD3vjkh-acmTE&index=10"   // <-- AQUÍ VA EL LINK DE YOUTUBE (Alalay - Personas)
        ));
        return c;
    }

    // =========================================================================
    //  FUNDACIÓN PALLINI
    // =========================================================================
    private static Contenido pallini() {
        Contenido c = new Contenido();

        // ---- NOVEDADES ----
        c.novedades.add(new Entrada(
                "Última novedad de la fundación",
                "NOVEDAD",
                "Noviembre 2026",
                "Edita este texto en FundacionContenido.java para mostrar las novedades reales.",
                "https://www.youtube.com/watch?v=Io0fBr1XBUA&list=RD3vjkh-acmTE&index=10"   // <-- AQUÍ VA EL LINK DE YOUTUBE (Pallini - Novedades)
        ));

        // ---- TRABAJOS ----
        c.trabajos.add(new Entrada(
                "Educación y deporte",
                "PROGRAMA",
                "",
                "Educación integral, talleres deportivos, apoyo escolar y acompañamiento comunitario.",
                "https://www.youtube.com/watch?v=Io0fBr1XBUA&list=RD3vjkh-acmTE&index=10"   // <-- AQUÍ VA EL LINK DE YOUTUBE (Pallini - Trabajos)
        ));

        // ---- PERSONAS ----
        c.personas.add(new Entrada(
                "Equipo de la fundación",
                "PERSONAS",
                "",
                "Conoce a quienes hacen posible el trabajo de la fundación.",
                "https://www.youtube.com/watch?v=Io0fBr1XBUA&list=RD3vjkh-acmTE&index=10"   // <-- AQUÍ VA EL LINK DE YOUTUBE (Pallini - Personas)
        ));
        return c;
    }

    // =========================================================================
    //  WORLD VISION BOLIVIA
    // =========================================================================
    private static Contenido worldVision() {
        Contenido c = new Contenido();

        // ---- NOVEDADES ----
        c.novedades.add(new Entrada(
                "Última novedad de la fundación",
                "NOVEDAD",
                "Noviembre 2026",
                "Edita este texto en FundacionContenido.java para mostrar las novedades reales.",
                "https://www.youtube.com/watch?v=Io0fBr1XBUA&list=RD3vjkh-acmTE&index=10"   // <-- AQUÍ VA EL LINK DE YOUTUBE (World Vision - Novedades)
        ));

        // ---- TRABAJOS ----
        c.trabajos.add(new Entrada(
                "Salud, agua y educación",
                "PROGRAMA",
                "",
                "Salud comunitaria, agua potable, educación de calidad y protección frente a la violencia.",
                "https://www.youtube.com/watch?v=Io0fBr1XBUA&list=RD3vjkh-acmTE&index=10"   // <-- AQUÍ VA EL LINK DE YOUTUBE (World Vision - Trabajos)
        ));

        // ---- PERSONAS ----
        c.personas.add(new Entrada(
                "Equipo de la fundación",
                "PERSONAS",
                "",
                "Conoce a quienes hacen posible el trabajo de la fundación.",
                "https://www.youtube.com/watch?v=Io0fBr1XBUA&list=RD3vjkh-acmTE&index=10"   // <-- AQUÍ VA EL LINK DE YOUTUBE (World Vision - Personas)
        ));
        return c;
    }

    // =========================================================================
    //  CUALQUIER OTRA FUNDACIÓN (agregada luego en Supabase)
    // =========================================================================
    private static Contenido generico(String nombre, String programas) {
        Contenido c = new Contenido();
        c.novedades.add(new Entrada(
                "Próximamente",
                "NOVEDAD",
                "",
                "Pronto publicaremos las novedades de " + (nombre == null ? "esta fundación" : nombre) + ".",
                ""
        ));
        c.trabajos.add(new Entrada(
                "Programas",
                "PROGRAMA",
                "",
                (programas == null || programas.isEmpty())
                        ? "Programas integrales de apoyo y donaciones directas."
                        : programas,
                ""
        ));
        c.personas.add(new Entrada(
                "Equipo de la fundación",
                "PERSONAS",
                "",
                "Pronto conocerás a las personas de esta fundación.",
                ""
        ));
        return c;
    }

    // =========================================================================
    //  UTILIDADES DE YOUTUBE (no necesitas tocar esto)
    // =========================================================================
    private static final Pattern PATRON_YOUTUBE = Pattern.compile(
            "(?:youtu\\.be/|youtube\\.com/(?:watch\\?(?:.*&)?v=|embed/|shorts/|live/|v/))([A-Za-z0-9_-]{11})");

    /** Devuelve el ID de 11 caracteres del video, o null si el link no es válido / está vacío. */
    public static String extraerIdYoutube(String url) {
        if (url == null || url.trim().isEmpty()) return null;
        Matcher m = PATRON_YOUTUBE.matcher(url.trim());
        return m.find() ? m.group(1) : null;
    }

    /** Imagen de portada del video (la que muestra YouTube). */
    public static String miniaturaYoutube(String url) {
        String id = extraerIdYoutube(url);
        return id == null ? null : "https://img.youtube.com/vi/" + id + "/hqdefault.jpg";
    }
}
