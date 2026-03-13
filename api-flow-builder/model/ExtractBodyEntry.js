export class ExtractBodyEntry {
    #uid;
    key;
    path;

    constructor(uid, key = '', path = '') {
        this.#uid = uid;
        this.key = key;
        this.path = path;
    }

    get uid() { return this.#uid; }
}

