import { StepCardView } from './StepCardView.js';
import { Div } from '../component/Div.js';
import { HorizontalLayout } from '../component/HorizontalLayout.js';
import { VerticalLayout } from '../component/VerticalLayout.js';
import { Span } from '../component/Span.js';
import { Button } from '../component/Button.js';
import { Style } from '../component/Style.js';
import { Property } from '../component/Property.js';
import { Theme } from './Theme.js';

export class StepsView {
    #model;
    #commit;
    #getFormPanel;

    constructor(model, commit, getFormPanel) {
        this.#model = model;
        this.#commit = commit;
        this.#getFormPanel = getFormPanel;
    }

    build() {
        const themeToken = Theme.token;
        let stepsListContainer;

        // Root wrapper
        const wrapperContainer = new Div()
            .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius12)
            .setStyle(Style.Key.OVERFLOW, Style.Value.HIDDEN)
            .setStyle(Style.Key.BORDER, `1px solid ${themeToken.border}`)
            .setStyle(Style.Key.BOX_SHADOW, themeToken.sectionShadow);

        // Header
        let isSectionOpen = true;

        const headerContainer = new HorizontalLayout()
            .setStyle(Style.Key.PADDING, Theme.spacing.padSectionHdr)
            .setStyle(Style.Key.CURSOR, Style.Value.POINTER)
            .setStyle(Style.Key.USER_SELECT, Style.Value.NONE)
            .setStyle(Style.Key.ALIGN_ITEMS, Style.Value.CENTER)
            .setStyle(Style.Key.JUSTIFY_CONTENT, Style.Value.SPACE_BETWEEN)
            .setStyle(Style.Key.FLEX_WRAP, Style.Value.NOWRAP)
            .setStyle(Style.Key.BACKGROUND_IMAGE, themeToken.headerGradient);

        const headerTitle = new Span().setText(themeToken.icon.steps)
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

        // Body
        const bodyContainer = new VerticalLayout()
            .setStyle(Style.Key.GAP, Theme.spacing.gap14)
            .setStyle(Style.Key.PADDING, Theme.spacing.padSectionBody)
            .setStyle(Style.Key.BACKGROUND_COLOR, themeToken.panelBg);

        const toggleSection = () => {
            isSectionOpen = !isSectionOpen;
            bodyContainer.setStyle(Style.Key.DISPLAY, isSectionOpen ? Style.Value.FLEX : Style.Value.NONE);
            toggleButton.setText(isSectionOpen ? 'Close' : 'Open');
        };

        headerContainer.addEventListener('click', () => {
            toggleSection();
        });
        toggleButton.addEventListener('click', event => {
            event.stopPropagation();
            toggleSection();
        });

        // Steps container
        stepsListContainer = new VerticalLayout()
            .setStyle(Style.Key.GAP, Theme.spacing.gap12);

        bodyContainer.addChild(stepsListContainer);

        // Render function
        const renderStepCards = () => {
            stepsListContainer.clearChildren();

            if (this.#model.steps.length === 0) {
                const emptyContainer = new VerticalLayout()
                    .setStyle(Style.Key.ALIGN_ITEMS, Style.Value.CENTER)
                    .setStyle(Style.Key.JUSTIFY_CONTENT, Style.Value.CENTER)
                    .setStyle(Style.Key.PADDING, Theme.spacing.padEmpty)
                    .setStyle(Style.Key.GAP, Theme.spacing.gap10)
                    .setStyle(Style.Key.TEXT_ALIGN, Style.Value.CENTER);

                const emptyIcon = new Div()
                    .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize120)
                    .setStyle(Style.Key.OPACITY, Theme.style.opacity50)
                    .setStyle(Style.Key.COLOR, themeToken.emptyIcon)
                    .setText(themeToken.icon.emptySteps);

                const emptyTitle = new Div()
                    .setStyle(Style.Key.FONT_WEIGHT, Theme.style.fontWeight600)
                    .setStyle(Style.Key.COLOR, themeToken.emptyText)
                    .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize88)
                    .setText('No steps yet');

                const emptySubtitle = new Div()
                    .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize78)
                    .setStyle(Style.Key.COLOR, themeToken.emptySubText)
                    .setStyle(Style.Key.MAX_WIDTH, Theme.style.maxWidth260)
                    .setText('Add steps to define your API workflow sequence');

                emptyContainer.addChild(emptyIcon);
                emptyContainer.addChild(emptyTitle);
                emptyContainer.addChild(emptySubtitle);

                stepsListContainer.addChild(emptyContainer);
                return;
            }

            this.#model.steps.forEach((step, index) => {
                const stepCardView = new StepCardView({
                    model: this.#model,
                    commit: this.#commit,
                    step,
                    index,
                    onRemove: () => {
                        this.#model.steps = this.#model.steps.filter(s => s.uid !== step.uid);
                        renderStepCards();
                        this.#commit();
                    },
                });
                stepsListContainer.addChild(stepCardView.card);
            });
        };

        renderStepCards();

        // Add step button
        const addStepButton = new Button()
            .setText('＋  Add Step')
            .setStyle(Style.Key.WIDTH, Theme.style.widthFull)
            .setStyle(Style.Key.PADDING, Theme.spacing.padCardBody)
            .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius10)
            .setStyle(Style.Key.CURSOR, Style.Value.POINTER)
            .setStyle(Style.Key.BORDER, `2px dashed ${themeToken.border}`)
            .setStyle(Style.Key.BACKGROUND_COLOR, Theme.style.backgroundTransparent)
            .setStyle(Style.Key.COLOR, themeToken.primary)
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize85)
            .setStyle(Style.Key.FONT_WEIGHT, Theme.style.fontWeight600);

        addStepButton.setProperty(Property.Key.TYPE, Property.Value.BUTTON);

        addStepButton.addEventListener('mouseover', () => {
            addStepButton.setStyle(Style.Key.BACKGROUND_COLOR, themeToken.addBtnBg);
            addStepButton.setStyle(Style.Key.BORDER_COLOR, themeToken.primary);
        });

        addStepButton.addEventListener('mouseout', () => {
            addStepButton.setStyle(Style.Key.BACKGROUND_COLOR, Theme.style.backgroundTransparent);
            addStepButton.setStyle(Style.Key.BORDER_COLOR, themeToken.border);
        });

        addStepButton.addEventListener('click', () => {
            const newStep = this.#model.createStep();
            this.#model.steps.push(newStep);
            renderStepCards();
            this.#commit();

            requestAnimationFrame(() => {
                const cardElement = stepsListContainer?.getElement?.()?.querySelector?.(`[data-step-uid="${newStep.uid}"]`);
                if (cardElement?.scrollIntoView) {
                    cardElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
                } else {
                    const panel = this.#getFormPanel?.();
                    panel?.scrollTo?.({ top: panel.scrollHeight, behavior: 'smooth' });
                }
            });
        });

        bodyContainer.addChild(addStepButton);

        wrapperContainer.addChild(headerContainer);
        wrapperContainer.addChild(bodyContainer);

        return wrapperContainer;
    }
}
