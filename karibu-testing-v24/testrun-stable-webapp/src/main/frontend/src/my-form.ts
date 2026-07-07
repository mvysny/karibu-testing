import {customElement, html, LitElement} from 'lit-element';
import '@vaadin/vaadin-form-layout';
import '@vaadin/vaadin-text-field';

@customElement("my-form")
export class MyFormElement extends LitElement {
    render() {
        return html`
<vaadin-form-layout>
      <vaadin-text-field id="firstNameField" label="First Name"></vaadin-text-field>
      <vaadin-text-field id="lastNameField" label="Last Name"></vaadin-text-field>
      <vaadin-email-field id="emailField" label="Email"></vaadin-email-field>
</vaadin-form-layout>
    `;
    }
}
