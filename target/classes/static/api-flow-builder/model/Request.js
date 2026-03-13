import { RetryConfiguration } from './RetryConfiguration.js';

export class Request {
    method;
    path;
    timeout;
    retry;
    headers;
    body;
    variables;

    constructor(method = 'GET', path = '', timeout = null, retry = new RetryConfiguration(), headers = [], body = [], variables = []) {
        this.method = method;
        this.path = path;
        this.timeout = timeout;
        this.retry = retry;
        this.headers = headers;
        this.body = body;
        this.variables = variables;
    }
}
