import { HorizontalLayout } from '../component/HorizontalLayout.js';
import { VerticalLayout } from '../component/VerticalLayout.js';
import { Span } from '../component/Span.js';
import { Button } from '../component/Button.js';
import { TextArea } from '../component/TextArea.js';
import { Style } from '../component/Style.js';
import { Property } from '../component/Property.js';
import { Theme } from './Theme.js';

export class PreviewView {

    #model;
    #onImport;

    #jsonTextarea;
    #syncStatusElement;
    #panelContainer;
    #setStatusStyle;

    constructor(model, onImport) {
        this.#model = model;
        this.#onImport = onImport;

        this.#panelContainer = this.#build();
    }

    get panel() {
        return this.#panelContainer.getElement();
    }

    refresh() {

        const themeToken = Theme.token;

        if (!this.#jsonTextarea) return;

        this.#jsonTextarea.setProperty(
            Property.Key.VALUE,
            JSON.stringify(this.#model.buildJson(), null, 2)
        );

        this.setStatus('live', themeToken.icon.statusLive);
    }

    setStatus(mode, label) {

        if (!this.#syncStatusElement) return;

        this.#setStatusStyle(this.#syncStatusElement, mode);
        this.#syncStatusElement.setText(label);
    }

    #build() {

        const themeToken = Theme.token;

        this.#setStatusStyle = (component, mode) => {

            const map = {
                live: { bg: themeToken.statusLiveBg, color: themeToken.statusLiveText },
                dirty: { bg: themeToken.statusDirtyBg, color: themeToken.statusDirtyText },
                error: { bg: themeToken.statusErrorBg, color: themeToken.statusErrorText },
            }[mode] ?? { bg: themeToken.statusLiveBg, color: themeToken.statusLiveText };

            component.setStyle(Style.Key.DISPLAY, Style.Value.INLINE_FLEX);
            component.setStyle(Style.Key.ALIGN_ITEMS, Style.Value.CENTER);
            component.setStyle(Style.Key.GAP, Theme.spacing.gap5);
            component.setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize70);
            component.setStyle(Style.Key.FONT_FAMILY, Theme.style.fontFamilyMono);
            component.setStyle(Style.Key.PADDING, Theme.spacing.padPill);
            component.setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadiusPill);
            component.setStyle(Style.Key.BACKGROUND_COLOR, map.bg);
            component.setStyle(Style.Key.COLOR, map.color);
        };

        this.#syncStatusElement = new Span();
        this.#setStatusStyle(this.#syncStatusElement, 'live');
        this.#syncStatusElement.setText(themeToken.icon.statusLive);


        const copyJsonButton = new Button()
            .setText('Copy JSON')
            .setStyle(Style.Key.PADDING, Theme.spacing.padCopyBtn)
            .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius8)
            .setStyle(Style.Key.CURSOR, Style.Value.POINTER)
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize78)
            .setStyle(Style.Key.FONT_WEIGHT, Theme.style.fontWeight600)
            .setStyle(Style.Key.BORDER, Style.Value.NONE)
            .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.primary)
            .setStyle(Style.Key.COLOR, themeToken.headerText);

        copyJsonButton.setProperty(Property.Key.TYPE, Property.Value.BUTTON);

        copyJsonButton.addEventListener('mouseover', () => {
            copyJsonButton.setStyle(Style.Key.OPACITY, Theme.style.opacity88);
            copyJsonButton.setStyle(Style.Key.TRANSFORM, Theme.style.transformUp1);
        });

        copyJsonButton.addEventListener('mouseout', () => {
            copyJsonButton.setStyle(Style.Key.OPACITY, Theme.style.opacity100);
            copyJsonButton.setStyle(Style.Key.TRANSFORM, Theme.style.transform0);
        });

        copyJsonButton.addEventListener('click', () => {

            navigator.clipboard.writeText(
                JSON.stringify(this.#model.buildJson(), null, 2)
            );

            copyJsonButton.setText(themeToken.icon.copied);

            setTimeout(() => {
                copyJsonButton.setText('Copy JSON');
            }, 1500);
        });


        this.#jsonTextarea = new TextArea()
            .setStyle(Style.Key.FLEX, Theme.style.flex1)
            .setStyle(Style.Key.MIN_HEIGHT, Theme.style.minHeight0)
            .setStyle(Style.Key.PADDING, Theme.spacing.padJson)
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize78)
            .setStyle(Style.Key.LINE_HEIGHT, Theme.style.lineHeightJson)
            .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.jsonBg)
            .setStyle(Style.Key.COLOR, themeToken.jsonText)
            .setStyle(Style.Key.FONT_FAMILY, Theme.style.fontFamilyCode)
            .setStyle(Style.Key.BORDER, Style.Value.NONE)
            .setStyle(Style.Key.OUTLINE, Style.Value.NONE)
            .setStyle(Style.Key.RESIZE, Style.Value.NONE)
            .setStyle(Style.Key.WHITE_SPACE, Style.Value.PRE)
            .setStyle('tabSize', '2');

        this.#jsonTextarea.setProperty('spellcheck', false);


        let debounceTimer;

        this.#jsonTextarea.addEventListener('input', () => {

            clearTimeout(debounceTimer);

            this.setStatus('dirty', '… syncing');

            debounceTimer = setTimeout(() => {

                const success = this.#onImport?.(
                    this.#jsonTextarea.getProperty(Property.Key.VALUE)
                );

                if (success) {
                    this.setStatus('live', themeToken.icon.statusLive);
                } else {
                    this.setStatus('error', themeToken.icon.statusError);
                }

            }, 400);
        });

        this.refresh();


        const headerLeftContainer = new VerticalLayout()
            .setStyle(Style.Key.GAP, Theme.spacing.gap2);


        const headerTitle = new Span()
            .setText(themeToken.icon.previewTitle)
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize88)
            .setStyle(Style.Key.FONT_WEIGHT, Theme.style.fontWeight700)
            .setStyle(Style.Key.COLOR, themeToken.text);


        const headerSubtitle = new Span()
            .setText(themeToken.icon.previewSubtitle)
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize68)
            .setStyle(Style.Key.COLOR, themeToken.mutedDim);


        headerLeftContainer.addChild(headerTitle);
        headerLeftContainer.addChild(headerSubtitle);


        const headerRightContainer = new HorizontalLayout()
            .setStyle(Style.Key.ALIGN_ITEMS, Style.Value.CENTER)
            .setStyle(Style.Key.GAP, Theme.spacing.gap10);

        headerRightContainer.addChild(this.#syncStatusElement);
        headerRightContainer.addChild(copyJsonButton);


        const headerContainer = new HorizontalLayout()
            .setStyle(Style.Key.ALIGN_ITEMS, Style.Value.CENTER)
            .setStyle(Style.Key.JUSTIFY_CONTENT, Style.Value.SPACE_BETWEEN)
            .setStyle(Style.Key.PADDING, Theme.spacing.padPreviewHdr)
            .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.sectionBg)
            .setStyle(Style.Key.BORDER_BOTTOM, `1px solid ${themeToken.border}`)
            .setStyle(Style.Key.FLEX_SHRINK, Theme.style.flexShrink0);

        headerContainer.addChild(headerLeftContainer);
        headerContainer.addChild(headerRightContainer);


        const panelContainer = new VerticalLayout()
            .setStyle(Style.Key.FLEX, Theme.style.flex1)
            .setStyle(Style.Key.MIN_WIDTH, Theme.style.minWidth0)
            .setStyle(Style.Key.HEIGHT, Theme.style.heightFull)
            .setStyle(Style.Key.POSITION, Theme.style.positionRelative)
            .setStyle(Style.Key.BORDER_LEFT, `1px solid ${themeToken.border}`)
            .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.jsonBg);

        panelContainer.addChild(headerContainer);
        panelContainer.addChild(this.#jsonTextarea);


        // draggable divider
        const divider = new Span()
            .setStyle(Style.Key.POSITION, Theme.style.positionAbsolute)
            .setStyle(Style.Key.LEFT, Theme.style.leftHandle)
            .setStyle(Style.Key.TOP, Theme.style.top0)
            .setStyle(Style.Key.BOTTOM, Theme.style.bottom0)
            .setStyle(Style.Key.WIDTH, Theme.style.widthHandle)
            .setStyle(Style.Key.CURSOR, Theme.style.cursorColResize)
            .setStyle(Style.Key.Z_INDEX, Theme.style.zIndex10);

        panelContainer.addChild(divider);


        let dragging = false;

        const wrapElement = el => ({ getElement: () => el });
        const setBodyCursor = cursor => {
            const bodyStyle = new Style(wrapElement(document.body));
            bodyStyle.set(Style.Key.CURSOR, cursor);
        };

        const getResizeTarget = () => {
            const panelEl = panelContainer.getElement();
            return panelEl.parentElement?.parentElement ?? panelEl.parentElement ?? panelEl;
        };

        divider.addEventListener('mousedown', event => {
            dragging = true;
            event.preventDefault();
            setBodyCursor('col-resize');
        });

        const stopDragging = () => {
            dragging = false;
            setBodyCursor('default');
        };

        window.addEventListener('mouseup', stopDragging);
        window.addEventListener('mouseleave', stopDragging);

        window.addEventListener('mousemove', (event) => {

            if (!dragging) return;

            const minWidth = 320;
            const maxWidth = window.innerWidth - 200;

            let newWidth = window.innerWidth - event.clientX;

            if (newWidth < minWidth) newWidth = minWidth;
            if (newWidth > maxWidth) newWidth = maxWidth;

            const targetElement = getResizeTarget();
            const targetStyle = new Style(wrapElement(targetElement));
            targetStyle.set(Style.Key.WIDTH, `${newWidth}px`);
            targetStyle.set(Style.Key.MIN_WIDTH, `${newWidth}px`);
        });


        return panelContainer;
    }
}
