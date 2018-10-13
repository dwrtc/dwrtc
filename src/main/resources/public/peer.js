"use strict"

const wsProtocol = location.protocol === "https" ? "wss" : "ws" // in a perfect world, it's always wss
const WEBSOCKET_URL = `${wsProtocol}://${location.host}/ws`

const connectNormalButton = document.getElementById("connectNormal")
const initialOtherPeerIdInput = document.getElementById("otherPeerId")
const connectToSessionButton = document.getElementById("connectToSession")

const input = document.getElementById("input")
const output = document.getElementById("output")
const idMessage = document.getElementById("idMessage")
const idValue = document.getElementById("idValue")
const yourVideo = document.getElementById("yourVideo")
const otherVideo = document.getElementById("otherVideo")

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
  connectNormalButton.onclick = event => {
    event.preventDefault
    startDwrtc(false)
  }
  connectToSessionButton.onclick = event => {
    event.preventDefault
    startDwrtc(true, initialOtherPeerIdInput.value)
  }
  enableInput()
}

const showOutput = () => {
  output.hidden = false
  // if the css is "display: grid" initially, this overrides the hidden attribute
  // therefore, we have to unhide it and add the proper class
  output.classList.add("grid")
}

const showOtherVideo = () => {
  otherVideo.hidden = false
  idMessage.hidden = true
}

const enableInput = () => (input.hidden = false)

const disableInput = () => (input.hidden = true)

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
      yourVideo.srcObject = stream
      yourVideo.play()
      yourVideo.muted = true
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
    this.socket = new WebSocket(WEBSOCKET_URL)

    // Dummy promise that we can resolve when the websocket is open
    await new Promise(
      function(resolve, reject) {
        this.socket.onopen = _ => resolve()
      }.bind(this)
    )
    this.socket.onclose = event =>
      console.log(
        `Websocket closed (Reason ${event.reason}, Code ${event.code})`
      )
    this.socket.onerror = event => console.log(`Websocket errored: (${event})`)

    this.socket.onmessage = event => this.handleWebsocketMessage(event)
    console.debug("Websocket set up")
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
    this.peer.on("stream", function(stream) {
      console.log("Got video stream!")
      showOtherVideo()
      otherVideo.srcObject = stream
      otherVideo.play()
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
    idValue.textContent = id
  }

  /**
   * Handle an error message
   * @param {Object} message an error message
   * @param {string} message.error the error reason
   */
  handleWebsocketErrorMessage(message) {
    console.error(message.error)
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
