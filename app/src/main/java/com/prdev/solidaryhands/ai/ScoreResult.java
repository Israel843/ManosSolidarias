package com.prdev.solidaryhands.ai;

/**
 * Resultado de la evaluación de una donación (todas las puntuaciones van de 0 a 10).
 */
public class ScoreResult {

    /** Calidad de las fotos (nitidez + iluminación). */
    public float calidadFotos;

    /** Puntuación del objeto (reconocimiento de la red neuronal + condición declarada). */
    public float puntuacionObjeto;

    /** Puntuación final combinada. */
    public float puntuacionFinal;

    /** Objeto que la red neuronal cree haber visto (ej. "Polera"). */
    public String objetoDetectado = "";

    /** Confianza (0..1) del objeto detectado. */
    public float confianzaObjeto;

    /** true si la red neuronal puede reconocer la categoría que eligió el usuario. */
    public boolean categoriaReconocible;

    /** true si lo detectado coincide con la categoría elegida por el usuario. */
    public boolean coincideCategoria;

    /** Motor usado: "Red Neuronal TFLite" o "Respaldo ML Kit". */
    public String motor = "";

    public long tiempoMs;

    /** 2 = Aprobado, 1 = Requiere revisión, 0 = No aprobado. */
    public int nivel;

    public String detalleCalidad = "";
    public String detalleObjeto = "";
}
