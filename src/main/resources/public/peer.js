"use strict"

class SignalingMessage {
  constructor(recipientSessionId, messageBody) {
    this.recipientSessionId = recipientSessionId
    this.messageBody = messageBody
  }
}

let id
let socket

window.onload = () => {
  const websocketUrl = "ws://localhost:7000/ws"
  let simplePeer = window.SimplePeer
  socket = new WebSocket(websocketUrl)
  socket.onopen = _ => setupSocket()
}

const setupSocket = () => {
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
  setupPeer()
}

const onMessage = (
  event,
  onWebsocketIdMessage,
  onWebsocketErrorMessage,
  onSignalingMessage
) => {
  let message = JSON.parse(event.data)
  switch (message.type) {
    case "WebsocketIdMessage":
      onWebsocketIdMessage(message)
      break
    case "WebsocketErrorMessage":
      onWebsocketErrorMessage(message)
      break
    case "SignalingMessage":
      onSignalingMessage(message)
      break
  }
}

const onWebsocketIdMessage = message => {
  id = message.id
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

const setupPeer = () => {
  let message = new SignalingMessage(
    "d9c3c68f-64ed-4abb-9009-3f0f044adfda",
    "Blablabla"
  )
  socket.send(JSON.stringify(message))
}
