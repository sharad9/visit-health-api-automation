export class Extract {
    body;
    headers;

    constructor(body = [], headers = []) {
        this.body = body;
        this.headers = headers;
    }
}

