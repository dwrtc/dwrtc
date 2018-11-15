"use strict"

const elements = getElementsArrayById([
  "connectNormal",
  "otherPeerId",
  "connectToSession",
  "connectToSessionForm",
  "input",
  "output",
  "idMessage",
  "idValue",
  "idCopy",
  "yourVideo",
  "otherVideo",
  "errorOverlay",
  "errorMessage"
])

/**
 * Onload. Setup the page interactions
 */
window.onload = () => {
  elements["connectNormal"].onclick = event => {
    event.preventDefault()
    startDwrtc(false)
    hide(elements["idMessage"])
  }
  elements["connectToSession"].onclick = event => {
    if (elements["connectToSessionForm"].checkValidity()) {
      // Allow HTML5 form validation to take place
      event.preventDefault()
      startDwrtc(true, elements["otherPeerId"].value)
      hide(elements["idMessage"])
    }
  }
  elements["idCopy"].onclick = copyIdToClipboard
  show(elements["input"])
}

const showOutput = () => {
  show(elements["output"])
  // if the css is "display: grid" initially, this overrides the hidden attribute
  // therefore, we have to unhide it and add the proper class
  elements["output"].classList.add("grid")
}

const copyIdToClipboard = event => {
  event.preventDefault()
  elements["idValue"].select()
  document.execCommand("copy")
  elements["idCopy"].textContent = "Copied!"
}

/**
 * Called when the user is ready. Sets up DWRTC.
 */
async function startDwrtc(initiator, initialPeerId) {
  const wsProtocol = location.protocol === "https:" ? "wss" : "ws" // in a perfect world, it's always wss
  const webSocketUrl = `${wsProtocol}://${location.host}/ws`

  console.log("Start DWRTC")
  hide(elements["input"])
  showOutput()
  const dwrtc = new DWRTC(
    initiator,
    initialPeerId,
    webSocketUrl,
    elements["yourVideo"],
    elements["idValue"],
    elements["idMessage"],
    elements["errorOverlay"]
  )
  await dwrtc.setup()
}
