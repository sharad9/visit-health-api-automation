import { Request } from './Request.js';
import { Extract } from './Extract.js';

export class Step {
    #uid;
    id;
    dependsOn;
    req;
    extract;
    check;

    constructor(uid, id = '', dependsOn = [], req = new Request(), extract = new Extract(), check = []) {
        this.#uid = uid;
        this.id = id;
        this.dependsOn = dependsOn;
        this.req = req;
        this.extract = extract;
        this.check = check;
    }

    get uid() { return this.#uid; }
}

