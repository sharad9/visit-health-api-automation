import { Div } from '../component/Div.js';
import { HorizontalLayout } from '../component/HorizontalLayout.js';
import { VerticalLayout } from '../component/VerticalLayout.js';
import { Span } from '../component/Span.js';
import { P } from '../component/P.js';
import { Button } from '../component/Button.js';
import { Input } from '../component/Input.js';
import { Style } from '../component/Style.js';
import { Property } from '../component/Property.js';
import { Theme } from './Theme.js';

export class InputView {

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

        const createTemplateInput = (value, onInput, placeholder = '') => {
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

            input.setProperty(Property.Key.TYPE, 'text');
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

        let isSectionOpen = true;


        const headerContainer = new HorizontalLayout()
            .setStyle(Style.Key.PADDING, Theme.spacing.padSectionHdr)
            .setStyle(Style.Key.CURSOR, Style.Value.POINTER)
            .setStyle(Style.Key.USER_SELECT, Style.Value.NONE)
            .setStyle(Style.Key.JUSTIFY_CONTENT, Style.Value.SPACE_BETWEEN)
            .setStyle(Style.Key.BACKGROUND_IMAGE, themeToken.headerGradient);


        const headerTitle = new Span()
            .setText(themeToken.icon.inputs)
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


        const toggleSection = () => {
            isSectionOpen = !isSectionOpen;
            bodyContainer.setStyle(
                Style.Key.DISPLAY,
                isSectionOpen ? Style.Value.FLEX : Style.Value.NONE
            );
            toggleButton.setText(isSectionOpen ? 'Close' : 'Open');
        };

        headerContainer.addEventListener('click', () => {
            toggleSection();
        });
        toggleButton.addEventListener('click', event => {
            event.stopPropagation();
            toggleSection();
        });


        const inputListContainer = new VerticalLayout()
            .setStyle(Style.Key.GAP, Theme.spacing.gap6);


        const renderInputRows = () => {

            inputListContainer.clearChildren();

            this.#model.inputs.forEach(inputEntry => {

                const rowContainer = new HorizontalLayout()
                    .setStyle(Style.Key.GAP, Theme.spacing.gap19)
                    .setStyle(Style.Key.ALIGN_ITEMS, Style.Value.CENTER);


                const keyInput = new Input()
                    .setStyle(Style.Key.FLEX, Theme.style.flex1)
                    .setStyle(Style.Key.MIN_WIDTH, Theme.style.minWidth0)
                    .setStyle(Style.Key.PADDING, Theme.spacing.padInput)
                    .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.inputBg)
                    .setStyle(Style.Key.BORDER, `1.5px solid ${themeToken.border}`)
                    .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius8)
                    .setStyle(Style.Key.COLOR, themeToken.text)
                    .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize82)
                    .setStyle(Style.Key.OUTLINE, Style.Value.NONE);

                keyInput.setProperty(Property.Key.TYPE, 'text');
                keyInput.setProperty(Property.Key.VALUE, inputEntry.key ?? '');
                keyInput.setProperty(Property.Key.PLACEHOLDER, 'KEY');

                keyInput.addEventListener('focus', () => {
                    setFocusRing(keyInput, true);
                });

                keyInput.addEventListener('blur', () => {
                    setFocusRing(keyInput, false);
                });

                keyInput.addEventListener('input', event => {
                    inputEntry.key = event.target.value;
                    this.#commit();
                });


                const valueInput = createTemplateInput(
                    inputEntry.value ?? '',
                    value => { inputEntry.value = value; },
                    '{{VARIABLE}}',
                );


                const removeButton = new Button()
                    .setText(themeToken.icon.remove)
                    .setStyle(Style.Key.BACKGROUND_COLOR, Theme.style.backgroundTransparent)
                    .setStyle(Style.Key.BORDER, Style.Value.NONE)
                    .setStyle(Style.Key.COLOR, themeToken.rmColor)
                    .setStyle(Style.Key.CURSOR, Style.Value.POINTER)
                    .setStyle(Style.Key.WIDTH, Theme.style.width26)
                    .setStyle(Style.Key.HEIGHT, Theme.style.height26)
                    .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius6)
                    .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize70)
                    .setStyle(Style.Key.DISPLAY, Style.Value.FLEX)
                    .setStyle(Style.Key.ALIGN_ITEMS, Style.Value.CENTER)
                    .setStyle(Style.Key.JUSTIFY_CONTENT, Style.Value.CENTER)
                    .setStyle(Style.Key.FLEX_SHRINK, Theme.style.flexShrink0);

                removeButton.setProperty(Property.Key.TYPE, Property.Value.BUTTON);

                removeButton.addEventListener('mouseover', () => {
                    removeButton.setStyle(Style.Key.BACKGROUND_COLOR, themeToken.rmHoverBg);
                    removeButton.setStyle(Style.Key.COLOR, themeToken.danger);
                });

                removeButton.addEventListener('mouseout', () => {
                    removeButton.setStyle(Style.Key.BACKGROUND_COLOR, Theme.style.backgroundTransparent);
                    removeButton.setStyle(Style.Key.COLOR, themeToken.rmColor);
                });

                removeButton.addEventListener('click', () => {

                    this.#model.inputs = this.#model.inputs.filter(x => x.uid !== inputEntry.uid);

                    renderInputRows();
                    this.#commit();
                });


                rowContainer.addChild(keyInput);
                rowContainer.addChild(valueInput);
                rowContainer.addChild(removeButton);

                inputListContainer.addChild(rowContainer);
            });
        };


        renderInputRows();

        const addInputButton = new Button()
            .setText('＋ Add Input')
            .setStyle(Style.Key.PADDING, Theme.spacing.padAddBtn)
            .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius8)
            .setStyle(Style.Key.CURSOR, Style.Value.POINTER)
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize78)
            .setStyle(Style.Key.FONT_WEIGHT, Theme.style.fontWeight500)
            .setStyle(Style.Key.BORDER, `1.5px solid ${themeToken.addBtnBorder}`)
            .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.addBtnBg)
            .setStyle(Style.Key.COLOR, themeToken.primary);

        addInputButton.setProperty(Property.Key.TYPE, Property.Value.BUTTON);

        addInputButton.addEventListener('mouseover', () => {
            addInputButton.setStyle(Style.Key.BACKGROUND_COLOR, themeToken.addHoverBg);
            addInputButton.setStyle(Style.Key.BORDER_COLOR, themeToken.primary);
        });

        addInputButton.addEventListener('mouseout', () => {
            addInputButton.setStyle(Style.Key.BACKGROUND_COLOR, themeToken.addBtnBg);
            addInputButton.setStyle(Style.Key.BORDER_COLOR, themeToken.addBtnBorder);
        });

        addInputButton.addEventListener('click', () => {

            this.#model.inputs.push(this.#model.createInput());

            renderInputRows();
            this.#commit();
        });


        bodyContainer.addChild(inputListContainer);
        bodyContainer.addChild(addInputButton);


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
