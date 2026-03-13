import { Div } from '../component/Div.js';
import { HorizontalLayout } from '../component/HorizontalLayout.js';
import { VerticalLayout } from '../component/VerticalLayout.js';
import { Span } from '../component/Span.js';
import { P } from '../component/P.js';
import { Input } from '../component/Input.js';
import { Style } from '../component/Style.js';
import { Property } from '../component/Property.js';
import { Button } from '../component/Button.js';
import { Select } from '../component/Select.js';
import { Option } from '../component/Option.js';
import { Theme } from './Theme.js';
import { Default } from '../model/Default.js';
import { RetryConfiguration } from '../model/RetryConfiguration.js';

export class EnvironmentView {
    #model;
    #commit;

    constructor(model, commit) {
        this.#model = model;
        this.#commit = commit;
    }

    build() {
        const themeToken = Theme.token;

        const setFocusRing = (component, on) => {
            component.setStyle(Style.Key.BORDER_COLOR, on ? themeToken.primary : themeToken.border);
            component.setStyle(Style.Key.BOX_SHADOW, on ? themeToken.focusRing : Style.Value.NONE);
        };

        const createInput = (value, onInput, placeholder = '', type = 'text') => {
            const escapeHtml = (text) => String(text ?? '')
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;');

            const buildTemplateHtml = (text) => {
                const raw = String(text ?? '');
                if (!raw) {
                    return `<span style="color:${themeToken.mutedDim}">${escapeHtml(placeholder)}</span>`;
                }
                const regex = /{{[^}]+}}/g;
                let result = '';
                let lastIndex = 0;
                for (const match of raw.matchAll(regex)) {
                    const start = match.index ?? 0;
                    result += escapeHtml(raw.slice(lastIndex, start));
                    result += `<span style="color:${themeToken.templateText}">${escapeHtml(match[0])}</span>`;
                    lastIndex = start + match[0].length;
                }
                result += escapeHtml(raw.slice(lastIndex));
                return result || '&nbsp;';
            };

            const wrapper = new Div()
                .setStyle(Style.Key.FLEX, Theme.style.flex1)
                .setStyle(Style.Key.MIN_WIDTH, Theme.style.minWidth0)
                .setStyle(Style.Key.POSITION, Theme.style.positionRelative)
                .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.inputBg)
                .setStyle(Style.Key.BORDER, `1.5px solid ${themeToken.border}`)
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
                .setStyle(Style.Key.COLOR, themeToken.text);
            overlay.setHtml(buildTemplateHtml(value));

            const input = new Input()
                .setStyle(Style.Key.WIDTH, Theme.style.widthFull)
                .setStyle(Style.Key.PADDING, Theme.spacing.padInput)
                .setStyle(Style.Key.BACKGROUND_COLOR, Theme.style.backgroundTransparent)
                .setStyle(Style.Key.BORDER, Style.Value.NONE)
                .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius8)
                .setStyle(Style.Key.COLOR, 'transparent')
                .setStyle(Style.Key.CARET_COLOR, themeToken.text)
                .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize82)
                .setStyle(Style.Key.FONT_FAMILY, Theme.style.fontFamilyBase)
                .setStyle(Style.Key.OUTLINE, Style.Value.NONE);
            input.setProperty(Property.Key.TYPE, type);
            input.setProperty(Property.Key.VALUE, value ?? '');
            input.setProperty(Property.Key.PLACEHOLDER, placeholder);
            input.addEventListener('focus', () => setFocusRing(wrapper, true));
            input.addEventListener('blur', () => setFocusRing(wrapper, false));
            input.addEventListener('input', event => {
                overlay.setHtml(buildTemplateHtml(event.target.value));
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
                .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.inputBg)
                .setStyle(Style.Key.BORDER, `1.5px solid ${themeToken.border}`)
                .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius8)
                .setStyle(Style.Key.COLOR, themeToken.text)
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
            .setStyle(Style.Key.COLOR, themeToken.primary)
            .setStyle(Style.Key.TEXT_TRANSFORM, Theme.style.textUppercase)
            .setStyle(Style.Key.LETTER_SPACING, Theme.style.letterSpacingWide)
            .setStyle(Style.Key.MARGIN_BOTTOM, Theme.spacing.marginBottomLabel);

        const createSubLabel = text => new P()
            .setText(text)
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize68)
            .setStyle(Style.Key.FONT_WEIGHT, Theme.style.fontWeight700)
            .setStyle(Style.Key.COLOR, themeToken.mutedDim)
            .setStyle(Style.Key.TEXT_TRANSFORM, Theme.style.textUppercase)
            .setStyle(Style.Key.LETTER_SPACING, Theme.style.letterSpacingWide);

        const createFieldWrap = (labelText, inputElement, grow = true) => {
            const container = new VerticalLayout()
                .setStyle(Style.Key.FLEX, grow ? Theme.style.flex1 : Theme.style.flexNone)
                .setStyle(Style.Key.MIN_WIDTH, Theme.style.minWidth0);
            container.addChild(createLabel(labelText));
            container.addChild(inputElement);
            return container;
        };

        const createDivider = () => new Div()
            .setStyle(Style.Key.HEIGHT, Theme.style.height1px)
            .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.border);

        const createAddButton = (text, onClick) => {
            const button = new Button()
                .setText(text)
                .setStyle(Style.Key.PADDING, Theme.spacing.padAddBtn)
                .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius8)
                .setStyle(Style.Key.CURSOR, Style.Value.POINTER)
                .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize78)
                .setStyle(Style.Key.FONT_WEIGHT, Theme.style.fontWeight500)
                .setStyle(Style.Key.BORDER, `1.5px solid ${themeToken.addBtnBorder}`)
                .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.addBtnBg)
                .setStyle(Style.Key.COLOR, themeToken.primary);
            button.setProperty(Property.Key.TYPE, Property.Value.BUTTON);
            button.addEventListener('mouseover', () => { button.setStyle(Style.Key.BACKGROUND_COLOR, themeToken.addHoverBg); button.setStyle(Style.Key.BORDER_COLOR, themeToken.primary); });
            button.addEventListener('mouseout', () => { button.setStyle(Style.Key.BACKGROUND_COLOR, themeToken.addBtnBg); button.setStyle(Style.Key.BORDER_COLOR, themeToken.addBtnBorder); });
            button.addEventListener('click', onClick);
            return button;
        };

        const createRemoveButton = (text, onClick) => {
            const button = new Button()
                .setText(text)
                .setStyle(Style.Key.PADDING, Theme.spacing.padAddBtn)
                .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius8)
                .setStyle(Style.Key.CURSOR, Style.Value.POINTER)
                .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize78)
                .setStyle(Style.Key.FONT_WEIGHT, Theme.style.fontWeight500)
                .setStyle(Style.Key.BORDER, `1.5px solid ${themeToken.border}`)
                .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.cardBg)
                .setStyle(Style.Key.COLOR, themeToken.danger);
            button.setProperty(Property.Key.TYPE, Property.Value.BUTTON);
            button.addEventListener('mouseover', () => { button.setStyle(Style.Key.BACKGROUND_COLOR, themeToken.rmHoverBg); });
            button.addEventListener('mouseout', () => { button.setStyle(Style.Key.BACKGROUND_COLOR, themeToken.cardBg); });
            button.addEventListener('click', onClick);
            return button;
        };

        const createKeyValueRow = (entry, keyPlaceholder, valuePlaceholder, onRemove) => {
            const row = new HorizontalLayout()
                .setStyle(Style.Key.GAP, Theme.spacing.gap6)
                .setStyle(Style.Key.ALIGN_ITEMS, Style.Value.CENTER);
            row.addChild(createInput(entry.key, value => { entry.key = value; }, keyPlaceholder));
            row.addChild(createInput(entry.value, value => { entry.value = value; }, valuePlaceholder));
            row.addChild(createRemoveButton(themeToken.icon.remove, onRemove));
            return row;
        };

        let isOpen = true;

        const headerContainer = new HorizontalLayout()
            .setStyle(Style.Key.PADDING, Theme.spacing.padSectionHdr)
            .setStyle(Style.Key.CURSOR, Style.Value.POINTER)
            .setStyle(Style.Key.USER_SELECT, Style.Value.NONE)
            .setStyle(Style.Key.JUSTIFY_CONTENT, Style.Value.SPACE_BETWEEN)
            .setStyle(Style.Key.BACKGROUND_IMAGE, themeToken.headerGradient);
        const headerTitle = new Span()
            .setText(themeToken.icon.environment)
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize87)
            .setStyle(Style.Key.FONT_WEIGHT, Theme.style.fontWeight600)
            .setStyle(Style.Key.COLOR, themeToken.headerText);
        const toggleButton = new Button()
            .setText('Close')
            .setStyle(Style.Key.PADDING, Theme.spacing.padToggleBtn)
            .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius8)
            .setStyle(Style.Key.CURSOR, Style.Value.POINTER)
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize70)
            .setStyle(Style.Key.FONT_WEIGHT, Theme.style.fontWeight600)
            .setStyle(Style.Key.BORDER, `1px solid ${themeToken.border}`)
            .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.cardBg)
            .setStyle(Style.Key.COLOR, themeToken.text)
            .setStyle(Style.Key.FLEX_SHRINK, Theme.style.flexShrink0);
        toggleButton.setProperty(Property.Key.TYPE, Property.Value.BUTTON);
        headerContainer.addChild(headerTitle);
        headerContainer.addChild(toggleButton);

        const bodyContainer = new VerticalLayout()
            .setStyle(Style.Key.GAP, Theme.spacing.gap14)
            .setStyle(Style.Key.PADDING, Theme.spacing.padSectionBody)
            .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.cardBg);

        const envSelectRow = new HorizontalLayout()
            .setStyle(Style.Key.GAP, Theme.spacing.gap10)
            .setStyle(Style.Key.ALIGN_ITEMS, Style.Value.CENTER)
            .setStyle(Style.Key.FLEX_WRAP, Style.Value.WRAP);

        const envSelectWrap = new HorizontalLayout()
            .setStyle(Style.Key.FLEX, Theme.style.flex1)
            .setStyle(Style.Key.MIN_WIDTH, Theme.style.minWidth0);

        const envActions = new HorizontalLayout()
            .setStyle(Style.Key.GAP, Theme.spacing.gap6)
            .setStyle(Style.Key.FLEX_SHRINK, Theme.style.flexShrink0);

        const envDetailsContainer = new VerticalLayout()
            .setStyle(Style.Key.GAP, Theme.spacing.gap12);

        let deleteEnvButton;

        const renderEnvSelect = () => {
            envSelectWrap.clearChildren();
            const envOptions = this.#model.envs.map(env => [String(env.id), env.name || 'Environment']);
            const select = createSelect(envOptions, String(this.#model.activeEnvId ?? ''), value => {
                this.#model.activeEnvId = value;
                renderEnvDetails();
            });
            envSelectWrap.addChild(select);
            if (deleteEnvButton) {
                const disableDelete = this.#model.envs.length <= 1;
                deleteEnvButton.setProperty(Property.Key.DISABLED, disableDelete ? Property.Value.TRUE : Property.Value.FALSE);
                deleteEnvButton.setStyle(Style.Key.OPACITY, disableDelete ? '0.5' : '1');
                deleteEnvButton.setStyle(Style.Key.CURSOR, disableDelete ? Style.Value.NOT_ALLOWED : Style.Value.POINTER);
            }
        };

        const renderEnvDetails = () => {
            envDetailsContainer.clearChildren();
            const environment = this.#model.env;
            if (!environment) return;
            if (!environment.variables) environment.variables = [];

            const nameRow = new HorizontalLayout()
                .setStyle(Style.Key.GAP, Theme.spacing.gap12);
            nameRow.addChild(createFieldWrap('Name', createInput(environment.name, v => { environment.name = v; }, 'Dev / Staging / Prod')));

            const defaultsRow = new HorizontalLayout()
                .setStyle(Style.Key.GAP, Theme.spacing.gap12);
            defaultsRow.addChild(createFieldWrap('Timeout (ms)', createInput(String(environment.defaults.timeout), v => { environment.defaults.timeout = +v; }, '10000', 'number')));
            defaultsRow.addChild(createFieldWrap('Retry Count', createInput(String(environment.defaults.retry.count), v => { environment.defaults.retry.count = +v; }, '2', 'number')));
            defaultsRow.addChild(createFieldWrap('Retry Delay', createInput(String(environment.defaults.retry.delay), v => { environment.defaults.retry.delay = +v; }, '500', 'number')));

            const variableList = new VerticalLayout()
                .setStyle(Style.Key.GAP, Theme.spacing.gap6);

            const renderVariableRows = () => {
                variableList.clearChildren();
                environment.variables.forEach(variable => variableList.addChild(createKeyValueRow(
                    variable,
                    'VAR_NAME',
                    'value',
                    () => {
                        environment.variables = environment.variables.filter(x => x.uid !== variable.uid);
                        renderVariableRows();
                        this.#commit();
                    },
                )));
            };

            renderVariableRows();

            const variableSection = new VerticalLayout()
                .setStyle(Style.Key.GAP, Theme.spacing.gap6);
            variableSection.addChild(createSubLabel('Variables'));
            variableSection.addChild(variableList);
            variableSection.addChild(createAddButton('＋ Variable', () => {
                environment.variables.push(this.#model.createRequestEntry());
                renderVariableRows();
                this.#commit();
            }));

            envDetailsContainer.addChild(nameRow);
            envDetailsContainer.addChild(createDivider());
            envDetailsContainer.addChild(defaultsRow);
            envDetailsContainer.addChild(createDivider());
            envDetailsContainer.addChild(variableSection);
        };

        const addEnvButton = createAddButton('＋ New', () => {
            const currentEnv = this.#model.env;
            const clonedDefaults = new Default(
                currentEnv?.defaults?.timeout ?? new Default().timeout,
                new RetryConfiguration(
                    false,
                    currentEnv?.defaults?.retry?.count ?? new Default().retry.count,
                    currentEnv?.defaults?.retry?.delay ?? new Default().retry.delay,
                ),
            );
            const newEnv = this.#model.createEnvironment('', '', clonedDefaults, []);
            this.#model.envs = [...this.#model.envs, newEnv];
            this.#model.activeEnvId = newEnv.id;
            renderEnvSelect();
            renderEnvDetails();
            this.#commit();
        });

        deleteEnvButton = createRemoveButton('Delete', () => {
            if (this.#model.envs.length <= 1) return;
            const activeId = this.#model.activeEnvId;
            this.#model.envs = this.#model.envs.filter(env => env.id !== activeId);
            this.#model.activeEnvId = this.#model.envs[0]?.id ?? '';
            renderEnvSelect();
            renderEnvDetails();
            this.#commit();
        });

        envActions.addChild(addEnvButton);
        envActions.addChild(deleteEnvButton);
        envSelectRow.addChild(envSelectWrap);
        envSelectRow.addChild(envActions);

        const toggleSection = () => {
            isOpen = !isOpen;
            bodyContainer.setStyle(Style.Key.DISPLAY, isOpen ? Style.Value.FLEX : Style.Value.NONE);
            toggleButton.setText(isOpen ? 'Close' : 'Open');
        };

        headerContainer.addEventListener('click', () => {
            toggleSection();
        });
        toggleButton.addEventListener('click', event => {
            event.stopPropagation();
            toggleSection();
        });

        renderEnvSelect();
        renderEnvDetails();
        bodyContainer.addChild(envSelectRow);
        bodyContainer.addChild(createDivider());
        bodyContainer.addChild(envDetailsContainer);

        const sectionContainer = new Div()
            .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius12)
            .setStyle(Style.Key.OVERFLOW, Style.Value.HIDDEN)
            .setStyle(Style.Key.BORDER, `1px solid ${themeToken.border}`)
            .setStyle(Style.Key.BOX_SHADOW, themeToken.sectionShadow);
        sectionContainer.addChild(headerContainer);
        sectionContainer.addChild(bodyContainer);
        return sectionContainer;
    }
}
