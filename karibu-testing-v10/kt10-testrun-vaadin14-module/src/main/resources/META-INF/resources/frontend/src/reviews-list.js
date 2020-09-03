

import {PolymerElement,html} from '@polymer/polymer/polymer-element.js';
import '@polymer/paper-input/paper-input.js';

class ReviewsList extends PolymerElement {

    static get template() {
        return html`
        <div class="view-toolbar">
            <vaadin-text-field id="search" autocapitalize=off>
                <iron-icon icon="lumo:magnifier" slot="prefix"></iron-icon>
            </vaadin-text-field>
            <vaadin-button id="newReview" theme="primary">
                <iron-icon icon="lumo:plus"></iron-icon><span>New review</span>
            </vaadin-button>
            <h1 id="header"></h1>
        </div>`;
    }

    static get is() {
        return 'reviews-list';
    }
}

customElements.define(ReviewsList.is, ReviewsList);

