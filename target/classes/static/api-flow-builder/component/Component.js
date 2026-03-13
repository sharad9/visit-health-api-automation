import { Attribute } from "./Attribute.js";
import { Property } from "./Property.js";
import { Style } from "./Style.js";

/**
 * Represents a base UI component with support for child components, 
 * styles, attributes, and event handling.
 */
export class Component {

  /**
   * @type {HTMLElement}
   */
  #element;
  #children = [];
  #style;
  #attribute;
  #property;

  constructor(tagName) {
    this.#element = document.createElement(tagName);
    this.#style = new Style(this);
    this.#attribute = new Attribute(this);
    this.#property = new Property(this);
  }


  addChild(child) {
    if (!child) return this;
    if (child instanceof Component) {
      this.#element.appendChild(child.getElement());
      this.#children.push(child);
    } else if (child instanceof Node) {
      this.#element.appendChild(child);
    }
    return this;
  }


  clearChildren() {
    this.#element.innerHTML = '';
    this.#children = [];
    return this;
  }

  addChildren(...children) {
    children.forEach(child => this.addChild(child));
    return this;
  }

  setText(text) {
    this.#element.textContent = text;
    return this;
  }

  setHtml(html) {
    this.#element.innerHTML = html;
    return this;
  }

  addClass(name) {
    this.#element.classList.add(name);
    return this;
  }

  removeClass(name) {
    this.#element.classList.remove(name);
    return this;
  }


  addEventListener(event, handler) {
    this.#element.addEventListener(event, handler);
    return this;
  }

  getElement() {
    return this.#element;
  }

  getParentElement() {
    return this.#element.parentElement;
  }

  getStyle(key) {
    return this.#style.get(key);
  }

  getProperty(key) {
    return this.#property.get(key);
  }

  getAttribute(key) {
    return this.#attribute.get(key);
  }

  setStyle(key, value) {
    this.#style.set(key, value);
    return this;
  }

  setAttribute(key, value) {
    this.#attribute.set(key, value);
    return this;
  }

  setProperty(key, value) {
    this.#property.set(key, value);
    return this;
  }
}
