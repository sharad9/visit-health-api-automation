import { VerticalLayout } from '../component/VerticalLayout.js';
import { Style } from '../component/Style.js';
import { Theme } from './Theme.js';

export class StepExtractView {
    constructor({
        UI,
        createSubLabel,
        createDivider,
        createAddButton,
        extractBodyList,
        extractHeaderList,
        onAddBodyExtract,
        onAddHeaderExtract,
    }) {
        const themeToken = Theme.token;
        const isDarkTheme = Theme.isDark;
        extractBodyList.setStyle(Style.Key.GAP, Theme.spacing.gap6);
        extractHeaderList.setStyle(Style.Key.GAP, Theme.spacing.gap6);

        const makeBlockBackground = baseColor => (isDarkTheme ? `rgba(${baseColor},0.06)` : `rgba(${baseColor},0.04)`);
        const makeBlockBorder = baseColor => (isDarkTheme ? `rgba(${baseColor},0.2)` : `rgba(${baseColor},0.18)`);

        const extractBodySection = new VerticalLayout()
            .setStyle(Style.Key.GAP, Theme.spacing.gap6);
        extractBodySection.addChild(createSubLabel(UI.labels.extractBody));
        extractBodySection.addChild(extractBodyList);
        extractBodySection.addChild(createAddButton(UI.buttons.addBodyExtract, onAddBodyExtract));

        const extractHeaderSection = new VerticalLayout()
            .setStyle(Style.Key.GAP, Theme.spacing.gap6);
        extractHeaderSection.addChild(createSubLabel(UI.labels.extractHeaders));
        extractHeaderSection.addChild(extractHeaderList);
        extractHeaderSection.addChild(createAddButton(UI.buttons.addHeaderExtract, onAddHeaderExtract));

        const extractBlock = new VerticalLayout()
            .setStyle(Style.Key.PADDING, Theme.spacing.padCardBody)
            .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius10)
            .setStyle(Style.Key.BORDER, `1px solid ${makeBlockBorder(UI.blockColors.extract)}`)
            .setStyle(Style.Key.BACKGROUND_COLOR, makeBlockBackground(UI.blockColors.extract))
            .setStyle(Style.Key.GAP, Theme.spacing.gap12);
        extractBlock.addChild(createSubLabel(themeToken.icon.extract));
        extractBlock.addChild(extractBodySection);
        extractBlock.addChild(createDivider());
        extractBlock.addChild(extractHeaderSection);

        this.extractBlock = extractBlock;
    }
}
