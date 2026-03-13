export class RetryConfiguration {
    enabled;
    count;
    delay;

    constructor(enabled = false, count = 3, delay = 1000) {
        this.enabled = enabled;
        this.count = count;
        this.delay = delay;
    }
}

