import {html, LitElement} from 'lit-element';

export class MyTestElement2 extends LitElement {
    render() {
        return html`
      <h2>Hello</h2>
    `;
    }
}

window.customElements.define('my-test-element2', MyTestElement2);
