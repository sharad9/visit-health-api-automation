export class Property {

    static #KeyEnum = class PropertyKey {
        #value;
        constructor(value) { this.#value = value; Object.freeze(this); }
        toString() { return this.#value; }
    };

    static #ValueEnum = class PropertyValue {
        #value;
        constructor(value) { this.#value = value; Object.freeze(this); }
        toString() { return this.#value; }
    };

    static Key = Object.freeze({
        ID:               new this.#KeyEnum('id'),
        CLASS_NAME:       new this.#KeyEnum('className'),
        TITLE:            new this.#KeyEnum('title'),
        TYPE:             new this.#KeyEnum('type'),
        NAME:             new this.#KeyEnum('name'),
        VALUE:            new this.#KeyEnum('value'),
        HREF:             new this.#KeyEnum('href'),
        SRC:              new this.#KeyEnum('src'),
        ALT:              new this.#KeyEnum('alt'),
        ROLE:             new this.#KeyEnum('role'),
        TAB_INDEX:        new this.#KeyEnum('tabIndex'),
        ARIA_LABEL:       new this.#KeyEnum('ariaLabel'),
        ARIA_HIDDEN:      new this.#KeyEnum('ariaHidden'),
        ARIA_EXPANDED:    new this.#KeyEnum('ariaExpanded'),
        DATASET:          new this.#KeyEnum('dataset'),
        TARGET:           new this.#KeyEnum('target'),
        REL:              new this.#KeyEnum('rel'),
        DISABLED:         new this.#KeyEnum('disabled'),
        CHECKED:          new this.#KeyEnum('checked'),
        SELECTED:         new this.#KeyEnum('selected'),
        PLACEHOLDER:      new this.#KeyEnum('placeholder'),
        REQUIRED:         new this.#KeyEnum('required'),
        READ_ONLY:        new this.#KeyEnum('readOnly'),
        HTML_FOR:         new this.#KeyEnum('htmlFor'),
        FORM:             new this.#KeyEnum('form'),
        METHOD:           new this.#KeyEnum('method'),
        ACTION:           new this.#KeyEnum('action'),
        ENCTYPE:          new this.#KeyEnum('enctype'),
        INNER_TEXT:       new this.#KeyEnum('innerText'),
        TEXT_CONTENT:     new this.#KeyEnum('textContent'),
        INNER_HTML:       new this.#KeyEnum('innerHTML'),
    });

    static Value = Object.freeze({
        TRUE:             true,
        FALSE:            false,

        BUTTON:           new this.#ValueEnum('button'),
        SUBMIT:           new this.#ValueEnum('submit'),
        RESET:            new this.#ValueEnum('reset'),
        TEXT:             new this.#ValueEnum('text'),
        CHECKBOX:         new this.#ValueEnum('checkbox'),
        RADIO:            new this.#ValueEnum('radio'),
        EMAIL:            new this.#ValueEnum('email'),
        PASSWORD:         new this.#ValueEnum('password'),
        NUMBER:           new this.#ValueEnum('number'),
        FILE:             new this.#ValueEnum('file'),
        HIDDEN:           new this.#ValueEnum('hidden'),
        IMAGE:            new this.#ValueEnum('image'),
        SEARCH:           new this.#ValueEnum('search'),
        TEL:              new this.#ValueEnum('tel'),
        URL:              new this.#ValueEnum('url'),
        DATE:             new this.#ValueEnum('date'),
        TIME:             new this.#ValueEnum('time'),
        DATETIME_LOCAL:   new this.#ValueEnum('datetime-local'),
        MONTH:            new this.#ValueEnum('month'),
        WEEK:             new this.#ValueEnum('week'),
        RANGE:            new this.#ValueEnum('range'),
        COLOR:            new this.#ValueEnum('color'),
    });

    #component;
    #property = {};

    constructor(component) {
        this.#component = component;
    }

    set(key, value) {
        const element = this.#component.getElement();
        if (value === null || value === undefined) {
            delete this.#property[key];
            element[key.toString()] = '';
        } else {
            this.#property[key] = value;
            element[key.toString()] = (value && typeof value === 'object') ? value.toString() : value;
        }
        return this;
    }

    get(property) {
        return this.#component.getElement()[property.toString()];
    }

    remove(property) {
        return this.set(property, null);
    }
}
