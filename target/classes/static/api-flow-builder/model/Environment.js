import { Default } from './Default.js';

export class Environment {
    id;
    name;
    baseUrl;
    defaults;
    variables;

    constructor(id, name = 'Environment', baseUrl = '', defaults = new Default(), variables = []) {
        this.id = id;
        this.name = name;
        this.baseUrl = baseUrl;
        this.defaults = defaults;
        this.variables = variables;
    }
}
