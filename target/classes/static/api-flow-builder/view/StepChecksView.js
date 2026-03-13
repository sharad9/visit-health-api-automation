import { HorizontalLayout } from '../component/HorizontalLayout.js';
import { VerticalLayout } from '../component/VerticalLayout.js';
import { Label } from '../component/Label.js';
import { Span } from '../component/Span.js';
import { Input } from '../component/Input.js';
import { Style } from '../component/Style.js';
import { Property } from '../component/Property.js';
import { Theme } from './Theme.js';

export class StepChecksView {
    constructor({
        step,
        UI,
        createSelect,
        createInput,
        createSubLabel,
        createAddButton,
        createRemoveButton,
        checkListContainer,
        onAddCheck,
        onCommit,
    }) {
        const themeToken = Theme.token;
        const isDarkTheme = Theme.isDark;
        checkListContainer.setStyle(Style.Key.GAP, Theme.spacing.gap6);

        const renderChecks = () => {
            checkListContainer.clearChildren();
            step.check.forEach(checkEntry => {
                const checkRow = new HorizontalLayout()
                    .setStyle(Style.Key.GAP, Theme.spacing.gap6)
                    .setStyle(Style.Key.ALIGN_ITEMS, Style.Value.CENTER)
                    .setStyle(Style.Key.FLEX_WRAP, Style.Value.WRAP);
                checkRow.addChild(createSelect(UI.checkSources, checkEntry.source, value => { checkEntry.source = value; renderChecks(); onCommit(); }));
                if (checkEntry.source !== 'status') checkRow.addChild(createInput(checkEntry.path, value => { checkEntry.path = value; }, UI.placeholders.checkPath));
                checkRow.addChild(createInput(checkEntry.equals != null ? String(checkEntry.equals) : '', value => { checkEntry.equals = value; }, UI.placeholders.checkEquals));

                const checkBoxLabel = new Label()
                    .setStyle(Style.Key.DISPLAY, Style.Value.FLEX)
                    .setStyle(Style.Key.ALIGN_ITEMS, Style.Value.CENTER)
                    .setStyle(Style.Key.GAP, Theme.spacing.gap5)
                    .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize75)
                    .setStyle(Style.Key.COLOR, themeToken.muted)
                    .setStyle(Style.Key.CURSOR, Style.Value.POINTER)
                    .setStyle(Style.Key.FLEX_SHRINK, Theme.style.flexShrink0);
                const checkBox = new Input()
                    .setProperty(Property.Key.TYPE, Property.Value.CHECKBOX)
                    .setProperty(Property.Key.CHECKED, checkEntry.exists ? Property.Value.TRUE : Property.Value.FALSE)
                    .setStyle('accentColor', themeToken.primary);
                checkBox.addEventListener('change', event => { checkEntry.exists = event.target.checked; onCommit(); });
                checkBoxLabel.addChild(checkBox);
                checkBoxLabel.addChild(new Span().setText('exists'));
                checkRow.addChild(checkBoxLabel);
                checkRow.addChild(createRemoveButton(() => { step.check = step.check.filter(x => x.uid !== checkEntry.uid); renderChecks(); onCommit(); }));
                checkListContainer.addChild(checkRow);
            });
        };

        const makeBlockBackground = baseColor => (isDarkTheme ? `rgba(${baseColor},0.06)` : `rgba(${baseColor},0.04)`);
        const makeBlockBorder = baseColor => (isDarkTheme ? `rgba(${baseColor},0.2)` : `rgba(${baseColor},0.18)`);

        const checkBlock = new VerticalLayout()
            .setStyle(Style.Key.PADDING, Theme.spacing.padCardBody)
            .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius10)
            .setStyle(Style.Key.BORDER, `1px solid ${makeBlockBorder(UI.blockColors.check)}`)
            .setStyle(Style.Key.BACKGROUND_COLOR, makeBlockBackground(UI.blockColors.check))
            .setStyle(Style.Key.GAP, Theme.spacing.gap12);
        checkBlock.addChild(createSubLabel(themeToken.icon.checks));
        checkBlock.addChild(checkListContainer);
        checkBlock.addChild(createAddButton(UI.buttons.addCheck, onAddCheck));

        renderChecks();

        this.checkBlock = checkBlock;
        this.renderChecks = renderChecks;
    }
}
