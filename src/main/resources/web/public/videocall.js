"use strict"

const elements = getElementsArrayById([
  "user-stream",
  "partner-stream",
  "start-session-button",
  "join-session-button",
  "start-overlay",
  "join-form",
  "peer-id-input",
  "copy-peer-id-input",
  "id-overlay",
  "copy-peer-id-button",
  "error-overlay"
])

/**
 * Onload. Setup the page interactions
 */
window.onload = () => {
  elements["start-session-button"].onclick = event => {
    event.preventDefault()
    startDwrtc(false)
    hide(elements["start-overlay"])
  }

  elements["join-session-button"].onclick = event => {
    event.preventDefault()
    if (elements["join-form"].checkValidity()) {
      const partnerId = elements["peer-id-input"].value
      console.log(`partner id: ${partnerId}`)
      startDwrtc(true, partnerId)
      hide(elements["start-overlay"])
    }
  }
}

/**
 * Called when the user is ready. Sets up DWRTC.
 */
async function startDwrtc(initiator, partnerId) {
  const wsProtocol = location.protocol === "https:" ? "wss" : "ws" // in a perfect world, it's always wss
  const webSocketUrl = `${wsProtocol}://${location.host}/ws`
  const iceServers = getIceServers(["node1.dwrtc.net", "node2.dwrtc.net"]).then(
    servers => [{ urls: servers, username: "user", credential: "dwrtc" }]
  )

  console.debug("initialize dwrtc")
  const dwrtc = new DWRTC(initiator, partnerId, webSocketUrl, await iceServers)

  dwrtc.on("started", stream => {
    console.debug("got user stream")
    elements["user-stream"].srcObject = stream
    elements["user-stream"].muted = true
    elements["user-stream"].play()
    elements["user-stream"].style.opacity = 1
  })

  dwrtc.on("stream", stream => {
    console.debug("got partner stream")
    hide(elements["id-overlay"])
    elements["partner-stream"].srcObject = stream
    elements["partner-stream"].play()
    elements["partner-stream"].style.opacity = 1
  })

  dwrtc.on("id", id => {
    console.debug(`got id message: ${id}`)
    elements["copy-peer-id-input"].value = id
    elements["copy-peer-id-button"].onclick = event => {
      elements["copy-peer-id-input"].select()
      document.execCommand("copy")
      elements["copy-peer-id-button"].textContent = "Copied!"
    }
    show(elements["id-overlay"])
  })

  dwrtc.on("webSocketError", message => {
    const error = message.error
    console.error(error)
    const errorSuffix =
      "Kindly reload the page and try again with another partner input"

    showError(`${error}. ${errorSuffix}.`)
  })

  dwrtc.on("error", message => {
    console.error(message)
    showError(message)
  })

  await dwrtc.setup()
}

const showError = message => {
  removeAllChildren(elements["error-overlay"])
  const p = document.createElement("p")
  p.appendChild(document.createTextNode(`Error: ${message}`))
  elements["error-overlay"].appendChild(p)
  hide(elements["id-overlay"])
  // TODO hide partner stream? or add background to error div so you can properly read it?
  // Then again, an error may mean that you can still talk to each other, just not signal anymore
  show(elements["error-overlay"])
}
