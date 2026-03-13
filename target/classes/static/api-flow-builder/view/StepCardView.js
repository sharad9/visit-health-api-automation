import { Div } from '../component/Div.js';
import { HorizontalLayout } from '../component/HorizontalLayout.js';
import { VerticalLayout } from '../component/VerticalLayout.js';
import { Span } from '../component/Span.js';
import { P } from '../component/P.js';
import { Button } from '../component/Button.js';
import { Input } from '../component/Input.js';
import { Select } from '../component/Select.js';
import { Option } from '../component/Option.js';
import { Style } from '../component/Style.js';
import { Property } from '../component/Property.js';
import { Theme } from './Theme.js';
import { StepCardHeaderView } from './StepCardHeaderView.js';
import { StepRequestView } from './StepRequestView.js';
import { StepExtractView } from './StepExtractView.js';
import { StepChecksView } from './StepChecksView.js';
import { StepResponseView } from './StepResponseView.js';

export class StepCardView {
    #model;
    #commit;

    constructor({ model, commit, step, index, onRemove }) {
        this.#model = model;
        this.#commit = commit;
        let isDetailsOpen = false;
        if (!step.req.variables) step.req.variables = [];

        const UI = {
            placeholders: {
                stepId: 'stepName',
                dependsOn: 'step1, step2',
                path: '{{BASE_URL}}/api/resource',
                timeout: 'optional',
                headerKey: 'header-name',
                headerValue: 'value',
                bodyKey: 'field',
                bodyValue: '{{var}} / value',
                variableKey: 'varName',
                variableValue: 'value',
                extractVar: 'varName',
                extractBodyPath: '$.data.field',
                extractHeaderKey: 'header-name',
                checkPath: '$.data.id',
                checkEquals: 'equals',
            },
            labels: {
                stepId: 'Step ID',
                dependsOn: 'Depends On',
                method: 'Method',
                path: 'Path',
                timeout: 'Timeout',
                headers: 'Headers',
                body: 'Body',
                variables: 'Variables',
                extractBody: 'From Body (JSONPath)',
                extractHeaders: 'From Headers',
                response: 'Response',
            },
            buttons: {
                addHeader: '＋ Header',
                addField: '＋ Field',
                addVariable: '＋ Variable',
                addBodyExtract: '＋ Body Extract',
                addHeaderExtract: '＋ Header Extract',
                addCheck: '＋ Check',
                run: 'Run',
                open: 'Open',
                close: 'Close',
                untitled: 'untitled',
            },
            messages: {
                missingUrl: 'Request URL is required.',
                invalidUrl: 'Request URL must be absolute (http/https).',
            },
            methodOptions: [
                ['GET', 'GET'],
                ['POST', 'POST'],
                ['PUT', 'PUT'],
                ['PATCH', 'PATCH'],
                ['DELETE', 'DELETE'],
            ],
            checkSources: [
                ['status', 'status'],
                ['body', 'body'],
                ['header', 'header'],
            ],
            blockColors: {
                request: '99,102,241',
                extract: '16,185,129',
                check: '245,158,11',
            },
        };

        const setFocusRing = (component, on) => {
            component.setStyle(Style.Key.BORDER_COLOR, on ? Theme.token.primary : Theme.token.border);
            component.setStyle(Style.Key.BOX_SHADOW, on ? Theme.token.focusRing : Style.Value.NONE);
        };

        const setMethodBadgeStyle = (component, method) => {
            const map = Theme.token.methodBadge[String(method || 'GET').toUpperCase()] ?? Theme.token.methodBadge.GET;

            component.setStyle(Style.Key.BACKGROUND_COLOR, map.bg);
            component.setStyle(Style.Key.COLOR, map.color);
        };

        const createInput = (value, onInput, placeholder = '', type = 'text') => {
            const buildTemplateContent = (text) => {
                const raw = String(text ?? '');
                const parts = [];
                if (!raw) return { parts, isEmpty: true };
                const regex = /{{[^}]+}}/g;
                let lastIndex = 0;
                for (const match of raw.matchAll(regex)) {
                    const start = match.index ?? 0;
                    const before = raw.slice(lastIndex, start);
                    if (before) parts.push({ type: 'text', value: before });
                    parts.push({ type: 'token', value: match[0] });
                    lastIndex = start + match[0].length;
                }
                const tail = raw.slice(lastIndex);
                if (tail) parts.push({ type: 'text', value: tail });
                return { parts, isEmpty: parts.length === 0 };
            };

            const renderOverlay = (overlayComponent, textValue) => {
                overlayComponent.clearChildren();
                const { parts, isEmpty } = buildTemplateContent(textValue);
                if (isEmpty) {
                    const placeholderSpan = new Span()
                        .setText(placeholder)
                        .setStyle(Style.Key.COLOR, Theme.token.mutedDim);
                    overlayComponent.addChild(placeholderSpan);
                    return;
                }
                parts.forEach(part => {
                    if (part.type === 'token') {
                        const tokenSpan = new Span()
                            .setText(part.value)
                            .setStyle(Style.Key.COLOR, Theme.token.templateText);
                        overlayComponent.addChild(tokenSpan);
                    } else if (part.value) {
                        overlayComponent.addChild(document.createTextNode(part.value));
                    }
                });
            };

            const wrapper = new Div()
                .setStyle(Style.Key.FLEX, Theme.style.flex1)
                .setStyle(Style.Key.MIN_WIDTH, Theme.style.minWidth0)
                .setStyle(Style.Key.POSITION, Theme.style.positionRelative)
                .setStyle(Style.Key.BACKGROUND_COLOR, Theme.token.inputBg)
                .setStyle(Style.Key.BORDER, `1.5px solid ${Theme.token.border}`)
                .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius8)
                .setStyle(Style.Key.OVERFLOW, Style.Value.HIDDEN);

            const overlay = new Span()
                .setStyle(Style.Key.POSITION, Theme.style.positionAbsolute)
                .setStyle(Style.Key.TOP, Theme.style.top0)
                .setStyle(Style.Key.LEFT, '0')
                .setStyle(Style.Key.RIGHT, '0')
                .setStyle(Style.Key.BOTTOM, Theme.style.bottom0)
                .setStyle(Style.Key.PADDING, Theme.spacing.padInput)
                .setStyle(Style.Key.WHITE_SPACE, Style.Value.PRE)
                .setStyle(Style.Key.POINTER_EVENTS, Style.Value.NONE)
                .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize82)
                .setStyle(Style.Key.FONT_FAMILY, Theme.style.fontFamilyBase)
                .setStyle(Style.Key.COLOR, Theme.token.text);
            renderOverlay(overlay, value);

            const input = new Input()
                .setStyle(Style.Key.WIDTH, Theme.style.widthFull)
                .setStyle(Style.Key.PADDING, Theme.spacing.padInput)
                .setStyle(Style.Key.BACKGROUND_COLOR, Theme.style.backgroundTransparent)
                .setStyle(Style.Key.BORDER, Style.Value.NONE)
                .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius8)
                .setStyle(Style.Key.COLOR, 'transparent')
                .setStyle(Style.Key.CARET_COLOR, Theme.token.text)
                .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize82)
                .setStyle(Style.Key.FONT_FAMILY, Theme.style.fontFamilyBase)
                .setStyle(Style.Key.OUTLINE, Style.Value.NONE);
            input.setProperty(Property.Key.TYPE, type);
            input.setProperty(Property.Key.VALUE, value ?? '');
            input.setProperty(Property.Key.PLACEHOLDER, placeholder);
            input.addEventListener('focus', () => setFocusRing(wrapper, true));
            input.addEventListener('blur', () => setFocusRing(wrapper, false));
            input.addEventListener('input', event => {
                renderOverlay(overlay, event.target.value);
                onInput(event.target.value);
                this.#commit();
            });

            wrapper.addChild(overlay);
            wrapper.addChild(input);
            return wrapper;
        };

        const createSelect = (options, value, onChange) => {
            const arrow = Theme.isDark ? '%23818cf8' : '%234f46e5';
            const select = new Select()
                .setStyle(Style.Key.PADDING, Theme.spacing.padSelect)
                .setStyle(Style.Key.BACKGROUND_COLOR, Theme.token.inputBg)
                .setStyle(Style.Key.BORDER, `1.5px solid ${Theme.token.border}`)
                .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius8)
                .setStyle(Style.Key.COLOR, Theme.token.text)
                .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize82)
                .setStyle(Style.Key.OUTLINE, Style.Value.NONE)
                .setStyle(Style.Key.CURSOR, Style.Value.POINTER)
                .setStyle(Style.Key.APPEARANCE, Style.Value.NONE)
                .setStyle(Style.Key.BACKGROUND_IMAGE, `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='10' height='10' viewBox='0 0 10 10'%3E%3Cpath fill='${arrow}' d='M5 7L0 2h10z'/%3E%3C/svg%3E")`)
                .setStyle(Style.Key.BACKGROUND_REPEAT, Theme.style.backgroundNoRepeat)
                .setStyle(Style.Key.BACKGROUND_POSITION, Theme.style.backgroundPosRight);
            select.addEventListener('focus', () => setFocusRing(select, true));
            select.addEventListener('blur', () => setFocusRing(select, false));
            options.forEach(([optionValue, optionLabel]) => {
                const option = new Option().setText(optionLabel);
                option.setProperty(Property.Key.VALUE, optionValue);
                if (optionValue === value) option.setProperty(Property.Key.SELECTED, Property.Value.TRUE);
                select.addChild(option);
            });
            select.addEventListener('change', event => { onChange(event.target.value); this.#commit(); });
            return select;
        };

        const createLabel = text => new P()
            .setText(text)
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize72)
            .setStyle(Style.Key.FONT_WEIGHT, Theme.style.fontWeight600)
            .setStyle(Style.Key.COLOR, Theme.token.primary)
            .setStyle(Style.Key.TEXT_TRANSFORM, Theme.style.textUppercase)
            .setStyle(Style.Key.LETTER_SPACING, Theme.style.letterSpacingWide)
            .setStyle(Style.Key.MARGIN_BOTTOM, Theme.spacing.marginBottomLabel);

        const createSubLabel = text => new P()
            .setText(text)
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize68)
            .setStyle(Style.Key.FONT_WEIGHT, Theme.style.fontWeight700)
            .setStyle(Style.Key.COLOR, Theme.token.mutedDim)
            .setStyle(Style.Key.TEXT_TRANSFORM, Theme.style.textUppercase)
            .setStyle(Style.Key.LETTER_SPACING, Theme.style.letterSpacingWide);

        const createDivider = () => new Div()
            .setStyle(Style.Key.HEIGHT, Theme.style.height1px)
            .setStyle(Style.Key.BACKGROUND_COLOR, Theme.token.border);

        const createFieldWrap = (labelText, inputElement, grow = true) => {
            const container = new VerticalLayout()
                .setStyle(Style.Key.FLEX, grow ? Theme.style.flex1 : Theme.style.flexNone)
                .setStyle(Style.Key.MIN_WIDTH, Theme.style.minWidth0);
            container.addChild(createLabel(labelText));
            container.addChild(inputElement);
            return container;
        };

        const createRemoveButton = onClick => {
            const button = new Button()
                .setText(Theme.token.icon.remove)
                .setStyle(Style.Key.BACKGROUND_COLOR, Theme.style.backgroundTransparent)
                .setStyle(Style.Key.BORDER, Style.Value.NONE)
                .setStyle(Style.Key.COLOR, Theme.token.rmColor)
                .setStyle(Style.Key.CURSOR, Style.Value.POINTER)
                .setStyle(Style.Key.WIDTH, Theme.style.width26)
                .setStyle(Style.Key.HEIGHT, Theme.style.height26)
                .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius6)
                .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize70)
                .setStyle(Style.Key.DISPLAY, Style.Value.FLEX)
                .setStyle(Style.Key.ALIGN_ITEMS, Style.Value.CENTER)
                .setStyle(Style.Key.JUSTIFY_CONTENT, Style.Value.CENTER)
                .setStyle(Style.Key.FLEX_SHRINK, Theme.style.flexShrink0);
            button.setProperty(Property.Key.TYPE, Property.Value.BUTTON);
            button.addEventListener('mouseover', () => { button.setStyle(Style.Key.BACKGROUND_COLOR, Theme.token.rmHoverBg); button.setStyle(Style.Key.COLOR, Theme.token.danger); });
            button.addEventListener('mouseout', () => { button.setStyle(Style.Key.BACKGROUND_COLOR, Theme.style.backgroundTransparent); button.setStyle(Style.Key.COLOR, Theme.token.rmColor); });
            button.addEventListener('click', onClick);
            return button;
        };

        const createAddButton = (text, onClick) => {
            const button = new Button()
                .setText(text)
                .setStyle(Style.Key.PADDING, Theme.spacing.padAddBtn)
                .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius8)
                .setStyle(Style.Key.CURSOR, Style.Value.POINTER)
                .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize78)
                .setStyle(Style.Key.FONT_WEIGHT, Theme.style.fontWeight500)
                .setStyle(Style.Key.BORDER, `1.5px solid ${Theme.token.addBtnBorder}`)
                .setStyle(Style.Key.BACKGROUND_COLOR, Theme.token.addBtnBg)
                .setStyle(Style.Key.COLOR, Theme.token.primary);
            button.setProperty(Property.Key.TYPE, Property.Value.BUTTON);
            button.addEventListener('mouseover', () => { button.setStyle(Style.Key.BACKGROUND_COLOR, Theme.token.addHoverBg); button.setStyle(Style.Key.BORDER_COLOR, Theme.token.primary); });
            button.addEventListener('mouseout', () => { button.setStyle(Style.Key.BACKGROUND_COLOR, Theme.token.addBtnBg); button.setStyle(Style.Key.BORDER_COLOR, Theme.token.addBtnBorder); });
            button.addEventListener('click', onClick);
            return button;
        };

        const createKeyValueRow = (keyValue, valueValue, keyPlaceholder, valuePlaceholder, onKeyChange, onValueChange, onRemove) => {
            const row = new HorizontalLayout()
                .setStyle(Style.Key.GAP, Theme.spacing.gap6)
                .setStyle(Style.Key.ALIGN_ITEMS, Style.Value.CENTER);
            row.addChild(createInput(keyValue, onKeyChange, keyPlaceholder));
            row.addChild(createInput(valueValue, onValueChange, valuePlaceholder));
            row.addChild(createRemoveButton(onRemove));
            return row;
        };

        let requestHeaderList;
        let requestBodyList;
        let requestVariableList;
        let extractBodyList;
        let extractHeaderList;
        let checkListContainer;
        let responseStatusBadge;
        let responseOutput;
        let responseMeta;
        let runRequestButton;
        let requestInFlight = false;

        const renderRequestHeaders = () => {
            requestHeaderList.clearChildren();
            step.req.headers.forEach(headerEntry => requestHeaderList.addChild(createKeyValueRow(
                headerEntry.key, headerEntry.value, UI.placeholders.headerKey, UI.placeholders.headerValue,
                value => { headerEntry.key = value; },
                value => { headerEntry.value = value; },
                () => { step.req.headers = step.req.headers.filter(x => x.uid !== headerEntry.uid); renderRequestHeaders(); this.#commit(); },
            )));
        };
        const renderRequestBody = () => {
            requestBodyList.clearChildren();
            step.req.body.forEach(bodyEntry => requestBodyList.addChild(createKeyValueRow(
                bodyEntry.key, bodyEntry.value, UI.placeholders.bodyKey, UI.placeholders.bodyValue,
                value => { bodyEntry.key = value; },
                value => { bodyEntry.value = value; },
                () => { step.req.body = step.req.body.filter(x => x.uid !== bodyEntry.uid); renderRequestBody(); this.#commit(); },
            )));
        };
        const renderRequestVariables = () => {
            requestVariableList.clearChildren();
            step.req.variables.forEach(variableEntry => requestVariableList.addChild(createKeyValueRow(
                variableEntry.key, variableEntry.value, UI.placeholders.variableKey, UI.placeholders.variableValue,
                value => { variableEntry.key = value; },
                value => { variableEntry.value = value; },
                () => { step.req.variables = step.req.variables.filter(x => x.uid !== variableEntry.uid); renderRequestVariables(); this.#commit(); },
            )));
        };
        const renderExtractBody = () => {
            extractBodyList.clearChildren();
            step.extract.body.forEach(extractEntry => extractBodyList.addChild(createKeyValueRow(
                extractEntry.key, extractEntry.path, UI.placeholders.extractVar, UI.placeholders.extractBodyPath,
                value => { extractEntry.key = value; },
                value => { extractEntry.path = value; },
                () => { step.extract.body = step.extract.body.filter(x => x.uid !== extractEntry.uid); renderExtractBody(); this.#commit(); },
            )));
        };
        const renderExtractHeaders = () => {
            extractHeaderList.clearChildren();
            step.extract.headers.forEach(extractEntry => extractHeaderList.addChild(createKeyValueRow(
                extractEntry.key, extractEntry.value, UI.placeholders.extractVar, UI.placeholders.extractHeaderKey,
                value => { extractEntry.key = value; },
                value => { extractEntry.value = value; },
                () => { step.extract.headers = step.extract.headers.filter(x => x.uid !== extractEntry.uid); renderExtractHeaders(); this.#commit(); },
            )));
        };

        const ensureResponseState = () => {
            if (!step.__response) {
                step.__response = { status: 'idle', statusCode: null, body: '', error: '', meta: '' };
            }
        };

        const parseValue = value => {
            if (value === 'true') return true;
            if (value === 'false') return false;
            const n = Number(value);
            return (value !== '' && !Number.isNaN(n)) ? n : value;
        };

        const buildInputMap = () => {
            const map = {};
            this.#model.inputs
                .filter(input => input.key && input.key.trim())
                .forEach(input => { map[input.key] = input.value; });
            this.#model.env?.variables
                ?.filter(variable => variable.key && variable.key.trim())
                .forEach(variable => { map[variable.key] = variable.value; });
            step.req.variables
                .filter(variable => variable.key && variable.key.trim())
                .forEach(variable => { map[variable.key] = variable.value; });
            return map;
        };

        const resolveTemplate = (value, inputMap) => {
            if (typeof value !== 'string') return value;
            let resolved = value;
            Object.keys(inputMap).forEach(key => {
                resolved = resolved.split(`{{${key}}}`).join(String(inputMap[key]));
            });
            return resolved;
        };

        const isAbsoluteUrl = value => /^https?:\/\//i.test(value);

        const setResponseState = nextState => {
            ensureResponseState();
            step.__response = { ...step.__response, ...nextState };
            if (!responseStatusBadge || !responseOutput || !responseMeta) return;

            const status = step.__response.status;
            let badgeText = 'Idle';
            let badgeBg = Theme.token.toggleBg;
            let badgeColor = Theme.token.muted;

            if (status === 'loading') {
                badgeText = 'Running...';
                badgeBg = Theme.token.statusDirtyBg;
                badgeColor = Theme.token.statusDirtyText;
            } else if (status === 'success') {
                badgeText = step.__response.statusCode ? String(step.__response.statusCode) : 'Success';
                badgeBg = Theme.token.statusLiveBg;
                badgeColor = Theme.token.statusLiveText;
            } else if (status === 'error') {
                badgeText = step.__response.statusCode ? String(step.__response.statusCode) : 'Error';
                badgeBg = Theme.token.statusErrorBg;
                badgeColor = Theme.token.statusErrorText;
            }

            responseStatusBadge
                .setText(badgeText)
                .setStyle(Style.Key.BACKGROUND_COLOR, badgeBg)
                .setStyle(Style.Key.COLOR, badgeColor);

            responseMeta.setText(step.__response.meta || '');
            responseOutput.setProperty(Property.Key.VALUE, step.__response.body || step.__response.error || '');
        };

        const runRequest = async () => {
            if (requestInFlight) return;
            requestInFlight = true;
            runRequestButton?.setProperty(Property.Key.DISABLED, Property.Value.TRUE);

            const inputMap = buildInputMap();
            const path = resolveTemplate(step.req.path || '', inputMap);
            const url = path;

            if (!url) {
                setResponseState({ status: 'error', error: UI.messages.missingUrl, body: '', statusCode: null, meta: '' });
                requestInFlight = false;
                runRequestButton?.setProperty(Property.Key.DISABLED, Property.Value.FALSE);
                return;
            }
            if (!isAbsoluteUrl(url)) {
                setResponseState({ status: 'error', error: UI.messages.invalidUrl, body: '', statusCode: null, meta: '' });
                requestInFlight = false;
                runRequestButton?.setProperty(Property.Key.DISABLED, Property.Value.FALSE);
                return;
            }

            const method = String(step.req.method || 'GET').toUpperCase();
            const headers = {};
            step.req.headers
                .filter(h => h.key)
                .forEach(h => { headers[h.key] = resolveTemplate(h.value, inputMap); });

            const bodyEntries = step.req.body.filter(b => b.key);
            let body;
            if (bodyEntries.length && !['GET', 'HEAD'].includes(method)) {
                const bodyObject = {};
                bodyEntries.forEach(entry => { bodyObject[entry.key] = parseValue(resolveTemplate(entry.value, inputMap)); });
                body = JSON.stringify(bodyObject);
                const hasContentType = Object.keys(headers).some(key => key.toLowerCase() === 'content-type');
                if (!hasContentType) headers['Content-Type'] = 'application/json';
            }

            const timeoutMs = step.req.timeout ?? this.#model.env.defaults.timeout;
            const retryConfig = step.req.retry.enabled ? step.req.retry : this.#model.env.defaults.retry;
            const maxAttempts = Math.max(1, Number(retryConfig?.count ?? 1));
            const delayMs = Math.max(0, Number(retryConfig?.delay ?? 0));

            setResponseState({ status: 'loading', error: '', body: '', statusCode: null, meta: '' });

            let lastError;
            for (let attempt = 1; attempt <= maxAttempts; attempt += 1) {
                try {
                    const startTime = Date.now();
                    const response = await fetch('/proxy', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ url, method, headers, body, timeout: timeoutMs }),
                    });
                    const proxyPayload = await response.json().catch(() => null);
                    if (proxyPayload?.error) throw new Error(proxyPayload.error);
                    if (!proxyPayload) throw new Error('Proxy returned an invalid response.');

                    const text = proxyPayload?.body ?? '';
                    const contentType = proxyPayload?.headers?.['content-type'] || '';
                    let bodyText = text;
                    if (text && contentType.includes('application/json')) {
                        try {
                            bodyText = JSON.stringify(JSON.parse(text), null, 2);
                        } catch {
                            bodyText = text;
                        }
                    }
                    const durationMs = Date.now() - startTime;
                    setResponseState({
                        status: proxyPayload?.status && proxyPayload.status < 400 ? 'success' : 'error',
                        statusCode: proxyPayload?.status ?? null,
                        body: bodyText || '',
                        error: proxyPayload?.statusText || '',
                        meta: `${method} ${path || url} • ${durationMs}ms`,
                    });
                    requestInFlight = false;
                    runRequestButton?.setProperty(Property.Key.DISABLED, Property.Value.FALSE);
                    return;
                } catch (error) {
                    lastError = error;
                    if (attempt < maxAttempts && delayMs) {
                        await new Promise(resolve => setTimeout(resolve, delayMs));
                    }
                }
            }

            setResponseState({
                status: 'error',
                statusCode: null,
                body: '',
                error: lastError?.message || 'Request failed.',
                meta: `${method} ${path || url}`,
            });
            requestInFlight = false;
            runRequestButton?.setProperty(Property.Key.DISABLED, Property.Value.FALSE);
        };

        const headerView = new StepCardHeaderView({
            step,
            index,
            onRemove,
            UI,
            createRemoveButton,
            setMethodBadgeStyle,
        });
        const cardHeader = headerView.header;
        const toggleButton = headerView.toggleButton;
        const titleElement = headerView.titleElement;
        const pathElement = headerView.pathElement;
        const methodBadge = headerView.methodBadge;

        const cardBody = new VerticalLayout()
            .setStyle(Style.Key.DISPLAY, Style.Value.NONE)
            .setStyle(Style.Key.GAP, Theme.spacing.gap12)
            .setStyle(Style.Key.PADDING, Theme.spacing.padCardBody)
            .setStyle(Style.Key.BACKGROUND_COLOR, Theme.token.cardBg);

        const toggleDetails = () => {
            isDetailsOpen = !isDetailsOpen;
            cardBody.setStyle(Style.Key.DISPLAY, isDetailsOpen ? Style.Value.FLEX : Style.Value.NONE);
            toggleButton.setText(isDetailsOpen ? UI.buttons.close : UI.buttons.open);
            if (isDetailsOpen) runRequest();
        };

        cardHeader
            .setStyle(Style.Key.CURSOR, Style.Value.POINTER)
            .addEventListener('click', () => {
                toggleDetails();
            });
        toggleButton.addEventListener('click', event => {
            event.stopPropagation();
            toggleDetails();
        });

        const basicRowContainer = new HorizontalLayout()
            .setStyle(Style.Key.GAP, Theme.spacing.gap12);
        basicRowContainer.addChild(createFieldWrap(UI.labels.stepId, createInput(step.id, value => { step.id = value; titleElement.setText(value || UI.buttons.untitled); }, UI.placeholders.stepId)));
        basicRowContainer.addChild(createFieldWrap(UI.labels.dependsOn, createInput(step.dependsOn.join(', '), value => { step.dependsOn = value.split(',').map(s => s.trim()).filter(Boolean); }, UI.placeholders.dependsOn)));
        cardBody.addChild(basicRowContainer);

        requestHeaderList = new VerticalLayout()
            .setStyle(Style.Key.GAP, Theme.spacing.gap6);
        requestBodyList = new VerticalLayout()
            .setStyle(Style.Key.GAP, Theme.spacing.gap6);
        requestVariableList = new VerticalLayout()
            .setStyle(Style.Key.GAP, Theme.spacing.gap6);
        renderRequestHeaders();
        renderRequestBody();
        renderRequestVariables();

        requestHeaderList = new VerticalLayout();
        requestBodyList = new VerticalLayout();
        requestVariableList = new VerticalLayout();
        extractBodyList = new VerticalLayout();
        extractHeaderList = new VerticalLayout();
        checkListContainer = new VerticalLayout();

        renderRequestHeaders();
        renderRequestBody();
        renderRequestVariables();
        renderExtractBody();
        renderExtractHeaders();

        const requestView = new StepRequestView({
            step,
            UI,
            createInput,
            createSelect,
            createSubLabel,
            createDivider,
            createFieldWrap,
            createAddButton,
            setMethodBadgeStyle,
            methodBadge,
            pathElement,
            requestHeaderList,
            requestBodyList,
            requestVariableList,
            onAddHeader: () => { step.req.headers.push(this.#model.createRequestEntry()); renderRequestHeaders(); this.#commit(); },
            onAddBody: () => { step.req.body.push(this.#model.createRequestEntry()); renderRequestBody(); this.#commit(); },
            onAddVariable: () => { step.req.variables.push(this.#model.createRequestEntry()); renderRequestVariables(); this.#commit(); },
            onCommit: () => this.#commit(),
        });
        const requestBlock = requestView.requestBlock;

        const extractView = new StepExtractView({
            UI,
            createSubLabel,
            createDivider,
            createAddButton,
            extractBodyList,
            extractHeaderList,
            onAddBodyExtract: () => { step.extract.body.push(this.#model.createExtractBodyEntry()); renderExtractBody(); this.#commit(); },
            onAddHeaderExtract: () => { step.extract.headers.push(this.#model.createExtractHeaderEntry()); renderExtractHeaders(); this.#commit(); },
        });
        const extractBlock = extractView.extractBlock;

        const checksView = new StepChecksView({
            step,
            UI,
            createSelect,
            createInput,
            createSubLabel,
            createAddButton,
            createRemoveButton,
            checkListContainer,
            onAddCheck: () => { step.check.push(this.#model.createCheckEntry()); renderChecks(); this.#commit(); },
            onCommit: () => this.#commit(),
        });
        const checkBlock = checksView.checkBlock;
        const renderChecks = checksView.renderChecks;

        const responseView = new StepResponseView({ UI, onRun: runRequest });
        responseStatusBadge = responseView.responseStatusBadge;
        responseOutput = responseView.responseOutput;
        responseMeta = responseView.responseMeta;
        runRequestButton = responseView.runRequestButton;
        const responseBlock = responseView.responseBlock;

        ensureResponseState();
        setResponseState(step.__response);

        cardBody.addChild(requestBlock);
        cardBody.addChild(responseBlock);
        cardBody.addChild(extractBlock);
        cardBody.addChild(checkBlock);

        const cardContainer = new Div()
            .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius12)
            .setStyle(Style.Key.OVERFLOW, Style.Value.HIDDEN)
            .setStyle(Style.Key.BORDER, `1px solid ${Theme.token.border}`)
            .setStyle(Style.Key.BOX_SHADOW, Theme.token.cardShadow);
        cardContainer.setAttribute('data-step-uid', String(step.uid));
        cardContainer.addChild(cardHeader);
        cardContainer.addChild(cardBody);
        this.card = cardContainer;
    }
}
