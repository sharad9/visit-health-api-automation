export class Theme {
    static THEME = {
        LIGHT: {
            bg: '#F6F7FB',
            panelBg: '#FFFFFF',
            cardBg: '#FFFFFF',
            sectionBg: '#F9FAFC',
            border: '#E2E6ED',
            text: '#1F2933',
            muted: '#6B7280',
            mutedDim: '#9AA5B1',

            primary: '#FF6C37',
            headerGradient: 'linear-gradient(135deg,#FF6C37,#FF8F5B)',
            headerText: '#FFFFFF',
            templateText: '#2563EB',
            templateChipBg: 'rgba(37,99,235,0.12)',
            templateChipBorder: 'rgba(37,99,235,0.28)',
            templateChipText: '#1D4ED8',
            tooltipBg: '#0F172A',
            tooltipText: '#F8FAFC',
            tooltipBorder: 'rgba(15,23,42,0.35)',

            inputBg: '#FFFFFF',
            navBg: '#FFFFFF',

            jsonBg: '#F7F8FB',
            jsonText: '#111827',
            jsonBorder: '#E2E6ED',

            addBtnBg: '#FFF3EE',
            addBtnBorder: '#FF6C37',

            stepNumBg: '#FFF3EE',

            sectionShadow: '0 1px 3px rgba(15,23,42,0.08)',
            cardShadow: '0 1px 2px rgba(15,23,42,0.06)',
            navShadow: '0 2px 20px rgba(15,23,42,0.12)',

            cardHdrBg: '#F9FAFC',
            cardHdrBorder: '#E2E6ED',

            emptyIcon: '#C7CED8',
            emptyText: '#6B7280',
            emptySubText: '#9AA5B1',

            rmColor: '#9AA5B1',
            rmHoverBg: 'rgba(239,68,68,0.12)',

            addHoverBg: 'rgba(255,108,55,0.18)',

            focusRing: '0 0 0 3px rgba(255,108,55,0.28)',

            toggleBg: '#F1F3F8',
            navHoverBg: 'rgba(255,255,255,0.2)',
            danger: '#E44D3A',
            statusLiveBg: '#E8F5EC',
            statusLiveText: '#157347',
            statusDirtyBg: '#FFF4E5',
            statusDirtyText: '#B45309',
            statusErrorBg: '#FDECEC',
            statusErrorText: '#B91C1C',
            methodBadge: {
                GET: { bg: '#E8F5EC', color: '#157347' },
                POST: { bg: '#E7F0FF', color: '#1E40AF' },
                PUT: { bg: '#FFF4E5', color: '#B45309' },
                PATCH: { bg: '#F3E8FF', color: '#6D28D9' },
                DELETE: { bg: '#FDECEC', color: '#B91C1C' },
            },
            icon: {
                app: '▣ API',
                meta: '▤ Meta',
                environment: '◍ Environment',
                inputs: '⌗ Inputs',
                steps: '▸ Steps',
                emptySteps: '▫ Step',

                request: '⟶ Request',
                extract: '⤓ Extract',
                checks: '✔ Checks',

                remove: '✖',
                copied: '✔ Copied',

                statusLive: '● Live',
                statusDirty: '◔ Syncing',
                statusError: '⚠ Invalid JSON',

                previewTitle: '▦ JSON Output',
                previewSubtitle: '✎ Edit JSON to sync with form',

                toggleLight: '☀ Light',
                toggleDark: '☽ Dark',

                productTag: '◆ API Flow JSON Generator',
                appSubtitle: '⚙ Design - Preview - Export',

                previewPanel: '▧ Preview'
            },
            toggleIcon: 'L',
        },

        DARK: {
            bg: '#1B1C20',
            panelBg: '#1F2127',
            cardBg: '#23262D',
            sectionBg: '#20232A',
            border: '#2E323B',

            text: '#E6E8EE',
            muted: '#A0A7B4',
            mutedDim: '#7C8594',

            primary: '#FF6C37',
            headerGradient: 'linear-gradient(135deg,#FF6C37,#FF8F5B)',
            headerText: '#FFFFFF',
            templateText: '#60A5FA',
            templateChipBg: 'rgba(96,165,250,0.16)',
            templateChipBorder: 'rgba(96,165,250,0.35)',
            templateChipText: '#93C5FD',
            tooltipBg: '#0B1220',
            tooltipText: '#E2E8F0',
            tooltipBorder: 'rgba(148,163,184,0.25)',

            inputBg: '#1B1C20',
            navBg: '#1F2127',

            jsonBg: '#1B1C20',
            jsonText: '#E6E8EE',
            jsonBorder: '#2E323B',

            addBtnBg: 'rgba(255,108,55,0.12)',
            addBtnBorder: '#FF6C37',

            stepNumBg: 'rgba(255,108,55,0.18)',

            sectionShadow: '0 2px 10px rgba(0,0,0,0.45)',
            cardShadow: '0 2px 6px rgba(0,0,0,0.5)',
            navShadow: '0 2px 20px rgba(0,0,0,0.5)',

            cardHdrBg: '#20232A',
            cardHdrBorder: '#2E323B',

            emptyIcon: '#6E7685',
            emptyText: '#A0A7B4',
            emptySubText: '#7C8594',

            rmColor: '#A0A7B4',
            rmHoverBg: 'rgba(239,68,68,0.22)',

            addHoverBg: 'rgba(255,108,55,0.22)',

            focusRing: '0 0 0 3px rgba(255,108,55,0.28)',

            toggleBg: '#23262D',
            navHoverBg: 'rgba(255,255,255,0.2)',
            danger: '#F06A55',
            statusLiveBg: 'rgba(22,163,74,0.15)',
            statusLiveText: '#6EE7B7',
            statusDirtyBg: 'rgba(245,158,11,0.18)',
            statusDirtyText: '#FCD34D',
            statusErrorBg: 'rgba(239,68,68,0.18)',
            statusErrorText: '#FCA5A5',
            methodBadge: {
                GET: { bg: 'rgba(22,163,74,0.18)', color: '#6EE7B7' },
                POST: { bg: 'rgba(59,130,246,0.18)', color: '#93C5FD' },
                PUT: { bg: 'rgba(245,158,11,0.18)', color: '#FCD34D' },
                PATCH: { bg: 'rgba(139,92,246,0.18)', color: '#C4B5FD' },
                DELETE: { bg: 'rgba(239,68,68,0.18)', color: '#FCA5A5' },
            },
            icon: {
                app: '▣ API',
                meta: '▤ Meta',
                environment: '◍ Environment',
                inputs: '⌗ Inputs',
                steps: '▸ Steps',
                emptySteps: '▫ Step',

                request: '⟶ Request',
                extract: '⤓ Extract',
                checks: '✔ Checks',

                remove: '✖',
                copied: '✔ Copied',

                statusLive: '● Live',
                statusDirty: '◔ Syncing',
                statusError: '⚠ Invalid JSON',

                previewTitle: '▦ JSON Output',
                previewSubtitle: '✎ Edit JSON to sync with form',

                toggleLight: '☀ Light',
                toggleDark: '☽ Dark',

                productTag: '◆ API Flow JSON Generator',
                appSubtitle: '⚙ Design - Preview - Export',

                previewPanel: '▧ Preview'
            },
            toggleIcon: 'D',
        },
    };

    static isDark = false;
    static token = Theme.THEME.LIGHT;

    static spacing = {
        // Gap scale
        gap2:   '1px',
        gap5:   '3px',
        gap6:   '4px',
        gap8:   '5px',
        gap10:  '6px',
        gap12:  '7px',
        gap14:  '8px',
        gap19:  '10px',

        // Padding presets
        padBadge:       '1px 5px',
        padToggleBtn:   '2px 7px',
        padBtn:         '3px 8px',
        padAddBtn:      '3px 8px',
        padInput:       '5px 8px',
        padSelect:      '5px 24px 5px 8px',
        padCardHdr:     '6px 8px',
        padSectionHdr:  '8px 10px',
        padCardBody:    '8px',
        padSectionBody: '10px',
        padFormInner:   '10px',
        padMainHeader:  '6px 8px',
        padNavbar:      '0 14px',
        padPill:        '2px 5px',
        padCopyBtn:     '3px 10px',
        padJson:        '12px',
        padPreviewHdr:  '8px 12px',
        padEmpty:       '22px 12px',

        // Margin
        marginBottomLabel: '6px',
    };

    static style = {
        // Layout
        flex1: '1',
        flexNone: 'none',
        minWidth0: '0',
        minHeight0: '0',
        flexShrink0: '0',
        widthFull: '100%',
        heightFull: '100%',
        heightViewport: '100vh',
        width42Percent: '42%',
        minWidth360: '360px',
        margin0: '0',

        // Positioning
        positionRelative: 'relative',
        positionAbsolute: 'absolute',
        cursorColResize: 'col-resize',
        zIndex10: '10',
        leftHandle: '-3px',
        top0: '0',
        bottom0: '0',
        widthHandle: '6px',

        // Sizing
        height1px: '1px',
        heightNavbar: '54px',
        height22: '22px',
        width22: '22px',
        height26: '26px',
        width26: '26px',
        maxWidth180: '180px',
        maxWidth260: '260px',
        minHeight120: '120px',

        // Typography
        fontFamilyMono: 'monospace',
        fontFamilyBase: '"Inter", system-ui, sans-serif',
        fontFamilyCode: '"Fira Code","Cascadia Code","JetBrains Mono",monospace',
        fontSize68: '0.6rem',
        fontSize70: '0.62rem',
        fontSize72: '0.64rem',
        fontSize73: '0.65rem',
        fontSize75: '0.67rem',
        fontSize78: '0.69rem',
        fontSize82: '0.72rem',
        fontSize85: '0.75rem',
        fontSize87: '0.77rem',
        fontSize88: '0.79rem',
        fontSize95: '0.85rem',
        fontSize100: '0.9rem',
        fontSize120: '1.0rem',
        fontSize140: '1.2rem',
        fontWeight500: '500',
        fontWeight600: '600',
        fontWeight700: '700',
        letterSpacingTight: '-0.2px',
        letterSpacingMedium: '0.5px',
        letterSpacingWide: '0.8px',
        textUppercase: 'uppercase',
        lineHeightJson: '1.7',
        textOverflowEllipsis: 'ellipsis',

        // Borders
        borderRadius5: '5px',
        borderRadius6: '6px',
        borderRadius8: '8px',
        borderRadius10: '10px',
        borderRadius12: '12px',
        borderRadiusPill: '99px',
        borderRadiusCircle: '50%',

        // Effects
        opacity50: '0.5',
        opacity88: '0.88',
        opacity100: '1',
        transformUp1: 'translateY(-1px)',
        transform0: 'translateY(0)',

        // Backgrounds
        backgroundTransparent: 'transparent',
        backgroundNoRepeat: 'no-repeat',
        backgroundPosRight: 'right 10px center',
    };

    isDark;
    token;

    constructor(isDark = false) {
        this.isDark = isDark;
        this.token = isDark ? Theme.THEME.DARK : Theme.THEME.LIGHT;
    }

    static setMode(isDark = false) {
        Theme.isDark = isDark;
        Theme.token = isDark ? Theme.THEME.DARK : Theme.THEME.LIGHT;
    }
}
