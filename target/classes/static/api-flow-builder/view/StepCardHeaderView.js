import { HorizontalLayout } from '../component/HorizontalLayout.js';
import { Span } from '../component/Span.js';
import { Button } from '../component/Button.js';
import { Style } from '../component/Style.js';
import { Property } from '../component/Property.js';
import { Theme } from './Theme.js';

export class StepCardHeaderView {
    constructor({ step, index, onRemove, UI, createRemoveButton, setMethodBadgeStyle }) {
        const themeToken = Theme.token;
        const toggleButton = new Button()
            .setText(UI.buttons.open)
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

        const stepNumberBadge = new Span()
            .setText(String(index + 1))
            .setStyle(Style.Key.WIDTH, Theme.style.width22)
            .setStyle(Style.Key.HEIGHT, Theme.style.height22)
            .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadiusCircle)
            .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.stepNumBg)
            .setStyle(Style.Key.COLOR, themeToken.primary)
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize70)
            .setStyle(Style.Key.FONT_WEIGHT, Theme.style.fontWeight700)
            .setStyle(Style.Key.DISPLAY, Style.Value.FLEX)
            .setStyle(Style.Key.ALIGN_ITEMS, Style.Value.CENTER)
            .setStyle(Style.Key.JUSTIFY_CONTENT, Style.Value.CENTER)
            .setStyle(Style.Key.FLEX_SHRINK, Theme.style.flexShrink0);

        const methodBadge = new Span()
            .setText(step.req.method)
            .setStyle(Style.Key.PADDING, Theme.spacing.padBadge)
            .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius5)
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize68)
            .setStyle(Style.Key.FONT_WEIGHT, Theme.style.fontWeight700)
            .setStyle(Style.Key.LETTER_SPACING, Theme.style.letterSpacingMedium)
            .setStyle(Style.Key.FONT_FAMILY, Theme.style.fontFamilyMono)
            .setStyle(Style.Key.FLEX_SHRINK, Theme.style.flexShrink0);
        setMethodBadgeStyle(methodBadge, step.req.method);

        const titleElement = new Span()
            .setText(step.id || UI.buttons.untitled)
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize85)
            .setStyle(Style.Key.FONT_WEIGHT, Theme.style.fontWeight600)
            .setStyle(Style.Key.COLOR, themeToken.text)
            .setStyle(Style.Key.MAX_WIDTH, Theme.style.maxWidth180)
            .setStyle(Style.Key.OVERFLOW, Style.Value.HIDDEN)
            .setStyle(Style.Key.TEXT_OVERFLOW, Theme.style.textOverflowEllipsis)
            .setStyle(Style.Key.WHITE_SPACE, Style.Value.NOWRAP);

        const pathElement = new Span()
            .setText(step.req.path || '')
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize73)
            .setStyle(Style.Key.COLOR, themeToken.muted)
            .setStyle(Style.Key.FONT_FAMILY, Theme.style.fontFamilyMono)
            .setStyle(Style.Key.MAX_WIDTH, Theme.style.maxWidth180)
            .setStyle(Style.Key.OVERFLOW, Style.Value.HIDDEN)
            .setStyle(Style.Key.TEXT_OVERFLOW, Theme.style.textOverflowEllipsis)
            .setStyle(Style.Key.WHITE_SPACE, Style.Value.NOWRAP);

        const cardHeader = new HorizontalLayout()
            .setStyle(Style.Key.PADDING, Theme.spacing.padCardHdr)
            .setStyle(Style.Key.ALIGN_ITEMS, Style.Value.CENTER)
            .setStyle(Style.Key.JUSTIFY_CONTENT, Style.Value.SPACE_BETWEEN)
            .setStyle(Style.Key.FLEX_WRAP, Style.Value.NOWRAP)
            .setStyle(Style.Key.GAP, Theme.spacing.gap8)
            .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.cardHdrBg)
            .setStyle(Style.Key.BORDER_BOTTOM, `1px solid ${themeToken.cardHdrBorder}`);

        const headerLeft = new HorizontalLayout()
            .setStyle(Style.Key.ALIGN_ITEMS, Style.Value.CENTER)
            .setStyle(Style.Key.GAP, Theme.spacing.gap8)
            .setStyle(Style.Key.MIN_WIDTH, Theme.style.minWidth0)
            .setStyle(Style.Key.FLEX, Theme.style.flex1)
            .setStyle(Style.Key.FLEX_WRAP, Style.Value.NOWRAP);
        headerLeft.addChild(stepNumberBadge);
        headerLeft.addChild(methodBadge);
        headerLeft.addChild(titleElement);
        headerLeft.addChild(pathElement);

        const headerRight = new HorizontalLayout()
            .setStyle(Style.Key.ALIGN_ITEMS, Style.Value.CENTER)
            .setStyle(Style.Key.GAP, Theme.spacing.gap6)
            .setStyle(Style.Key.FLEX_SHRINK, Theme.style.flexShrink0)
            .setStyle(Style.Key.FLEX_WRAP, Style.Value.NOWRAP);
        headerRight.addChild(toggleButton);
        headerRight.addChild(createRemoveButton(onRemove));

        cardHeader.addChild(headerLeft);
        cardHeader.addChild(headerRight);

        this.header = cardHeader;
        this.toggleButton = toggleButton;
        this.titleElement = titleElement;
        this.pathElement = pathElement;
        this.methodBadge = methodBadge;
    }
}
