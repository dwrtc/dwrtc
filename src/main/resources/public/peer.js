"use strict"

const wsProtocol = location.protocol === "https" ? "wss" : "ws" // in a perfect world, it's always wss
const websocketUrl = `${wsProtocol}://${location.host}/ws`

const elements = []
// TODO why does this need to be a named constant? "inline" doesn't seem to work
const elementIds = [
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
]
elementIds.forEach(e => (elements[e] = document.getElementById(e)))

class SignalingMessage {
  constructor(recipientSessionId, messageBody) {
    this.recipientSessionId = recipientSessionId
    this.messageBody = messageBody
  }
}

/**
 * Onload. Setup the page interactions
 */
window.onload = () => {
  elements["connectNormal"].onclick = event => {
    event.preventDefault()
    startDwrtc(false)
  }
  elements["connectToSession"].onclick = event => {
    if (elements["connectToSessionForm"].checkValidity()) {
      // Allow HTML5 form validation to take place
      event.preventDefault()
      startDwrtc(true, elements["otherPeerId"].value)
      hideIdMessage()
    }
  }
  elements["idCopy"].onclick = copyIdToClipboard
  enableInput()
}

const showOutput = () => {
  elements["output"].hidden = false
  // if the css is "display: grid" initially, this overrides the hidden attribute
  // therefore, we have to unhide it and add the proper class
  elements["output"].classList.add("grid")
}

const hideIdMessage = () => {
  elements["idMessage"].hidden = true
}

const showOtherVideo = () => {
  elements["otherVideo"].hidden = false
  hideIdMessage()
}

const copyIdToClipboard = event => {
  event.preventDefault()
  elements["idValue"].select()
  document.execCommand("copy")
  elements["idCopy"].textContent = "Copied!"
}

const enableInput = () => (elements["input"].hidden = false)

const disableInput = () => (elements["input"].hidden = true)

const showError = error => {
  elements["errorOverlay"].hidden = false
  elements["errorOverlay"].classList.add("fade-in")
  elements["errorMessage"].textContent = error
}

/**
 * Called when the user is ready. Sets up DWRTC.
 */
async function startDwrtc(initiator, initialPeerId) {
  console.log("Start DWRTC")
  disableInput()
  showOutput()
  const dwrtc = new DWRTC(initiator, initialPeerId)
  await dwrtc.setup()
}

class DWRTC {
  constructor(isInitiator, initialPeerId) {
    this.isInitiator = isInitiator
    if (this.isInitiator) {
      this.otherPeerId = initialPeerId
    }
    console.log(
      `Started DWRTC with isInitiator: ${isInitiator}, initialPeerId: ${initialPeerId}`
    )
  }

  /**
   * Sets the class up
   * This is a separate method to the constructor, since constructors are not allowed to be async
   */
  async setup() {
    await this.startSimplePeer()
    await this.setupSocket()
    this.completeSimplePeerSetup()
  }

  /**
   * Initialize the peer with the information we have so far
   */
  async startSimplePeer() {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: true,
        audio: true
      })
      elements["yourVideo"].srcObject = stream
      elements["yourVideo"].play()
      elements["yourVideo"].muted = true
      this.peer = new window.SimplePeer({
        initiator: this.isInitiator,
        stream: stream
      })
    } catch (error) {
      throw error
    }
    // TODO https://developer.mozilla.org/en-US/docs/Web/API/MediaDevices/getUserMedia
    // "It's possible for the returned promise to neither resolve nor reject,
    // as the user is not required to make a choice at all and may simply ignore the request."
    console.debug("SimplePeer started")
  }

  /** Initialize the websocket completely */
  async setupSocket() {
    this.socket = new WebSocket(websocketUrl)

    await this.websocketIsReady()
    this.socket.onclose = event => {
      const message = `Websocket closed (Reason ${event.reason}, Code ${
        event.code
      })`
      console.error(message)
      showError(message)
    }

    this.socket.onerror = event => {
      const message = `Websocket errored: (${event})`
      console.error(message)
      showError(message)
    }

    this.socket.onmessage = event => this.handleWebsocketMessage(event)
    console.debug("Websocket set up")
  }

  async websocketIsReady() {
    await new Promise(
      function(resolve, reject) {
        this.socket.onopen = _ => resolve()
      }.bind(this)
    )
  }

  /**
   * Make the Simple Peer configuration complete
   * This needs to run in a separate step than setupSimplePeer(), since we first need to set up the socket
   */
  completeSimplePeerSetup() {
    this.peer.on("signal", data => {
      // Peer wants to send signaling data
      console.debug(`Send Signal message: ${data}`)
      const message = new SignalingMessage(
        this.otherPeerId,
        JSON.stringify(data)
      )
      this.socket.send(JSON.stringify(message))
    })
    this.peer.on("stream", stream => {
      console.log("Got video stream!")
      showOtherVideo()
      elements["otherVideo"].srcObject = stream
      elements["otherVideo"].play()
    })
  }

  /**
   * Dispatch the incoming message
   * @param {Object} rawMessage raw message from the websocket
   */
  handleWebsocketMessage(rawMessage) {
    let message = JSON.parse(rawMessage.data)
    let debugMessage = "New message, type: "
    switch (message.type) {
      case "WebsocketIdMessage":
        console.debug(debugMessage + "WebsocketIdMessage")
        this.handleWebsocketIdMessage(message)
        break
      case "WebsocketErrorMessage":
        console.debug(debugMessage + "WebsocketErrorMessage")
        this.handleWebsocketErrorMessage(message)
        break
      case "SignalingMessage":
        console.debug(debugMessage + "SignalingMessage")
        this.handleWebsocketSignalingMessage(message)
        break
      default:
        console.error(debugMessage + "UNKNOWN")
    }
  }

  /**
   * Handle an ID message
   * @param {Object} message an ID message
   * @param {String} message.id the new ID
   */
  handleWebsocketIdMessage(message) {
    const id = message.id
    console.debug(`ID: ${id}`)
    elements["idValue"].value = id
  }

  /**
   * Handle an error message
   * @param {Object} message an error message
   * @param {string} message.error the error reason
   */
  handleWebsocketErrorMessage(message) {
    const error = message.error
    console.error(error)
    const errorSuffix =
      "Kindly reload the page and try again with another input"
    showError(`${error}. ${errorSuffix}.`)
  }

  /**
   * Handle a signaling message
   * @param {SignalingMessage} message a signaling message
   */
  handleWebsocketSignalingMessage(message) {
    console.debug(
      `Sender: ${message.senderSessionId}, Recipient: ${
        message.recipientSessionId
      }, Message: ${message.messageBody}`
    )
    this.otherPeerId = message.senderSessionId
    console.debug(`Set otherPeerId to ${this.otherPeerId}`)
    const data = JSON.parse(message.messageBody)
    // Send received message to our peer
    this.peer.signal(data)
  }
}
