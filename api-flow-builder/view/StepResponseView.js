import { HorizontalLayout } from '../component/HorizontalLayout.js';
import { VerticalLayout } from '../component/VerticalLayout.js';
import { Span } from '../component/Span.js';
import { Button } from '../component/Button.js';
import { TextArea } from '../component/TextArea.js';
import { Style } from '../component/Style.js';
import { Property } from '../component/Property.js';
import { Theme } from './Theme.js';

export class StepResponseView {
    constructor({ UI, onRun }) {
        const themeToken = Theme.token;
        const responseHeader = new HorizontalLayout()
            .setStyle(Style.Key.ALIGN_ITEMS, Style.Value.CENTER)
            .setStyle(Style.Key.JUSTIFY_CONTENT, Style.Value.SPACE_BETWEEN)
            .setStyle(Style.Key.GAP, Theme.spacing.gap10);

        const responseTitle = new Span()
            .setText(UI.labels.response)
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize68)
            .setStyle(Style.Key.FONT_WEIGHT, Theme.style.fontWeight700)
            .setStyle(Style.Key.COLOR, themeToken.mutedDim)
            .setStyle(Style.Key.TEXT_TRANSFORM, Theme.style.textUppercase)
            .setStyle(Style.Key.LETTER_SPACING, Theme.style.letterSpacingWide);

        const responseStatusBadge = new Span()
            .setText('Idle')
            .setStyle(Style.Key.DISPLAY, Style.Value.INLINE_FLEX)
            .setStyle(Style.Key.ALIGN_ITEMS, Style.Value.CENTER)
            .setStyle(Style.Key.GAP, Theme.spacing.gap5)
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize70)
            .setStyle(Style.Key.FONT_FAMILY, Theme.style.fontFamilyMono)
            .setStyle(Style.Key.PADDING, Theme.spacing.padPill)
            .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadiusPill)
            .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.toggleBg)
            .setStyle(Style.Key.COLOR, themeToken.muted);

        const runRequestButton = new Button()
            .setText(UI.buttons.run)
            .setStyle(Style.Key.PADDING, Theme.spacing.padBtn)
            .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius8)
            .setStyle(Style.Key.CURSOR, Style.Value.POINTER)
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize78)
            .setStyle(Style.Key.FONT_WEIGHT, Theme.style.fontWeight600)
            .setStyle(Style.Key.BORDER, `1px solid ${themeToken.border}`)
            .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.cardBg)
            .setStyle(Style.Key.COLOR, themeToken.text);
        runRequestButton.setProperty(Property.Key.TYPE, Property.Value.BUTTON);
        runRequestButton.addEventListener('click', event => {
            event.stopPropagation();
            onRun?.();
        });

        const responseHeaderRight = new HorizontalLayout()
            .setStyle(Style.Key.ALIGN_ITEMS, Style.Value.CENTER)
            .setStyle(Style.Key.GAP, Theme.spacing.gap8);
        responseHeaderRight.addChild(responseStatusBadge);
        responseHeaderRight.addChild(runRequestButton);

        responseHeader.addChild(responseTitle);
        responseHeader.addChild(responseHeaderRight);

        const responseMeta = new Span()
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize68)
            .setStyle(Style.Key.COLOR, themeToken.mutedDim);

        const responseOutput = new TextArea()
            .setStyle(Style.Key.PADDING, Theme.spacing.padInput)
            .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius8)
            .setStyle(Style.Key.BORDER, `1px solid ${themeToken.border}`)
            .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.inputBg)
            .setStyle(Style.Key.COLOR, themeToken.text)
            .setStyle(Style.Key.FONT_FAMILY, Theme.style.fontFamilyCode)
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize73)
            .setStyle(Style.Key.LINE_HEIGHT, Theme.style.lineHeightJson)
            .setStyle(Style.Key.MIN_HEIGHT, Theme.style.minHeight120)
            .setStyle(Style.Key.RESIZE, Style.Value.NONE);
        responseOutput.setProperty(Property.Key.READ_ONLY, Property.Value.TRUE);
        responseOutput.setProperty('spellcheck', false);

        const responseBlock = new VerticalLayout()
            .setStyle(Style.Key.PADDING, Theme.spacing.padCardBody)
            .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius10)
            .setStyle(Style.Key.BORDER, `1px solid ${themeToken.border}`)
            .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.sectionBg)
            .setStyle(Style.Key.GAP, Theme.spacing.gap10);
        responseBlock.addChild(responseHeader);
        responseBlock.addChild(responseMeta);
        responseBlock.addChild(responseOutput);

        this.responseBlock = responseBlock;
        this.responseStatusBadge = responseStatusBadge;
        this.responseOutput = responseOutput;
        this.responseMeta = responseMeta;
        this.runRequestButton = runRequestButton;
    }
}
