"use strict"

/**
 * Returns an array of DOM elements indexed by ID.
 */
const getElementsArrayById = ids => {
  const elements = []
  ids.forEach(e => (elements[e] = document.getElementById(e)))
  return elements
}

const hide = e => (e.hidden = true)
const show = e => (e.hidden = false)
