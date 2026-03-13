import { Span } from '../component/Span.js';
import { Button } from '../component/Button.js';
import { HorizontalLayout } from '../component/HorizontalLayout.js';
import { VerticalLayout } from '../component/VerticalLayout.js';
import { Style } from '../component/Style.js';
import { Property } from '../component/Property.js';
import { Theme } from './Theme.js';

export class NavbarView {

    #onToggle;

    constructor(onToggle) {
        this.#onToggle = onToggle;
    }

    build() {

        const themeToken = Theme.token;
        const isDarkTheme = Theme.isDark;

        const navbarContainer = new HorizontalLayout()
            .setStyle(Style.Key.ALIGN_ITEMS, Style.Value.CENTER)
            .setStyle(Style.Key.JUSTIFY_CONTENT, Style.Value.SPACE_BETWEEN)
            .setStyle(Style.Key.PADDING, Theme.spacing.padNavbar)
            .setStyle(Style.Key.HEIGHT, Theme.style.heightNavbar)
            .setStyle(Style.Key.FLEX_SHRINK, Theme.style.flexShrink0)
            .setStyle(Style.Key.FLEX_WRAP, Style.Value.NOWRAP)
            .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.navBg)
            .setStyle(Style.Key.COLOR, themeToken.text)
            .setStyle(
                Style.Key.BORDER_BOTTOM,
                `1px solid ${themeToken.border}`
            )
            .setStyle(Style.Key.BOX_SHADOW, themeToken.navShadow);


        const leftSection = new HorizontalLayout()
            .setStyle(Style.Key.ALIGN_ITEMS, Style.Value.CENTER)
            .setStyle(Style.Key.GAP, Theme.spacing.gap12)
            .setStyle(Style.Key.FLEX_WRAP, Style.Value.NOWRAP);


        const applicationIcon = new Span()
            .setText(themeToken.icon.app)
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize140);


        const titleContainer = new VerticalLayout();


        const applicationTitle = new Span()
            .setText('Visit Health Api Flow Builder')
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize95)
            .setStyle(Style.Key.FONT_WEIGHT, Style.Value.BOLD)
            .setStyle(Style.Key.LETTER_SPACING, Theme.style.letterSpacingTight);


        const applicationSubtitle = new Span()
            .setText(themeToken.icon.appSubtitle)
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize68)
            .setStyle(Style.Key.COLOR, themeToken.muted)


        titleContainer.addChild(applicationTitle);
        titleContainer.addChild(applicationSubtitle);

        leftSection.addChild(applicationIcon);
        leftSection.addChild(titleContainer);


        const rightSection = new HorizontalLayout()
            .setStyle(Style.Key.ALIGN_ITEMS, Style.Value.CENTER)
            .setStyle(Style.Key.GAP, Theme.spacing.gap12)
            .setStyle(Style.Key.FLEX_WRAP, Style.Value.NOWRAP);


        const productTag = new Span()
            .setText(themeToken.icon.productTag)
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize72)
            .setStyle(Style.Key.FONT_FAMILY, Theme.style.fontFamilyMono)
            .setStyle(Style.Key.COLOR, themeToken.muted);


        rightSection.addChild(productTag);


        if (this.#onToggle) {

            const themeToggleButton = new Button()
                .setProperty(Property.Key.TYPE, Property.Value.BUTTON)
                .setStyle(Style.Key.DISPLAY, Style.Value.FLEX)
                .setStyle(Style.Key.ALIGN_ITEMS, Style.Value.CENTER)
                .setStyle(Style.Key.GAP, Theme.spacing.gap6)
                .setStyle(Style.Key.PADDING, Theme.spacing.padBtn)
                .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius8)
                .setStyle(Style.Key.CURSOR, Style.Value.POINTER)
                .setStyle(Style.Key.BORDER, `1px solid ${themeToken.border}`)
                .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.toggleBg)
                .setStyle(Style.Key.COLOR, themeToken.text)
                .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize78)
                .setStyle(Style.Key.FONT_WEIGHT, Theme.style.fontWeight500);


            const toggleIcon = new Span()
                .setText(isDarkTheme ? themeToken.icon.toggleLight : themeToken.icon.toggleDark)
                .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize100);


            themeToggleButton.addChild(toggleIcon);


            themeToggleButton.addEventListener('mouseover', () => {
                themeToggleButton.setStyle(
                    Style.Key.BACKGROUND_COLOR,
                    themeToken.navHoverBg
                );
            });

            themeToggleButton.addEventListener('mouseout', () => {
                themeToggleButton.setStyle(
                    Style.Key.BACKGROUND_COLOR,
                    themeToken.toggleBg
                );
            });

            themeToggleButton.addEventListener('click', this.#onToggle);

            rightSection.addChild(themeToggleButton);
        }


        navbarContainer.addChild(leftSection);
        navbarContainer.addChild(rightSection);

        return navbarContainer.getElement();
    }
}
