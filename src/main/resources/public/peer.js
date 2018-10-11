"use strict"

class SignalingMessage {
  constructor(recipientSessionId, messageBody) {
    this.recipientSessionId = recipientSessionId
    this.messageBody = messageBody
  }
}

let socket
let peer
let otherPeerId

window.onload = () => {
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
  otherPeerId = message.senderSessionId
  console.debug(`Set otherPeerId to ${otherPeerId}`)
  const data = JSON.parse(message.messageBody)
  // Send received message to our peer
  peer.signal(data)
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
  const SimplePeer = window.SimplePeer

  console.debug("Setting up SimplePeer")
  const isInitiator = document.getElementById("initiator").checked
  console.debug(`Is initiator? ${isInitiator}`)
  if (isInitiator) {
    console.debug("Setting initial otherPeerId")
    otherPeerId = document.getElementById("otherPeerId").value
    console.debug(`Other Peer ID: ${otherPeerId}`)
  }
  navigator.mediaDevices
    .getUserMedia({ video: true, audio: true })
    .then(function(stream) {
      console.debug("Got user media")
      peer = new SimplePeer({ initiator: isInitiator, stream: stream })

      peer.on("signal", data => {
        // Peer wants to send signalling data
        console.debug(`Send Signal message: ${data}`)
        const message = new SignalingMessage(otherPeerId, JSON.stringify(data))
        socket.send(JSON.stringify(message))
      })
      peer.on("stream", function(stream) {
        console.log("Got video stream!")
        var video = document.querySelector("video")
        video.src = window.URL.createObjectURL(stream)
        video.play()
      })
    })
    .catch(function(err) {
      console.error(`Could not get user media or other error in setup, ${err}`)
    })
  // TODO https://developer.mozilla.org/en-US/docs/Web/API/MediaDevices/getUserMedia
  // "It's possible for the returned promise to neither resolve nor reject,
  // as the user is not required to make a choice at all and may simply ignore the request."
  console.debug("Setting up SimplePeer COMPLETE")
}

const setPageReady = () => {
  console.debug("Page is ready")
  const connectButton = document.getElementById("connect")
  connectButton.onclick = connectClicked
  connectButton.disabled = false
}

const sendTestMessage = () => {
  let message = new SignalingMessage(
    "9c445770-deaa-4f8c-8e6e-60c575515ed4",
    "Blablabla"
  )
  socket.send(JSON.stringify(message))
}
