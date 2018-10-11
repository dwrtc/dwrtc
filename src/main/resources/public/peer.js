"use strict"

class SignalingMessage {
  constructor(recipientSessionId, messageBody) {
    this.recipientSessionId = recipientSessionId
    this.messageBody = messageBody
  }
}

let socket

window.onload = () => {
  simplePeer = window.SimplePeer
  setupSocket()
}

const setupSocket = () => {
  console.debug("Setting up websocket")
  const websocketUrl = "ws://localhost:7000/ws"
  socket = new WebSocket(websocketUrl)
  socket.onmessage = event =>
    onMessage(
      event,
      onWebsocketIdMessage,
      onWebsocketErrorMessage,
      onSignalingMessage
    )
  socket.onclose = event =>
    console.log(`OnClose (Reason ${event.reason}, Code ${event.code})`)
  socket.onerror = event => console.log(`OnError ${event}`)
  console.debug("Setting up websocket COMPLETE")
  socket.onopen = event => onWebsocketOpen(event)
}

const onMessage = (
  event,
  onWebsocketIdMessage,
  onWebsocketErrorMessage,
  onSignalingMessage
) => {
  let message = JSON.parse(event.data)
  let debugMessage = "New message, type: "
  switch (message.type) {
    case "WebsocketIdMessage":
      console.debug(debugMessage + "WebsocketIdMessage")
      onWebsocketIdMessage(message)
      break
    case "WebsocketErrorMessage":
      console.debug(debugMessage + "WebsocketErrorMessage")
      onWebsocketErrorMessage(message)
      break
    case "SignalingMessage":
      console.debug(debugMessage + "SignalingMessage")
      onSignalingMessage(message)
      break
    default:
      console.error(debugMessage + "UNKNOWN")
  }
}

const onWebsocketIdMessage = message => {
  const id = message.id
  console.log(`ID: ${id}`)
}

const onWebsocketErrorMessage = message => {
  console.error(message.error)
}

const onSignalingMessage = message => {
  console.log(
    `Message. Sender: ${message.senderSessionId}, Recipient: ${
      message.recipientSessionId
    }, Message: ${message.messageBody}`
  )
}

const onWebsocketOpen = event => {
  console.debug("Websocket is open")
  // sendTestMessage();
  setPageReady()
}

const connectClicked = event => {
  event.preventDefault()
  console.debug("Connect is clicked")
  setupPeer()
}
const setupPeer = () => {
  const simplePeer = window.SimplePeer

  console.debug("Setting up SimplePeer")
  const isInitiator = document.getElementById("initiator").checked
  const otherPeerId = document.getElementById("otherPeerId").value
  console.debug(`Is initiator? ${isInitiator}`)
  console.debug(`Other Peer ID: ${otherPeerId}`)

  console.debug("Setting up SimplePeer COMPLETE")
}

const setPageReady = () => {
  console.debug("Page is ready")
  const connectButton = document.getElementById("connect")
  connectButton.onclick = connectClicked
  connectButton.disabled = false
}

function sendTestMessage() {
  let message = new SignalingMessage(
    "9c445770-deaa-4f8c-8e6e-60c575515ed4",
    "Blablabla"
  )
  socket.send(JSON.stringify(message))
}
