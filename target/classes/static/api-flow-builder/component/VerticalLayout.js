import { Div } from './Div.js';
import { Style } from './Style.js'; 

export class VerticalLayout extends Div {
    constructor() {
        super();
        this.setStyle(Style.Key.DISPLAY, Style.Value.FLEX)
            .setStyle(Style.Key.FLEX_DIRECTION, Style.Value.COLUMN)
            .setStyle(Style.Key.FLEX_WRAP, Style.Value.NOWRAP)
    }
}
