"use strict"

/**
 * Returns an array of DOM elements indexed by ID.
 */
const getElementsArrayById = ids => {
  const elements = []
  ids.forEach(e => (elements[e] = document.getElementById(e)))
  return elements
}

const hide = e => (e.style.display = "none")
const show = e => (e.style.display = "block")
const removeAllChildren = e => {
  while (e.firstChild) {
    e.removeChild(e.firstChild)
  }
}

/**
 * Returns an array of RTCIceServers ordered by response time
 */
const getTurnServers = (hostnames, limit = 1) =>
  urlPing(hostnames.map(hostname => `https://${hostname}/px.png`)).then(
    states =>
      states.slice(0, limit).map(state => `turn:${new URL(state.url).hostname}`)
  )
