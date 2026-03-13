import { MainModel } from '../model/MainModel.js';
import { MainView } from '../view/MainView.js';

export class Controller {
    constructor() {
        this.model = new MainModel();
        this.isDark = false;
        this.applicationElement = null;
    }

    toggleTheme() {
        this.isDark = !this.isDark;
        this.mount();
    }

    mount() {
        if (this.applicationElement && this.applicationElement.parentNode) {
            this.applicationElement.parentNode.removeChild(this.applicationElement);
        }

        // Create new view
        const view = new MainView(this.model, this.isDark, () => this.toggleTheme());
        this.applicationElement = view.applicationElement;
        this.applicationElement.id = 'api-flow-app';
        document.body.appendChild(this.applicationElement);

        view.render();
    }

    // Start the app
    start() {
        this.mount();
    }
}