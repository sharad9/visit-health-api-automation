export class Memory {
    #state;
    #listeners;
    #storageKey;

    constructor(initialState = {}, storageKey = 'app_memory') {
        this.#storageKey = storageKey;
        this.#listeners = new Map();
        this.#state = { ...initialState, ...this.#load() };
    }

    #load() {
        try {
            const raw = localStorage.getItem(this.#storageKey);
            return raw ? JSON.parse(raw) : {};
        } catch {
            return {};
        }
    }

    #save() {
        try {
            localStorage.setItem(this.#storageKey, JSON.stringify(this.#state));
        } catch {
            // storage unavailable or quota exceeded
        }
    }

    getState() {
        return { ...this.#state };
    }

    setState(updates) {
        const changed = Object.keys(updates).filter(
            key => this.#state[key] !== updates[key]
        );
        if (changed.length === 0) return;

        this.#state = { ...this.#state, ...updates };
        this.#save();

        changed.forEach(key => {
            if (this.#listeners.has(key)) {
                this.#listeners.get(key).forEach(cb => cb(this.#state[key], this.getState()));
            }
        });
    }

    clearStorage() {
        localStorage.removeItem(this.#storageKey);
    }

    subscribe(key, callback) {
        if (!this.#listeners.has(key)) {
            this.#listeners.set(key, []);
        }
        this.#listeners.get(key).push(callback);
    }

    unsubscribe(key, callback) {
        if (!this.#listeners.has(key)) return;
        this.#listeners.set(key, this.#listeners.get(key).filter(cb => cb !== callback));
    }
}
