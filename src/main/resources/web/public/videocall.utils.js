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

/**
 * Returns an array of RTCIceServers ordered by response time
 */
const getTurnServers = (hostnames, limit = 1) =>
  urlPing(hostnames.map(hostname => `https://${hostname}/px.png`)).then(
    states =>
      states
        .slice(0, limit)
        .map(state => `turn:${new URL(state.url).hostname}`)
  )
