export class CheckEntry {
    #uid;
    source;
    path;
    equals;
    exists;

    constructor(uid, source = 'status', path = '', equals = '', exists = false) {
        this.#uid = uid;
        this.source = source;
        this.path = path;
        this.equals = equals;
        this.exists = exists;
    }

    get uid() { return this.#uid; }
}

