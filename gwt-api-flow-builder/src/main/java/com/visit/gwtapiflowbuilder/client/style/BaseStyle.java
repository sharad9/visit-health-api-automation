package com.visit.gwtapiflowbuilder.client.style;

public final class BaseStyle {


    /* =========================
       KEYS
    ========================= */

    public static final class Key {

        private Key() {}

        public static final String WIDTH = "width";
        public static final String HEIGHT = "height";
        public static final String MIN_WIDTH = "minWidth";
        public static final String MIN_HEIGHT = "minHeight";
        public static final String MAX_WIDTH = "maxWidth";
        public static final String MAX_HEIGHT = "maxHeight";

        public static final String PADDING = "padding";
        public static final String PADDING_TOP = "paddingTop";
        public static final String PADDING_BOTTOM = "paddingBottom";
        public static final String PADDING_LEFT = "paddingLeft";
        public static final String PADDING_RIGHT = "paddingRight";

        public static final String MARGIN = "margin";
        public static final String MARGIN_TOP = "marginTop";
        public static final String MARGIN_BOTTOM = "marginBottom";
        public static final String MARGIN_LEFT = "marginLeft";
        public static final String MARGIN_RIGHT = "marginRight";

        public static final String BORDER = "border";
        public static final String BORDER_TOP = "borderTop";
        public static final String BORDER_BOTTOM = "borderBottom";
        public static final String BORDER_LEFT = "borderLeft";
        public static final String BORDER_RIGHT = "borderRight";
        public static final String BORDER_RADIUS = "borderRadius";

        public static final String BOX_SIZING = "boxSizing";
        public static final String BOX_SHADOW = "boxShadow";
        public static final String OUTLINE = "outline";

        public static final String DISPLAY = "display";
        public static final String POSITION = "position";

        public static final String TOP = "top";
        public static final String RIGHT = "right";
        public static final String BOTTOM = "bottom";
        public static final String LEFT = "left";

        public static final String Z_INDEX = "zIndex";

        public static final String OVERFLOW = "overflow";
        public static final String OVERFLOW_X = "overflowX";
        public static final String OVERFLOW_Y = "overflowY";

        public static final String FLOAT = "float";
        public static final String CLEAR = "clear";
        public static final String VISIBILITY = "visibility";
        public static final String CLIP_PATH = "clipPath";

        public static final String INSET = "inset";
        public static final String INSET_BLOCK = "insetBlock";
        public static final String INSET_INLINE = "insetInline";

        public static final String FLEX = "flex";
        public static final String FLEX_DIRECTION = "flexDirection";
        public static final String JUSTIFY_CONTENT = "justifyContent";
        public static final String ALIGN_ITEMS = "alignItems";
        public static final String ALIGN_CONTENT = "alignContent";
        public static final String ALIGN_SELF = "alignSelf";
        public static final String FLEX_WRAP = "flexWrap";
        public static final String FLEX_GROW = "flexGrow";
        public static final String FLEX_SHRINK = "flexShrink";
        public static final String ORDER = "order";
        public static final String GAP = "gap";

        public static final String GRID_AREA = "gridArea";
        public static final String GRID_TEMPLATE_AREAS = "gridTemplateAreas";
        public static final String GRID_TEMPLATE_COLUMNS = "gridTemplateColumns";
        public static final String GRID_TEMPLATE_ROWS = "gridTemplateRows";
        public static final String GRID_COLUMN_GAP = "columnGap";
        public static final String GRID_ROW_GAP = "rowGap";
        public static final String GRID_GAP = "gap";

        public static final String PLACE_ITEMS = "placeItems";
        public static final String ALIGN_TRACKS = "alignTracks";
        public static final String JUSTIFY_TRACKS = "justifyTracks";

        public static final String COLOR = "color";
        public static final String FONT_SIZE = "fontSize";
        public static final String FONT_WEIGHT = "fontWeight";
        public static final String FONT_FAMILY = "fontFamily";
        public static final String FONT_STYLE = "fontStyle";

        public static final String TEXT_ALIGN = "textAlign";
        public static final String TEXT_DECORATION = "textDecoration";
        public static final String LINE_HEIGHT = "lineHeight";
        public static final String LETTER_SPACING = "letterSpacing";
        public static final String WHITE_SPACE = "whiteSpace";
        public static final String WORD_BREAK = "wordBreak";

        public static final String LINE_CLAMP = "-webkit-line-clamp";

        public static final String TEXT_OVERFLOW = "textOverflow";
        public static final String OVERFLOW_WRAP = "overflowWrap";
        public static final String WORD_WRAP = "wordWrap";
        public static final String VERTICAL_ALIGN = "verticalAlign";
        public static final String CARET_COLOR = "caretColor";

        public static final String BACKGROUND_COLOR = "backgroundColor";
        public static final String BACKGROUND_IMAGE = "backgroundImage";
        public static final String BACKGROUND_SIZE = "backgroundSize";
        public static final String BACKGROUND_REPEAT = "backgroundRepeat";
        public static final String BACKGROUND_POSITION = "backgroundPosition";
        public static final String BACKDROP_FILTER = "backdropFilter";

        public static final String OPACITY = "opacity";
        public static final String TRANSFORM = "transform";
        public static final String TRANSITION = "transition";
        public static final String ANIMATION = "animation";
        public static final String FILTER = "filter";
        public static final String MIX_BLEND_MODE = "mixBlendMode";
        public static final String ISOLATION = "isolation";

        public static final String CURSOR = "cursor";
        public static final String USER_SELECT = "userSelect";
        public static final String POINTER_EVENTS = "pointerEvents";
        public static final String ASPECT_RATIO = "aspectRatio";
        public static final String TOUCH_ACTION = "touchAction";
        public static final String CONTENT = "content";
        public static final String TAB_INDEX = "tabIndex";

        public static final String SCROLL_BEHAVIOR = "scrollBehavior";
        public static final String SCROLL_SNAP_TYPE = "scrollSnapType";
        public static final String SCROLL_PADDING = "scrollPadding";

        public static final String OBJECT_FIT = "objectFit";
        public static final String OBJECT_POSITION = "objectPosition";

        public static final String LIST_STYLE = "listStyle";
        public static final String LIST_STYLE_TYPE = "listStyleType";
        public static final String LIST_STYLE_POSITION = "listStylePosition";

        public static final String DISPLAY_PRINT = "display";

        public static final String TEXT_TRANSFORM = "textTransform";
        public static final String TEXT_SHADOW = "textShadow";
        public static final String TEXT_INDENT = "textIndent";
        public static final String WORD_SPACING = "wordSpacing";

        public static final String BORDER_WIDTH = "borderWidth";
        public static final String BORDER_STYLE = "borderStyle";
        public static final String BORDER_COLOR = "borderColor";
        public static final String BORDER_COLLAPSE = "borderCollapse";
        public static final String BORDER_SPACING = "borderSpacing";
        public static final String BORDER_IMAGE = "borderImage";

        public static final String OUTLINE_WIDTH = "outlineWidth";
        public static final String OUTLINE_STYLE = "outlineStyle";
        public static final String OUTLINE_COLOR = "outlineColor";
        public static final String OUTLINE_OFFSET = "outlineOffset";

        public static final String FLEX_BASIS = "flexBasis";
        public static final String FLEX_FLOW = "flexFlow";

        public static final String GRID_COLUMN = "gridColumn";
        public static final String GRID_ROW = "gridRow";
        public static final String GRID_COLUMN_START = "gridColumnStart";
        public static final String GRID_COLUMN_END = "gridColumnEnd";
        public static final String GRID_ROW_START = "gridRowStart";
        public static final String GRID_ROW_END = "gridRowEnd";

        public static final String GRID_AUTO_COLUMNS = "gridAutoColumns";
        public static final String GRID_AUTO_ROWS = "gridAutoRows";
        public static final String GRID_AUTO_FLOW = "gridAutoFlow";

        public static final String JUSTIFY_ITEMS = "justifyItems";
        public static final String JUSTIFY_SELF = "justifySelf";
        public static final String PLACE_CONTENT = "placeContent";
        public static final String PLACE_SELF = "placeSelf";

        public static final String TRANSFORM_ORIGIN = "transformOrigin";
        public static final String TRANSFORM_STYLE = "transformStyle";

        public static final String PERSPECTIVE = "perspective";
        public static final String PERSPECTIVE_ORIGIN = "perspectiveOrigin";
        public static final String BACKFACE_VISIBILITY = "backfaceVisibility";

        public static final String BACKGROUND_ATTACHMENT = "backgroundAttachment";
        public static final String BACKGROUND_CLIP = "backgroundClip";
        public static final String BACKGROUND_ORIGIN = "backgroundOrigin";
        public static final String BACKGROUND_BLEND_MODE = "backgroundBlendMode";

        public static final String ANIMATION_NAME = "animationName";
        public static final String ANIMATION_DURATION = "animationDuration";
        public static final String ANIMATION_DELAY = "animationDelay";
        public static final String ANIMATION_TIMING_FUNCTION = "animationTimingFunction";
        public static final String ANIMATION_ITERATION_COUNT = "animationIterationCount";
        public static final String ANIMATION_DIRECTION = "animationDirection";
        public static final String ANIMATION_FILL_MODE = "animationFillMode";
        public static final String ANIMATION_PLAY_STATE = "animationPlayState";

        public static final String TRANSITION_PROPERTY = "transitionProperty";
        public static final String TRANSITION_DURATION = "transitionDuration";
        public static final String TRANSITION_DELAY = "transitionDelay";
        public static final String TRANSITION_TIMING_FUNCTION = "transitionTimingFunction";

        public static final String SCROLL_MARGIN = "scrollMargin";
        public static final String SCROLL_SNAP_ALIGN = "scrollSnapAlign";
        public static final String OVERSCROLL_BEHAVIOR = "overscrollBehavior";

        public static final String COLUMN_COUNT = "columnCount";
        public static final String COLUMN_WIDTH = "columnWidth";
        public static final String COLUMN_RULE = "columnRule";
        public static final String COLUMN_SPAN = "columnSpan";
        public static final String COLUMNS = "columns";

        public static final String RESIZE = "resize";
        public static final String APPEARANCE = "appearance";
        public static final String WILL_CHANGE = "willChange";
    }

    /* =========================
       VALUES
    ========================= */

    public static final class Value {

        private Value() {}

        public static final String AUTO = "auto";
        public static final String DEFAULT = "default";
        public static final String POINTER = "pointer";
        public static final String WAIT = "wait";
        public static final String TEXT = "text";
        public static final String MOVE = "move";
        public static final String NOT_ALLOWED = "not-allowed";
        public static final String CROSSHAIR = "crosshair";
        public static final String HELP = "help";
        public static final String PROGRESS = "progress";
        public static final String GRAB = "grab";
        public static final String GRABBING = "grabbing";
        public static final String NONE = "none";
        public static final String ALL_SCROLL = "all-scroll";
        public static final String COL_RESIZE = "col-resize";
        public static final String ROW_RESIZE = "row-resize";
        public static final String VERTICAL_TEXT = "vertical-text";
        public static final String CONTEXT_MENU = "context-menu";
        public static final String CELL = "cell";
        public static final String ZOOM_IN = "zoom-in";
        public static final String ZOOM_OUT = "zoom-out";
        public static final String ALIAS = "alias";
        public static final String COPY = "copy";
        public static final String NO_DROP = "no-drop";
        public static final String OPEN_HAND = "open-hand";
        public static final String CLOSED_HAND = "closed-hand";

        public static final String BLOCK = "block";
        public static final String INLINE = "inline";
        public static final String INLINE_BLOCK = "inline-block";
        public static final String FLEX = "flex";
        public static final String INLINE_FLEX = "inline-flex";
        public static final String GRID = "grid";
        public static final String INLINE_GRID = "inline-grid";
        public static final String TABLE = "table";
        public static final String TABLE_ROW = "table-row";
        public static final String TABLE_CELL = "table-cell";
        public static final String LIST_ITEM = "list-item";
        public static final String RUN_IN = "run-in";
        public static final String FLOW_ROOT = "flow-root";

        public static final String INITIAL = "initial";
        public static final String INHERIT = "inherit";
        public static final String REVERT = "revert";
        public static final String UNSET = "unset";

        public static final String TRANSPARENT = "transparent";

        public static final String BLACK = "black";
        public static final String WHITE = "white";
        public static final String RED = "red";
        public static final String GREEN = "green";
        public static final String BLUE = "blue";
        public static final String YELLOW = "yellow";
        public static final String CYAN = "cyan";
        public static final String MAGENTA = "magenta";
        public static final String GRAY = "gray";
        public static final String LIGHTGRAY = "lightgray";
        public static final String DARKGRAY = "darkgray";
        public static final String ORANGE = "orange";
        public static final String PURPLE = "purple";
        public static final String BROWN = "brown";
        public static final String PINK = "pink";
        public static final String LIME = "lime";
        public static final String NAVY = "navy";
        public static final String TEAL = "teal";
        public static final String OLIVE = "olive";
        public static final String MAROON = "maroon";
        public static final String SILVER = "silver";
        public static final String GOLD = "gold";
        public static final String CORAL = "coral";
        public static final String TOMATO = "tomato";
        public static final String SALMON = "salmon";

        public static final String STATIC = "static";
        public static final String RELATIVE = "relative";
        public static final String ABSOLUTE = "absolute";
        public static final String FIXED = "fixed";
        public static final String STICKY = "sticky";

        public static final String VISIBLE = "visible";
        public static final String HIDDEN = "hidden";
        public static final String SCROLL = "scroll";
        public static final String CLIP = "clip";

        public static final String ROW = "row";
        public static final String ROW_REVERSE = "row-reverse";
        public static final String COLUMN = "column";
        public static final String COLUMN_REVERSE = "column-reverse";

        public static final String FLEX_START = "flex-start";
        public static final String FLEX_END = "flex-end";
        public static final String CENTER = "center";
        public static final String SPACE_BETWEEN = "space-between";
        public static final String SPACE_AROUND = "space-around";
        public static final String SPACE_EVENLY = "space-evenly";

        public static final String BASELINE = "baseline";
        public static final String STRETCH = "stretch";

        public static final String NORMAL = "normal";
        public static final String BOLD = "bold";
        public static final String BOLDER = "bolder";
        public static final String LIGHTER = "lighter";

        public static final String LEFT = "left";
        public static final String RIGHT = "right";
        public static final String JUSTIFY = "justify";
        public static final String START = "start";
        public static final String END = "end";

        public static final String WRAP = "wrap";
        public static final String NOWRAP = "nowrap";

        public static final String PRE = "pre";
        public static final String PRE_LINE = "pre-line";
        public static final String PRE_WRAP = "pre-wrap";
        public static final String BREAK_SPACES = "break-spaces";

        public static final String DOTTED = "dotted";
        public static final String DASHED = "dashed";
        public static final String SOLID = "solid";
        public static final String DOUBLE = "double";
        public static final String GROOVE = "groove";
        public static final String RIDGE = "ridge";
        public static final String INSET = "inset";
        public static final String OUTSET = "outset";

        public static final String CONTENT_BOX = "content-box";
        public static final String BORDER_BOX = "border-box";

        public static final String SUB = "sub";
        public static final String SUPER = "super";
        public static final String TEXT_TOP = "text-top";
        public static final String TEXT_BOTTOM = "text-bottom";
        public static final String MIDDLE = "middle";
        public static final String TOP = "top";
        public static final String BOTTOM = "bottom";

        public static final String DISC = "disc";
        public static final String CIRCLE = "circle";
        public static final String SQUARE = "square";
        public static final String DECIMAL = "decimal";
        public static final String DECIMAL_LEADING_ZERO = "decimal-leading-zero";
        public static final String LOWER_ROMAN = "lower-roman";
        public static final String UPPER_ROMAN = "upper-roman";
        public static final String LOWER_GREEK = "lower-greek";
        public static final String LOWER_ALPHA = "lower-alpha";
        public static final String UPPER_ALPHA = "upper-alpha";

        public static final String BREAK_WORD = "break-word";

        public static final String UNDERLINE = "underline";
        public static final String OVERLINE = "overline";
        public static final String LINE_THROUGH = "line-through";
        public static final String BLINK = "blink";

        public static final String CONTAIN = "contain";

        public static final String ALL = "all";
        public static final String ELEMENT = "element";

        public static final String VISIBLE_PAINTED = "visiblePainted";
        public static final String VISIBLE_FILL = "visibleFill";
        public static final String VISIBLE_STROKE = "visibleStroke";
        public static final String PAINTED = "painted";
        public static final String FILL = "fill";
        public static final String STROKE = "stroke";
    }
}
