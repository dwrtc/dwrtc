"use strict"

export const getElementsArrayById = ids => {
  const elements = []
  ids.forEach(e => (elements[e] = document.getElementById(e)))
  return elements
}

export const hide = e => (e.hidden = true)
export const show = e => (e.hidden = false)
