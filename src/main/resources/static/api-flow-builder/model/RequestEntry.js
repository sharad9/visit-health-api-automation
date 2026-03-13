export class RequestEntry {
    #uid;
    key;
    value;

    constructor(uid, key = '', value = '') {
        this.#uid = uid;
        this.key = key;
        this.value = value;
    }

    get uid() { return this.#uid; }
}

