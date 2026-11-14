package com.prdev.solidaryhands.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CatalogConfig {

    public static class DynamicFieldSpec {
        public final String key;
        public final String label;
        public final List<String> options;

        public DynamicFieldSpec(String key, String label, List<String> options) {
            this.key = key;
            this.label = label;
            this.options = options;
        }
    }

    public static class CatalogItem {
        public final String key;
        public final String displayName;
        public final String category;
        public final List<DynamicFieldSpec> dynamicFields;

        public CatalogItem(
                String key,
                String displayName,
                String category,
                List<DynamicFieldSpec> dynamicFields
        ) {
            this.key = key;
            this.displayName = displayName;
            this.category = category;
            this.dynamicFields = dynamicFields;
        }
    }

    public static final List<String> ALL_CATEGORIES = Arrays.asList(
            "Todas",
            "Ropa y Calzado",
            "Electrodomésticos",
            "Hogar / Muebles",
            "Educación",
            "Recreación"
    );

    public static final List<String> COLOR_OPTIONS = Arrays.asList(
            "Negro", "Blanco", "Gris", "Azul", "Rojo",
            "Verde", "Amarillo", "Naranja", "Morado", "Rosado", "Café", "Multicolor"
    );

    public static final List<String> CONDITION_OPTIONS = Arrays.asList(
            "Nuevo", "Muy bueno", "Usado", "Para reparar"
    );

    private static final Map<String, CatalogItem> ITEMS = new LinkedHashMap<>();

    static {
        DynamicFieldSpec tallaRopa = new DynamicFieldSpec(
                "talla", "Talla", Arrays.asList("XS", "S", "M", "L", "XL", "Niño")
        );
        DynamicFieldSpec funciona = new DynamicFieldSpec(
                "funciona", "¿Funciona?", Arrays.asList("Sí, funciona", "Con detalle", "Para repuesto")
        );

        register(new CatalogItem("01_polera", "Polera", "Ropa y Calzado", Arrays.asList(
                tallaRopa,
                new DynamicFieldSpec("manga", "Manga", Arrays.asList("Corta", "Larga", "Sin manga"))
        )));
        register(new CatalogItem("02_camisa", "Camisa", "Ropa y Calzado", Arrays.asList(
                tallaRopa,
                new DynamicFieldSpec("estilo", "Estilo", Arrays.asList("Casual", "Formal"))
        )));
        register(new CatalogItem("03_poleron_chompa", "Polerón / Chompa", "Ropa y Calzado", Arrays.asList(
                tallaRopa,
                new DynamicFieldSpec("abrigo", "Grosor", Arrays.asList("Ligero", "Grueso / Invierno"))
        )));
        register(new CatalogItem("04_short", "Short", "Ropa y Calzado", Arrays.asList(
                tallaRopa,
                new DynamicFieldSpec("uso", "Tipo", Arrays.asList("Deportivo", "Casual / Jean"))
        )));
        register(new CatalogItem("05_pantalon", "Pantalón / Jean", "Ropa y Calzado", Arrays.asList(
                new DynamicFieldSpec("talla", "Talla aprox.", Arrays.asList("28-30", "32-34", "36+", "Niño")),
                new DynamicFieldSpec("tipo", "Tipo", Arrays.asList("Jean", "Tela / Vestir", "Buzo"))
        )));
        register(new CatalogItem("06_zapatillas", "Zapatillas", "Ropa y Calzado", Arrays.asList(
                new DynamicFieldSpec("talla", "Talla aprox.", Arrays.asList("Niño (<34)", "35-37", "38-40", "41-43", "44+")),
                new DynamicFieldSpec("par", "Par completo", Arrays.asList("Sí", "Con desgaste en suela"))
        )));
        register(new CatalogItem("07_licuadora", "Licuadora", "Electrodomésticos", Arrays.asList(
                funciona,
                new DynamicFieldSpec("vaso", "Vaso", Arrays.asList("Vidrio", "Plástico", "Sin vaso"))
        )));
        register(new CatalogItem("08_microondas", "Microondas", "Electrodomésticos", Arrays.asList(
                funciona,
                new DynamicFieldSpec("plato", "Plato giratorio", Arrays.asList("Incluido", "Falta plato"))
        )));
        register(new CatalogItem("09_plancha", "Plancha", "Electrodomésticos", Arrays.asList(
                funciona,
                new DynamicFieldSpec("tipo", "Tipo", Arrays.asList("A vapor", "Seca"))
        )));
        register(new CatalogItem("10_silla", "Silla", "Hogar / Muebles", Arrays.asList(
                new DynamicFieldSpec("material", "Material", Arrays.asList("Madera", "Plástico", "Metal / Oficina")),
                new DynamicFieldSpec("firmeza", "Estabilidad", Arrays.asList("Firme", "Requiere ajuste"))
        )));
        register(new CatalogItem("11_mochila", "Mochila", "Educación", Arrays.asList(
                new DynamicFieldSpec("tamano", "Tamaño", Arrays.asList("Escolar", "Grande / Viaje")),
                new DynamicFieldSpec("cierres", "Cierres", Arrays.asList("Funcionan bien", "Un cierre con falla"))
        )));
        register(new CatalogItem("12_juguete", "Juguete / Peluche", "Recreación", Arrays.asList(
                new DynamicFieldSpec("edad", "Edad recomendada", Arrays.asList("0-3 años", "4-7 años", "8+ años")),
                new DynamicFieldSpec("piezas", "Piezas", Arrays.asList("Completas", "Incompletas"))
        )));
    }

    private CatalogConfig() {}

    private static void register(CatalogItem item) {
        ITEMS.put(item.key, item);
    }

    public static List<CatalogItem> getAllItems() {
        return new ArrayList<>(ITEMS.values());
    }

    public static CatalogItem getByKey(String key) {
        CatalogItem found = ITEMS.get(key);
        if (found != null) {
            return found;
        }
        return ITEMS.get("01_polera");
    }

    public static String buildSuggestedTitle(CatalogItem item, String color, String primaryAttr) {
        StringBuilder sb = new StringBuilder(item.displayName);
        if (color != null && !color.isEmpty() && !color.equals("Multicolor")) {
            sb.append(" ").append(color.toLowerCase());
        }
        if (primaryAttr != null && !primaryAttr.isEmpty()) {
            sb.append(" · ").append(primaryAttr);
        }
        return sb.toString();
    }
}
