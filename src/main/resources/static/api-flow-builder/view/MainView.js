import { Theme } from './Theme.js';
import { MetaView } from './MetaView.js';
import { EnvironmentView } from './EnvironmentView.js';
import { InputView } from './InputView.js';
import { StepsView } from './StepsView.js';
import { PreviewView } from './PreviewView.js';
import { NavbarView } from './NavbarView.js';
import { HorizontalLayout } from '../component/HorizontalLayout.js';
import { VerticalLayout } from '../component/VerticalLayout.js';
import { Span } from '../component/Span.js';
import { Button } from '../component/Button.js';
import { Style } from '../component/Style.js';
import { Property } from '../component/Property.js';

export class MainView {
    #model;
    #previewView;
    #navbarView;
    #metaView;
    #environmentView;
    #inputView;
    #stepsView;
    #formPanelComponent;
    #isPreviewOpen;
    applicationElement;

    constructor(model, isDark = false, onToggle = null) {
        this.#model = model;
        Theme.setMode(isDark);
        this.#navbarView = new NavbarView(onToggle);
        this.#metaView = new MetaView(this.#model, () => this.#commit());
        this.#environmentView = new EnvironmentView(this.#model, () => this.#commit());
        this.#inputView = new InputView(this.#model, () => this.#commit());
        this.#stepsView = new StepsView(this.#model, () => this.#commit(), () => this.#formPanelComponent.getElement());
        this.#isPreviewOpen = true;

        this.#applyGlobalStyles();
        this.#previewView = new PreviewView(this.#model, (raw) => this.#onImport(raw));
        this.#buildLayout();
    }

    render() {
        this.#renderFormPanel();
    }

    #applyGlobalStyles() {
        const themeToken = Theme.token;
        const wrap = el => ({ getElement: () => el });
        const documentElementStyle = new Style(wrap(document.documentElement));
        const documentBodyStyle = new Style(wrap(document.body));
        documentElementStyle.set(Style.Key.HEIGHT, Theme.style.heightFull);
        documentBodyStyle.set(Style.Key.HEIGHT, Theme.style.heightFull);
        documentBodyStyle.set(Style.Key.MARGIN, Theme.style.margin0);
        documentBodyStyle.set(Style.Key.OVERFLOW, Style.Value.HIDDEN);
        documentBodyStyle.set(Style.Key.FONT_FAMILY, Theme.style.fontFamilyBase);
        documentBodyStyle.set(Style.Key.BACKGROUND_COLOR, themeToken.bg);
    }

    #buildLayout() {
        const themeToken = Theme.token;
        const navigationBar = this.#navbarView.build();

        this.#formPanelComponent = new VerticalLayout()
            .setStyle(Style.Key.FLEX, Theme.style.flex1)
            .setStyle(Style.Key.MIN_WIDTH, Theme.style.minWidth0)
            .setStyle(Style.Key.MIN_HEIGHT, Theme.style.minHeight0)
            .setStyle(Style.Key.OVERFLOW_Y, Style.Value.AUTO)
            .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.panelBg);

        const contentBodyRow = new HorizontalLayout()
            .setStyle(Style.Key.FLEX, Theme.style.flex1)
            .setStyle(Style.Key.MIN_HEIGHT, Theme.style.minHeight0)
            .setStyle(Style.Key.OVERFLOW, Style.Value.HIDDEN)
            .setStyle(Style.Key.FLEX_WRAP, Style.Value.NOWRAP);
        contentBodyRow.addChild(this.#formPanelComponent);

        const previewPanelHeader = new HorizontalLayout()
            .setStyle(Style.Key.PADDING, Theme.spacing.padMainHeader)
            .setStyle(Style.Key.ALIGN_ITEMS, Style.Value.CENTER)
            .setStyle(Style.Key.JUSTIFY_CONTENT, Style.Value.SPACE_BETWEEN)
            .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.sectionBg)
            .setStyle(Style.Key.BORDER_BOTTOM, `1px solid ${themeToken.border}`)
            .setStyle(Style.Key.FLEX_SHRINK, Theme.style.flexShrink0);

        const previewPanelTitle = new Span()
            .setText(themeToken.icon.previewPanel)
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize78)
            .setStyle(Style.Key.FONT_WEIGHT, Theme.style.fontWeight600)
            .setStyle(Style.Key.COLOR, themeToken.text);

        const previewPanelToggleButton = new Button()
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
        previewPanelToggleButton.setProperty(Property.Key.TYPE, Property.Value.BUTTON);

        previewPanelHeader.addChild(previewPanelTitle);
        previewPanelHeader.addChild(previewPanelToggleButton);

        const previewPanelBody = new VerticalLayout()
            .setStyle(Style.Key.FLEX, Theme.style.flex1)
            .setStyle(Style.Key.MIN_HEIGHT, Theme.style.minHeight0);
        previewPanelBody.addChild(this.#previewView.panel);

        const previewPanelWrapper = new VerticalLayout()
            .setStyle(Style.Key.WIDTH, Theme.style.width42Percent)
            .setStyle(Style.Key.MIN_WIDTH, Theme.style.minWidth360)
            .setStyle(Style.Key.FLEX_SHRINK, Theme.style.flexShrink0)
            .setStyle(Style.Key.HEIGHT, Theme.style.heightFull)
            .setStyle(Style.Key.OVERFLOW, Style.Value.HIDDEN)
            .setStyle(Style.Key.BORDER_LEFT, `1px solid ${themeToken.border}`)
            .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.jsonBg);
        previewPanelWrapper.addChild(previewPanelHeader);
        previewPanelWrapper.addChild(previewPanelBody);

        const updatePreviewPanelLayout = () => {
            previewPanelBody.setStyle(Style.Key.DISPLAY, this.#isPreviewOpen ? Style.Value.FLEX : Style.Value.NONE);
            previewPanelToggleButton.setText(this.#isPreviewOpen ? 'Close' : 'Open');
        };
        updatePreviewPanelLayout();

        previewPanelToggleButton.addEventListener('click', event => {
            event.stopPropagation();
            this.#isPreviewOpen = !this.#isPreviewOpen;
            updatePreviewPanelLayout();
        });

        contentBodyRow.addChild(previewPanelWrapper);

        const applicationContainer = new VerticalLayout()
            .setStyle(Style.Key.HEIGHT, Theme.style.heightViewport)
            .setStyle(Style.Key.OVERFLOW, Style.Value.HIDDEN);
        applicationContainer.addChild(navigationBar);
        applicationContainer.addChild(contentBodyRow);
        this.applicationElement = applicationContainer.getElement();
    }

    #renderFormPanel() {
        this.#formPanelComponent.clearChildren();
        const formContentContainer = new VerticalLayout()
            .setStyle(Style.Key.GAP, Theme.spacing.gap14)
            .setStyle(Style.Key.PADDING, Theme.spacing.padFormInner);
        formContentContainer.addChild(this.#metaView.build());
        formContentContainer.addChild(this.#environmentView.build());
        formContentContainer.addChild(this.#inputView.build());
        formContentContainer.addChild(this.#stepsView.build());
        this.#formPanelComponent.addChild(formContentContainer);
        this.#commit();
    }

    #commit() {
        this.#model.commit();
        this.#previewView.refresh();
    }

    #onImport(raw) {
        const importSuccessful = this.#model.importJson(raw);
        if (importSuccessful) this.#renderFormPanel();
        return importSuccessful;
    }
}
