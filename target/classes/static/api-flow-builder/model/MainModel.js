import { Memory } from '../memory/Memory.js';
import { Meta } from './Meta.js';
import { Default } from './Default.js';
import { Environment } from './Environment.js';
import { RetryConfiguration } from './RetryConfiguration.js';
import { Input } from './Input.js';
import { RequestEntry } from './RequestEntry.js';
import { ExtractBodyEntry } from './ExtractBodyEntry.js';
import { ExtractHeaderEntry } from './ExtractHeaderEntry.js';
import { CheckEntry } from './CheckEntry.js';
import { Request } from './Request.js';
import { Extract } from './Extract.js';
import { Step } from './Step.js';
import { ENVIRONMENT, INPUT, META } from '../Config.js';

export class MainModel {
    #counter = 0;
    #meta;
    #envs;
    #activeEnvId;
    #inputs;
    #steps;
    #store;

    constructor() {
        this.#meta = new Meta(META.id, META.version);
        const initialEnv = new Environment(String(this.#nextId()), 'Default', ENVIRONMENT.baseUrl, new Default());
        this.#envs = [initialEnv];
        this.#activeEnvId = initialEnv.id;
        this.#inputs = INPUT.map(input => new Input(this.#nextId(), input.key, input.value));
        this.#steps = [];
        this.#store = new Memory({ revision: 0 }, 'api_flow_builder');
        const saved = this.#store.getState().json;
        if (saved) this.importJson(saved);
    }

    #nextId() { return ++this.#counter; }

    createInput(key = '', value = '') {
        return new Input(this.#nextId(), key, value);
    }
    createRequestEntry(key = '', value = '') {
        return new RequestEntry(this.#nextId(), key, value);
    }
    createEnvironment(name = '', baseUrl = '', defaults = new Default(), variables = []) {
        const envName = name || `Environment ${this.#envs.length + 1}`;
        return new Environment(String(this.#nextId()), envName, baseUrl, defaults, variables);
    }
    createExtractBodyEntry(key = '', path = '') {
        return new ExtractBodyEntry(this.#nextId(), key, path);
    }
    createExtractHeaderEntry(key = '', value = '') {
        return new ExtractHeaderEntry(this.#nextId(), key, value);
    }
    createCheckEntry(source = 'status', path = '', equals = '', exists = false) {
        return new CheckEntry(this.#nextId(), source, path, equals, exists);
    }
    createStep() {
        return new Step(
            this.#nextId(), '', [],
            new Request('GET', '', null, new RetryConfiguration()),
            new Extract(),
            [],
        );
    }

    get meta() { return this.#meta; }
    get env() { return this.#envs.find(env => env.id === this.#activeEnvId) || this.#envs[0]; }
    get envs() { return this.#envs; }
    get activeEnvId() { return this.#activeEnvId; }
    get inputs() { return this.#inputs; }
    get steps() { return this.#steps; }
    get store() { return this.#store; }

    set envs(list) { this.#envs = list; }
    set activeEnvId(id) { this.#activeEnvId = id; }
    set inputs(list) { this.#inputs = list; }
    set steps(list) { this.#steps = list; }

    commit() {
        this.#store.setState({
            revision: this.#store.getState().revision + 1,
            json: this.buildJson(),
        });
    }

    buildJson() {
        const output = {
            metadata: { identifier: this.#meta.id, version: this.#meta.version },
            environment: (() => {
                const envJson = {
                    defaults: {
                        timeoutMilliseconds: +this.env?.defaults?.timeout,
                        retryPolicy: {
                            maximumAttempts: +this.env?.defaults?.retry?.count,
                            delayMilliseconds: +this.env?.defaults?.retry?.delay,
                        },
                    },
                };
                if (this.env?.baseUrl) envJson.baseUrl = this.env.baseUrl;
                return envJson;
            })(),
            environments: {
                activeEnvironmentId: this.#activeEnvId,
                environmentItems: this.#envs.map(env => {
                    const envJson = {
                        environmentId: env.id,
                        environmentName: env.name,
                        defaults: {
                            timeoutMilliseconds: +env.defaults.timeout,
                            retryPolicy: {
                                maximumAttempts: +env.defaults.retry.count,
                                delayMilliseconds: +env.defaults.retry.delay,
                            },
                        },
                    };
                    if (env.baseUrl) envJson.baseUrl = env.baseUrl;
                    const validEnvVars = env.variables.filter(v => v.key);
                    if (validEnvVars.length) {
                        envJson.environmentVariables = {};
                        validEnvVars.forEach(v => { envJson.environmentVariables[v.key] = v.value; });
                    }
                    return envJson;
                }),
            },
        };

        const validInputs = this.#inputs.filter(i => i.key.trim());
        if (validInputs.length) {
            output.globalInputs = {};
            validInputs.forEach(i => { output.globalInputs[i.key] = i.value; });
        }

        output.steps = this.#steps.map(step => {
            const stepJson = { stepIdentifier: step.id };
            if (step.dependsOn.length) stepJson.dependsOnStepIdentifiers = [...step.dependsOn];

            const reqJson = { httpMethod: step.req.method, requestUrl: step.req.path };
            if (step.req.timeout) reqJson.timeoutMilliseconds = +step.req.timeout;
            if (step.req.retry.enabled) {
                reqJson.retryPolicy = {
                    maximumAttempts: +step.req.retry.count,
                    delayMilliseconds: +step.req.retry.delay,
                };
            }

            const validHeaders = step.req.headers.filter(h => h.key);
            if (validHeaders.length) {
                reqJson.headers = {};
                validHeaders.forEach(h => { reqJson.headers[h.key] = h.value; });
            }
            const validVariables = (step.req.variables ?? []).filter(v => v.key);
            if (validVariables.length) {
                reqJson.requestVariables = {};
                validVariables.forEach(v => { reqJson.requestVariables[v.key] = v.value; });
            }
            const validBody = step.req.body.filter(b => b.key);
            if (validBody.length) {
                reqJson.body = {};
                validBody.forEach(b => { reqJson.body[b.key] = MainModel.#parseVal(b.value); });
            }
            stepJson.request = reqJson;

            const extractJson = {};
            const validExtractBody = step.extract.body.filter(e => e.key);
            if (validExtractBody.length) {
                extractJson.bodyJsonPaths = {};
                validExtractBody.forEach(e => { extractJson.bodyJsonPaths[e.key] = e.path; });
            }
            const validExtractHeaders = step.extract.headers.filter(e => e.key);
            if (validExtractHeaders.length) {
                extractJson.headerValues = {};
                validExtractHeaders.forEach(e => { extractJson.headerValues[e.key] = e.value; });
            }
            if (Object.keys(extractJson).length) stepJson.extraction = extractJson;

            const validChecks = step.check.filter(c => c.source);
            if (validChecks.length) {
                stepJson.checks = validChecks.map(c => {
                    const checkJson = { source: c.source };
                    if (c.path) checkJson.jsonPath = c.path;
                    if (c.equals !== '') checkJson.equals = MainModel.#parseVal(c.equals);
                    if (c.exists) checkJson.exists = true;
                    return checkJson;
                });
            }

            return stepJson;
        });

        return output;
    }

    importJson(raw) {
        try {
            const json = typeof raw === 'string' ? JSON.parse(raw) : raw;

            if (json.metadata || json.meta) {
                const meta = json.metadata ?? json.meta ?? {};
                this.#meta.id = meta.identifier ?? meta.id ?? this.#meta.id;
                this.#meta.version = meta.version ?? this.#meta.version;
            }

            if (json.environments?.environmentItems?.length || json.envs?.items?.length) {
                const items = json.environments?.environmentItems ?? json.envs?.items ?? [];
                this.#envs = items.map(env => new Environment(
                    String(env.environmentId ?? env.id ?? this.#nextId()),
                    env.environmentName ?? env.name ?? 'Environment',
                    env.baseUrl ?? '',
                    new Default(
                        env.defaults?.timeoutMilliseconds ?? env.defaults?.timeout ?? new Default().timeout,
                        new RetryConfiguration(
                            false,
                            env.defaults?.retryPolicy?.maximumAttempts ?? env.defaults?.retry?.count ?? new Default().retry.count,
                            env.defaults?.retryPolicy?.delayMilliseconds ?? env.defaults?.retry?.delay ?? new Default().retry.delay,
                        ),
                    ),
                    Object.entries(env.environmentVariables ?? env.variables ?? {}).map(([k, v]) => new RequestEntry(this.#nextId(), k, String(v))),
                ));
                this.#activeEnvId = String(json.environments?.activeEnvironmentId ?? json.envs?.activeId ?? this.#envs[0]?.id ?? '');
            } else if (json.environment || json.env) {
                const env = json.environment ?? json.env ?? {};
                const fallbackEnv = new Environment(
                    String(this.#nextId()),
                    'Default',
                    env.baseUrl ?? ENVIRONMENT.baseUrl,
                    new Default(
                        env.defaults?.timeoutMilliseconds ?? env.defaults?.timeout ?? new Default().timeout,
                        new RetryConfiguration(
                            false,
                            env.defaults?.retryPolicy?.maximumAttempts ?? env.defaults?.retry?.count ?? new Default().retry.count,
                            env.defaults?.retryPolicy?.delayMilliseconds ?? env.defaults?.retry?.delay ?? new Default().retry.delay,
                        ),
                    ),
                    Object.entries(env.environmentVariables ?? env.variables ?? {}).map(([k, v]) => new RequestEntry(this.#nextId(), k, String(v))),
                );
                this.#envs = [fallbackEnv];
                this.#activeEnvId = fallbackEnv.id;
            }

            if (json.globalInputs || json.inputs) {
                const inputs = json.globalInputs ?? json.inputs ?? {};
                this.#inputs = Object.entries(inputs)
                    .map(([k, v]) => new Input(this.#nextId(), k, String(v)));
            }

            if (Array.isArray(json.steps)) {
                this.#steps = json.steps.map(s => new Step(
                    this.#nextId(),
                    s.stepIdentifier ?? s.id ?? '',
                    s.dependsOnStepIdentifiers ?? s.dependsOn ?? [],
                    new Request(
                        s.request?.httpMethod ?? s.req?.method ?? 'GET',
                        s.request?.requestUrl ?? s.req?.path ?? '',
                        s.request?.timeoutMilliseconds ?? s.req?.timeout ?? null,
                        new RetryConfiguration(
                            s.req?.retry?.enabled ?? !!(s.request?.retryPolicy ?? s.req?.retry),
                            s.request?.retryPolicy?.maximumAttempts ?? s.req?.retry?.count ?? 3,
                            s.request?.retryPolicy?.delayMilliseconds ?? s.req?.retry?.delay ?? 1000,
                        ),
                        Object.entries(s.request?.headers ?? s.req?.headers ?? {}).map(([k, v]) => new RequestEntry(this.#nextId(), k, String(v))),
                        Object.entries(s.request?.body ?? s.req?.body ?? {}).map(([k, v]) => new RequestEntry(this.#nextId(), k, String(v))),
                        Object.entries(s.request?.requestVariables ?? s.req?.variables ?? {}).map(([k, v]) => new RequestEntry(this.#nextId(), k, String(v))),
                    ),
                    new Extract(
                        Object.entries(s.extraction?.bodyJsonPaths ?? s.extract?.body ?? {}).map(([k, v]) => new ExtractBodyEntry(this.#nextId(), k, String(v))),
                        Object.entries(s.extraction?.headerValues ?? s.extract?.headers ?? {}).map(([k, v]) => new ExtractHeaderEntry(this.#nextId(), k, String(v))),
                    ),
                    (s.checks ?? s.check ?? []).map(c => new CheckEntry(
                        this.#nextId(), c.source ?? 'status', c.jsonPath ?? c.path ?? '',
                        c.equals != null ? String(c.equals) : '', !!c.exists,
                    )),
                ));
            }

            return true;
        } catch {
            return false;
        }
    }

    static #parseVal(v) {
        if (v === 'true') return true;
        if (v === 'false') return false;
        const n = Number(v);
        return (v !== '' && !Number.isNaN(n)) ? n : v;
    }
}
