export class Attribute {

    static #KeyEnum = class AttributeKey {
        #value;
        constructor(value) { this.#value = value; Object.freeze(this); }
        toString() { return this.#value; }
    };

    static #ValueEnum = class AttributeValue {
        #value;
        constructor(value) { this.#value = value; Object.freeze(this); }
        toString() { return this.#value; }
    };

    static Key = Object.freeze({
        ID:               new this.#KeyEnum('id'),
        CLASS:            new this.#KeyEnum('class'),
        TITLE:            new this.#KeyEnum('title'),
        TYPE:             new this.#KeyEnum('type'),
        NAME:             new this.#KeyEnum('name'),
        VALUE:            new this.#KeyEnum('value'),
        HREF:             new this.#KeyEnum('href'),
        SRC:              new this.#KeyEnum('src'),
        ALT:              new this.#KeyEnum('alt'),
        ROLE:             new this.#KeyEnum('role'),
        TABINDEX:         new this.#KeyEnum('tabindex'),
        ARIA_LABEL:       new this.#KeyEnum('aria-label'),
        ARIA_HIDDEN:      new this.#KeyEnum('aria-hidden'),
        ARIA_EXPANDED:    new this.#KeyEnum('aria-expanded'),
        DATA_ID:          new this.#KeyEnum('data-id'),
        DATA_ROLE:        new this.#KeyEnum('data-role'),
        TARGET:           new this.#KeyEnum('target'),
        REL:              new this.#KeyEnum('rel'),
        DISABLED:         new this.#KeyEnum('disabled'),
        CHECKED:          new this.#KeyEnum('checked'),
        SELECTED:         new this.#KeyEnum('selected'),
        PLACEHOLDER:      new this.#KeyEnum('placeholder'),
        REQUIRED:         new this.#KeyEnum('required'),
        READONLY:         new this.#KeyEnum('readonly'),
        FOR:              new this.#KeyEnum('for'),
        FORM:             new this.#KeyEnum('form'),
        METHOD:           new this.#KeyEnum('method'),
        ACTION:           new this.#KeyEnum('action'),
        ENCTYPE:          new this.#KeyEnum('enctype'),
    });

    static Value = Object.freeze({
        TRUE:             new this.#ValueEnum('true'),
        FALSE:            new this.#ValueEnum('false'),

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

        DIALOG:           new this.#ValueEnum('dialog'),
        ALERT:            new this.#ValueEnum('alert'),
        TAB:              new this.#ValueEnum('tab'),
        TABLIST:          new this.#ValueEnum('tablist'),
        TABPANEL:         new this.#ValueEnum('tabpanel'),
        GROUP:            new this.#ValueEnum('group'),
        LIST:             new this.#ValueEnum('list'),
        LISTITEM:         new this.#ValueEnum('listitem'),
        LINK:             new this.#ValueEnum('link'),
        MENU:             new this.#ValueEnum('menu'),
        MENUITEM:         new this.#ValueEnum('menuitem'),
        PROGRESSBAR:      new this.#ValueEnum('progressbar'),
        SLIDER:           new this.#ValueEnum('slider'),
    });

    #component;
    #attributes = {};

    constructor(component) {
        this.#component = component;
    }

    set(key, value) {
        const element = this.#component.getElement();
        if (value === null || value === undefined) {
            delete this.#attributes[key];
            element.removeAttribute(key.toString());
        } else {
            this.#attributes[key] = value;
            element.setAttribute(key.toString(), value.toString());
        }
        return this;
    }

    get(property) {
        return this.#component.getElement().getAttribute(property.toString());
    }

    remove(property) {
        return this.set(property, null);
    }
}
