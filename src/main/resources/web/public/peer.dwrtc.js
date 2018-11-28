"use strict"

class EventDispatcher {
  constructor() {
    this.events = []
  }

  on(event, callback) {
    const handlers = this.events[event] || []
    handlers.push(callback)
    this.events[event] = handlers
  }

  trigger(event, data) {
    const handlers = this.events[event] || []
    handlers.forEach(handler => handler(data))
  }
}

class SignalingMessage {
  constructor(recipientSessionId, messageBody) {
    this.type = "SignalingMessage"
    this.recipientSessionId = recipientSessionId
    this.messageBody = messageBody
  }
}

/**
 * DWRTC class to be used with it's corresponding server.
 * 
 * Initiates a WebRTC session over a P2P network.
 */
class DWRTC {
  constructor(isInitiator, initialPeerId, webSocketUrl) {
    this.isInitiator = isInitiator
    if (this.isInitiator) this.otherPeerId = initialPeerId
    this.webSocketUrl = webSocketUrl
    this.dispatcher = new EventDispatcher()

    console.log(
      `Started DWRTC with isInitiator: ${isInitiator}, initialPeerId: ${initialPeerId}`
    )
  }

  /**
   * Register on event
   * Uses the EventDispatcher to handle events.
   */
  on(key, emitter) {
    this.dispatcher.on(key, emitter)
  }

  /**
   * Sets the class up
   * This is a separate method to the constructor, since constructors are not allowed to be async
   */
  async setup() {
    await this.setupSocket()
    await this.startSimplePeer()
  }

  /**
   * Get video / audio stream
   */
  getStream(videoEnabled = true, audioEnabled = true) {
    let stream
    try {
      stream = navigator.mediaDevices.getUserMedia({
        video: videoEnabled,
        audio: audioEnabled
      })
    } catch (error) {
      throw error
    }
    return stream
  }

  /**
   * Initialize the peer
   */
  async startSimplePeer() {
    const stream = await this.getStream()

    this.peer = new window.SimplePeer({
      initiator: this.isInitiator,
      stream: stream
    })

    this.peer.on("signal", data => {
      // Peer wants to send signaling data
      console.debug(`Send Signal message: ${JSON.stringify(data)}`)
      const message = new SignalingMessage(
        this.otherPeerId,
        JSON.stringify(data)
      )
      this.socket.send(JSON.stringify(message))
    })

    this.peer.on("stream", stream => {
      this.dispatcher.trigger("stream", stream)
    })

    this.dispatcher.trigger("started", stream)

    console.debug("SimplePeer started")
  }

  /** Initialize the websocket completely */
  async setupSocket() {
    this.socket = new WebSocket(this.webSocketUrl)

    this.socket.onmessage = event => this.handleWebSocketMessage(event)

    this.socket.onclose = event => {
      const message = `Websocket closed (Reason ${event.reason}, Code ${
        event.code
      })`
      this.dispatcher.trigger("error", message)
    }

    this.socket.onerror = event => {
      const message = `Websocket errored: (${event})`
      this.dispatcher.trigger("error", message)
    }

    await this.webSocketIsReady()
    console.debug("Websocket set up")
  }

  async webSocketIsReady() {
    await new Promise(
      function(resolve, reject) {
        this.socket.onopen = _ => resolve()
      }.bind(this)
    )
  }

  /**
   * Dispatch the incoming message
   * @param {Object} rawMessage raw message from the websocket
   */
  handleWebSocketMessage(rawMessage) {
    let message = JSON.parse(rawMessage.data)
    let debugMessage = "New message, type: "
    switch (message.type) {
      case "WebSocketIdMessage":
        console.debug(`${debugMessage} WebSocketIdMessage`)
        this.handleWebSocketIdMessage(message)
        break
      case "WebSocketErrorMessage":
        console.debug(`${debugMessage} WebSocketErrorMessage`)
        this.dispatcher.trigger("webSocketError", message)
        break
      case "SignalingMessage":
        console.debug(`${debugMessage} SignalingMessage`)
        this.handleWebSocketSignalingMessage(message)
        break
      default:
        console.error(
          `${debugMessage} UNKNOWN type (${message.type}): ${JSON.stringify(
            message
          )}`
        )
    }
  }

  /**
   * Handle an ID message
   * @param {Object} message an ID message
   * @param {String} message.id the new ID
   */
  handleWebSocketIdMessage(message) {
    this.dispatcher.trigger("idMessage", message)
  }

  /**
   * Handle an error message
   * @param {Object} message an error message
   * @param {string} message.error the error reason
   */
  handleWebSocketErrorMessage(message) {
    const error = message.error
    console.error(error)
    const errorSuffix =
      "Kindly reload the page and try again with another input"
    showError(`${error}. ${errorSuffix}.`, this.errorOverlayElement)
  }

  /**
   * Handle a signaling message
   * @param {SignalingMessage} message a signaling message
   */
  handleWebSocketSignalingMessage(message) {
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
