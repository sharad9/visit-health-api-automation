import { RetryConfiguration } from './RetryConfiguration.js';

export class Default {
    timeout;
    retry;

    constructor(timeout = 10000, retry = new RetryConfiguration(false, 2, 500)) {
        this.timeout = timeout;
        this.retry = retry;
    }
}

