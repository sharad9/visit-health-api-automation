import { Div } from './Div.js';
import { Style } from './Style.js';

export class HorizontalLayout extends Div {
    constructor() {
        super();
        this.setStyle(Style.Key.DISPLAY, Style.Value.FLEX)
            .setStyle(Style.Key.FLEX_DIRECTION, Style.Value.ROW)
            .setStyle(Style.Key.FLEX_WRAP, Style.Value.NOWRAP)
    }
}
