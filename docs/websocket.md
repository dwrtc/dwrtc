# Package ch.hsr.dsl.dwrtc.websocket

UI layer. Handles sending and receiving messages from the WebSocket connections.

## Design Decisions

### Message format

The [Message] format is re-used.

### Few Components

The [WebSocketHandler] consists of four main components:

* [WebSocketHandler.clients] is a map of session ID to [InternalClient]s
* [WebSocketHandler.sessions] is map of session ID to WebSocket sessions
* [WebSocketHandler.onReceiveMessageFromWebSocket] uses the session ID to get the [InternalClient]. This is then used to send a message through the P2P layer
* [WebSocketHandler.onReceiveMessageFromSignaling] uses the session ID to get the WebSocket session. This is then used to send the message to the specific WebSocket

## API Doc

When connected to the WebSocket, an implementation can send or receive the following messages.

### Send

* `SignalingMessage`. Send a black box signaling message to another peer.
  * `recipientSessionId: String`. The recipient's session ID. Used for routing.
  * `messageBody: String`. The free-form message body. To the transport layer, this is a black box.
  
### Receive

All these incoming message types have to be handled. They are distinguishable by their `type` field.
``
* `WebSocketIdMessage`. Includes the session ID.
  * `type: String`. Static value `WebSocketIdMessage`
  * `id: String`. The session ID
* `WebSocketErrorMessage`. Tells the implementation that something went wrong
  * `type: String`. Static value `WebSocketErrorMessage`
  * `error: String`. The error message
* `SignalingMessage`. Incoming, signaling messages
  * `type: String`. Static value `SignalingMessage`
  * `senderSessionId: String`. The sender' session ID. Can be used to reply to messages
  * `recipientSessionId: String`. The recipient's session ID. This should be the current session ID!
  * `messageBody: String`. The free-form message body
