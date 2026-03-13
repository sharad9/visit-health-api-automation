import { HorizontalLayout } from '../component/HorizontalLayout.js';
import { VerticalLayout } from '../component/VerticalLayout.js';
import { Input } from '../component/Input.js';
import { Label } from '../component/Label.js';
import { Span } from '../component/Span.js';
import { Style } from '../component/Style.js';
import { Property } from '../component/Property.js';
import { Theme } from './Theme.js';

export class StepRequestView {
    constructor({
        step,
        UI,
        createInput,
        createSelect,
        createSubLabel,
        createDivider,
        createFieldWrap,
        createAddButton,
        setMethodBadgeStyle,
        methodBadge,
        pathElement,
        requestHeaderList,
        requestBodyList,
        requestVariableList,
        onAddHeader,
        onAddBody,
        onAddVariable,
        onCommit,
    }) {
        const themeToken = Theme.token;
        const isDarkTheme = Theme.isDark;
        requestHeaderList.setStyle(Style.Key.GAP, Theme.spacing.gap6);
        requestBodyList.setStyle(Style.Key.GAP, Theme.spacing.gap6);
        requestVariableList.setStyle(Style.Key.GAP, Theme.spacing.gap6);

        const retryContainer = new HorizontalLayout()
            .setStyle(Style.Key.DISPLAY, step.req.retry.enabled ? Style.Value.FLEX : Style.Value.NONE)
            .setStyle(Style.Key.GAP, Theme.spacing.gap10);
        retryContainer.addChild(createFieldWrap('Count', createInput(String(step.req.retry.count), value => { step.req.retry.count = +value; }, '3', 'number')));
        retryContainer.addChild(createFieldWrap('Delay (ms)', createInput(String(step.req.retry.delay), value => { step.req.retry.delay = +value; }, '1000', 'number')));

        const retryCheckbox = new Input()
            .setProperty(Property.Key.TYPE, Property.Value.CHECKBOX)
            .setProperty(Property.Key.CHECKED, step.req.retry.enabled ? Property.Value.TRUE : Property.Value.FALSE)
            .setStyle('accentColor', themeToken.primary);
        retryCheckbox.addEventListener('change', event => {
            step.req.retry.enabled = event.target.checked;
            retryContainer.setStyle(Style.Key.DISPLAY, event.target.checked ? Style.Value.FLEX : Style.Value.NONE);
            onCommit?.();
        });

        const retryLabel = new Label()
            .setStyle(Style.Key.DISPLAY, Style.Value.FLEX)
            .setStyle(Style.Key.ALIGN_ITEMS, Style.Value.CENTER)
            .setStyle(Style.Key.GAP, Theme.spacing.gap6)
            .setStyle(Style.Key.FONT_SIZE, Theme.style.fontSize78)
            .setStyle(Style.Key.COLOR, themeToken.muted)
            .setStyle(Style.Key.CURSOR, Style.Value.POINTER)
            .setStyle(Style.Key.USER_SELECT, Style.Value.NONE);
        retryLabel.addChild(retryCheckbox);
        retryLabel.addChild(new Span().setText('Override retry'));

        const methodSelect = createSelect(UI.methodOptions, step.req.method, value => {
            step.req.method = value;
            methodBadge.setText(value);
            setMethodBadgeStyle(methodBadge, value);
        });

        const requestTopRow = new HorizontalLayout()
            .setStyle(Style.Key.GAP, Theme.spacing.gap10)
            .setStyle(Style.Key.ALIGN_ITEMS, Style.Value.FLEX_END);
        requestTopRow.addChild(createFieldWrap(UI.labels.method, methodSelect, false));
        requestTopRow.addChild(createFieldWrap(UI.labels.path, createInput(step.req.path, value => { step.req.path = value; pathElement.setText(value); }, UI.placeholders.path)));
        requestTopRow.addChild(createFieldWrap(UI.labels.timeout, createInput(step.req.timeout ? String(step.req.timeout) : '', value => { step.req.timeout = value ? +value : null; }, UI.placeholders.timeout, 'number'), false));

        const makeBlockBackground = baseColor => (isDarkTheme ? `rgba(${baseColor},0.06)` : `rgba(${baseColor},0.04)`);
        const makeBlockBorder = baseColor => (isDarkTheme ? `rgba(${baseColor},0.2)` : `rgba(${baseColor},0.18)`);

        const requestHeaderSection = new VerticalLayout()
            .setStyle(Style.Key.GAP, Theme.spacing.gap6);
        requestHeaderSection.addChild(createSubLabel(UI.labels.headers));
        requestHeaderSection.addChild(requestHeaderList);
        requestHeaderSection.addChild(createAddButton(UI.buttons.addHeader, onAddHeader));

        const requestBodySection = new VerticalLayout()
            .setStyle(Style.Key.GAP, Theme.spacing.gap6);
        requestBodySection.addChild(createSubLabel(UI.labels.body));
        requestBodySection.addChild(requestBodyList);
        requestBodySection.addChild(createAddButton(UI.buttons.addField, onAddBody));

        const requestVariableSection = new VerticalLayout()
            .setStyle(Style.Key.GAP, Theme.spacing.gap6);
        requestVariableSection.addChild(createSubLabel(UI.labels.variables));
        requestVariableSection.addChild(requestVariableList);
        requestVariableSection.addChild(createAddButton(UI.buttons.addVariable, onAddVariable));

        const requestBlock = new VerticalLayout()
            .setStyle(Style.Key.PADDING, Theme.spacing.padCardBody)
            .setStyle(Style.Key.BORDER_RADIUS, Theme.style.borderRadius10)
            .setStyle(Style.Key.BORDER, `1px solid ${makeBlockBorder(UI.blockColors.request)}`)
            .setStyle(Style.Key.BACKGROUND_COLOR, makeBlockBackground(UI.blockColors.request))
            .setStyle(Style.Key.GAP, Theme.spacing.gap12);
        requestBlock.addChild(createSubLabel(themeToken.icon.request));
        requestBlock.addChild(requestTopRow);
        requestBlock.addChild(retryLabel);
        requestBlock.addChild(retryContainer);
        requestBlock.addChild(createDivider());
        requestBlock.addChild(requestHeaderSection);
        requestBlock.addChild(createDivider());
        requestBlock.addChild(requestVariableSection);
        requestBlock.addChild(createDivider());
        requestBlock.addChild(requestBodySection);

        this.requestBlock = requestBlock;
    }
}
