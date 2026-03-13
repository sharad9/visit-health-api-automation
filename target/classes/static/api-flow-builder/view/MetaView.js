import { Div } from '../component/Div.js';
import { HorizontalLayout } from '../component/HorizontalLayout.js';
import { VerticalLayout } from '../component/VerticalLayout.js';
import { Span } from '../component/Span.js';
import { P } from '../component/P.js';
import { Input } from '../component/Input.js';
import { Style } from '../component/Style.js';
import { Property } from '../component/Property.js';
import { Button } from '../component/Button.js';
import { Theme } from './Theme.js';

export class MetaView {

    #model;
    #commit;

    constructor(model, commit) {
        this.#model = model;
        this.#commit = commit;
    }

    build() {

        const metaData = this.#model.meta;
        const themeToken = Theme.token;

        const setFocusRing = (component, on) => {
            component.setStyle(Style.Key.BORDER_COLOR, on ? themeToken.primary : themeToken.border);
            component.setStyle(Style.Key.BOX_SHADOW, on ? themeToken.focusRing : Style.Value.NONE);
        };

        let isSectionOpen = true;


        const headerContainer = new HorizontalLayout()
            .setStyle(Style.Key.PADDING, Theme.spacing.padSectionHdr)
            .setStyle(Style.Key.CURSOR, Style.Value.POINTER)
            .setStyle(Style.Key.USER_SELECT, Style.Value.NONE)
            .setStyle(Style.Key.JUSTIFY_CONTENT, Style.Value.SPACE_BETWEEN)
            .setStyle(Style.Key.BACKGROUND_IMAGE, themeToken.headerGradient);


        const headerTitle = new Span()
            .setText(themeToken.icon.meta)
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


        const rowContainer = new HorizontalLayout()
            .setStyle(Style.Key.GAP, Theme.spacing.gap12);


        const workflowIdContainer = new VerticalLayout()
            .setStyle(Style.Key.FLEX, Theme.style.flex1)
            .setStyle(Style.Key.MIN_WIDTH, Theme.style.minWidth0);

        const workflowIdLabel = new P()
            .setText('Api Fow ID')
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize72)
            .setStyle(Style.Key.FONT_WEIGHT, Theme.style.fontWeight600)
            .setStyle(Style.Key.COLOR, themeToken.primary)
            .setStyle(Style.Key.TEXT_TRANSFORM, Theme.style.textUppercase)
            .setStyle(Style.Key.LETTER_SPACING, Theme.style.letterSpacingWide)
            .setStyle(Style.Key.MARGIN_BOTTOM, Theme.spacing.marginBottomLabel);

        const workflowIdInput = new Input()
            .setStyle(Style.Key.FLEX, Theme.style.flex1)
            .setStyle(Style.Key.MIN_WIDTH, Theme.style.minWidth0)
            .setStyle(Style.Key.PADDING, Theme.spacing.padInput)
            .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.inputBg)
            .setStyle(Style.Key.BORDER, `1.5px solid ${themeToken.border}`)
            .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius8)
            .setStyle(Style.Key.COLOR, themeToken.text)
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize82)
            .setStyle(Style.Key.OUTLINE, Style.Value.NONE);

        workflowIdInput.setProperty(Property.Key.TYPE, 'text');
        workflowIdInput.setProperty(Property.Key.VALUE, metaData.id ?? '');
        workflowIdInput.setProperty(Property.Key.PLACEHOLDER, 'workflow.id');

        workflowIdInput.addEventListener('focus', () => {
            setFocusRing(workflowIdInput, true);
        });

        workflowIdInput.addEventListener('blur', () => {
            setFocusRing(workflowIdInput, false);
        });

        workflowIdInput.addEventListener('input', event => {
            metaData.id = event.target.value;
            this.#commit();
        });

        workflowIdContainer.addChild(workflowIdLabel);
        workflowIdContainer.addChild(workflowIdInput);



        const versionContainer = new VerticalLayout()
            .setStyle(Style.Key.MIN_WIDTH, Theme.style.minWidth0);

        const versionLabel = new P()
            .setText('Version')
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize72)
            .setStyle(Style.Key.FONT_WEIGHT, Theme.style.fontWeight600)
            .setStyle(Style.Key.COLOR, themeToken.primary)
            .setStyle(Style.Key.TEXT_TRANSFORM, Theme.style.textUppercase)
            .setStyle(Style.Key.LETTER_SPACING, Theme.style.letterSpacingWide)
            .setStyle(Style.Key.MARGIN_BOTTOM, Theme.spacing.marginBottomLabel);

        const versionInput = new Input()
            .setStyle(Style.Key.PADDING, Theme.spacing.padInput)
            .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.inputBg)
            .setStyle(Style.Key.BORDER, `1.5px solid ${themeToken.border}`)
            .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius8)
            .setStyle(Style.Key.COLOR, themeToken.text)
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize82)
            .setStyle(Style.Key.OUTLINE, Style.Value.NONE);

        versionInput.setProperty(Property.Key.TYPE, 'text');
        versionInput.setProperty(Property.Key.VALUE, metaData.version ?? '');
        versionInput.setProperty(Property.Key.PLACEHOLDER, '1.0');

        versionInput.addEventListener('focus', () => {
            setFocusRing(versionInput, true);
        });

        versionInput.addEventListener('blur', () => {
            setFocusRing(versionInput, false);
        });

        versionInput.addEventListener('input', event => {
            metaData.version = event.target.value;
            this.#commit();
        });


        versionContainer.addChild(versionLabel);
        versionContainer.addChild(versionInput);


        rowContainer.addChild(workflowIdContainer);
        rowContainer.addChild(versionContainer);

        bodyContainer.addChild(rowContainer);


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
