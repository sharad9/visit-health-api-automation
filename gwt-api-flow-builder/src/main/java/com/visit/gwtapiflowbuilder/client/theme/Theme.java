package com.visit.gwtapiflowbuilder.client.theme;

public final class Theme {
    public static final String FONT_BASE = "\"Inter\", system-ui, sans-serif";
    public static final String FONT_MONO = "\"Fira Code\",\"Cascadia Code\",\"JetBrains Mono\",monospace";
    public static final String TOGGLE_LABEL_LIGHT = "☀ Light";
    public static final String TOGGLE_LABEL_DARK = "☽ Dark";
    public static final String FONT_SIZE_68 = "10px";
    public static final String FONT_SIZE_72 = "10.5px";
    public static final String FONT_SIZE_78 = "11px";
    public static final String FONT_SIZE_82 = "11.5px";
    public static final String FONT_SIZE_87 = "12px";
    public static final String FONT_SIZE_95 = "13px";
    public static final String FONT_SIZE_140 = "18px";
    public static final String FONT_WEIGHT_500 = "500";
    public static final String FONT_WEIGHT_600 = "600";
    public static final String FONT_WEIGHT_700 = "700";
    public static final String LETTER_SPACING_TIGHT = "-0.2px";
    public static final String LETTER_SPACING_WIDE = "0.8px";
    public static final String GAP_6 = "6px";
    public static final String GAP_8 = "8px";
    public static final String GAP_10 = "10px";
    public static final String PAD_SECTION_HDR = "8px 10px";
    public static final String PAD_SECTION_BODY = "10px";
    public static final String PAD_FORM_INNER = "10px";
    public static final String PAD_INPUT = "5px 8px";
    public static final String PAD_NAVBAR = "0 14px";
    public static final String PAD_PREVIEW_HDR = "8px 12px";
    public static final String PAD_JSON = "12px";
    public static final String HEIGHT_NAVBAR = "54px";
    private static final Palette LIGHT = new Palette(
            "#F6F7FB",
            "#FFFFFF",
            "#F9FAFC",
            "#E2E6ED",
            "#1F2933",
            "#6B7280",
            "#9AA5B1",
            "#FF6C37",
            "#FFF3EE",
            "#F7F8FB",
            "#111827",
            "#E8F5EC",
            "#157347",
            "#FDECEC",
            "#B91C1C",
            "#E44D3A",
            "#2563EB",
            "linear-gradient(135deg,#FF6C37,#FF8F5B)"
    );
    private static final Palette DARK = new Palette(
            "#1B1C20",
            "#1F2127",
            "#20232A",
            "#2E323B",
            "#E6E8EE",
            "#A0A7B4",
            "#7C8594",
            "#FF6C37",
            "rgba(255,108,55,0.12)",
            "#1B1C20",
            "#E6E8EE",
            "rgba(22,163,74,0.15)",
            "#6EE7B7",
            "rgba(239,68,68,0.18)",
            "#FCA5A5",
            "#F06A55",
            "#60A5FA",
            "linear-gradient(135deg,#FF6C37,#FF8F5B)"
    );
    public static boolean IS_DARK = false;
    public static String COLOR_BG = "#F6F7FB";
    public static String COLOR_PANEL = "#FFFFFF";
    public static String COLOR_SECTION = "#F9FAFC";
    public static String COLOR_BORDER = "#E2E6ED";
    public static String COLOR_TEXT = "#1F2933";
    public static String COLOR_MUTED = "#6B7280";
    public static String COLOR_MUTED_DIM = "#9AA5B1";
    public static String COLOR_PRIMARY = "#FF6C37";
    public static String COLOR_PRIMARY_LIGHT = "#FFF3EE";
    public static String COLOR_JSON_BG = "#F7F8FB";
    public static String COLOR_JSON_TEXT = "#111827";
    public static String COLOR_STATUS_BG = "#E8F5EC";
    public static String COLOR_STATUS_TEXT = "#157347";
    public static String COLOR_STATUS_ERROR_BG = "#FDECEC";
    public static String COLOR_STATUS_ERROR_TEXT = "#B91C1C";
    public static String COLOR_DANGER = "#E44D3A";
    public static String COLOR_TEMPLATE = "#2563EB";
    public static String GRADIENT_HEADER = "linear-gradient(135deg,#FF6C37,#FF8F5B)";

    private Theme() {
    }

    public static void setMode(boolean dark) {
        IS_DARK = dark;
        apply(dark ? DARK : LIGHT);
    }

    public static String toggleLabel() {
        return IS_DARK ? TOGGLE_LABEL_LIGHT : TOGGLE_LABEL_DARK;
    }

    private static void apply(Palette palette) {
        COLOR_BG = palette.bg;
        COLOR_PANEL = palette.panel;
        COLOR_SECTION = palette.section;
        COLOR_BORDER = palette.border;
        COLOR_TEXT = palette.text;
        COLOR_MUTED = palette.muted;
        COLOR_MUTED_DIM = palette.mutedDim;
        COLOR_PRIMARY = palette.primary;
        COLOR_PRIMARY_LIGHT = palette.primaryLight;
        COLOR_JSON_BG = palette.jsonBg;
        COLOR_JSON_TEXT = palette.jsonText;
        COLOR_STATUS_BG = palette.statusBg;
        COLOR_STATUS_TEXT = palette.statusText;
        COLOR_STATUS_ERROR_BG = palette.statusErrorBg;
        COLOR_STATUS_ERROR_TEXT = palette.statusErrorText;
        COLOR_DANGER = palette.danger;
        COLOR_TEMPLATE = palette.template;
        GRADIENT_HEADER = palette.headerGradient;
    }

    private static final class Palette {
        private final String bg;
        private final String panel;
        private final String section;
        private final String border;
        private final String text;
        private final String muted;
        private final String mutedDim;
        private final String primary;
        private final String primaryLight;
        private final String jsonBg;
        private final String jsonText;
        private final String statusBg;
        private final String statusText;
        private final String statusErrorBg;
        private final String statusErrorText;
        private final String danger;
        private final String template;
        private final String headerGradient;

        private Palette(String bg,
                        String panel,
                        String section,
                        String border,
                        String text,
                        String muted,
                        String mutedDim,
                        String primary,
                        String primaryLight,
                        String jsonBg,
                        String jsonText,
                        String statusBg,
                        String statusText,
                        String statusErrorBg,
                        String statusErrorText,
                        String danger,
                        String template,
                        String headerGradient) {
            this.bg = bg;
            this.panel = panel;
            this.section = section;
            this.border = border;
            this.text = text;
            this.muted = muted;
            this.mutedDim = mutedDim;
            this.primary = primary;
            this.primaryLight = primaryLight;
            this.jsonBg = jsonBg;
            this.jsonText = jsonText;
            this.statusBg = statusBg;
            this.statusText = statusText;
            this.statusErrorBg = statusErrorBg;
            this.statusErrorText = statusErrorText;
            this.danger = danger;
            this.template = template;
            this.headerGradient = headerGradient;
        }
    }
}
