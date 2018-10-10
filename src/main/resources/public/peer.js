"use strict"

class SignalingMessage {
  constructor(recipientSessionId, messageBody) {
    this.recipientSessionId = recipientSessionId
    this.messageBody = messageBody
  }
}

let id
window.onload = () => {
  const websocketUrl = "ws://localhost:7000/ws"
  let simplePeer = window.SimplePeer
  let socket = new WebSocket(websocketUrl)
  socket.onopen = _ => setupSocket(socket)
}

const setupSocket = socket => {
  socket.onmessage = event => onMessage(event)
  socket.onclose = event =>
    console.log(`OnClose (Reason ${event.reason}, Code ${event.code})`)
  socket.onerror = event => console.log(`OnError ${event}`)

  let message = new SignalingMessage(
    "d3ceaa27-d260-4fcb-aa3a-8039e43c44d1",
    "Hallo"
  )
  socket.send(JSON.stringify(message))
}

const onMessage = event => {
  let message = JSON.parse(event.data)
  switch (message.type) {
    case "WebsocketIdMessage":
      id = message.id
      break
    case "WebsocketErrorMessage":
      console.error(message.error)
      break
  }
}
