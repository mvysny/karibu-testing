import {html, LitElement} from 'lit-element';

export class MyTestElement extends LitElement {
    render() {
        return html`
      <h2>Hello</h2>
    `;
    }
}

window.customElements.define('my-test-element', MyTestElement);
