# dwrtc.js

JS helper for the [DWRTC server](https://github.com/dwrtc/dwrtc) to initiate WebRTC connections over a P2P network.

## Prerequisites

A running [DWRTC server](https://github.com/dwrtc/dwrtc) (check [dwrtc.net](https://dwrtc.net) for available servers or deploy your own) and [simple-peer](https://github.com/feross/simple-peer) are required.

## Usage

```html
<html>
  <body>
    <div class="container"></div>

    <script src="simple-peer.js"></script>
    <script src="dwrtc.js"></script>
    <script>
        const initiator = true
        const partnerId = null
        const webSocketUrl = "wss://node1.dwrtc.net/ws"

        const dwrtc = new DWRTC(initiator, partnerId, webSocketUrl)

        dwrtc.on("started", stream => {})
        dwrtc.on("stream", stream => {})
        dwrtc.on("id", id => {})
        dwrtc.on("webSocketError", message => {})
        dwrtc.on("error", message => {})

        await dwrtc.setup()
    </script>
  </body>
</html>
```

## Constructor Arguments

### initiator

The session which initiates the call is the initiator. Only one person per session is allowed to be the initiator.

### partnerId

Defines the initiators id if the current session is not the initiator.

### webSocketUrl

The WebSocket URL of the DWRTC server to connect to. Check [dwrtc.net](dwrtc.net) for available servers or deploy your own.

## Events

### started

```js
dwrtc.on("started", stream => {})
```

Returns the users video stream. Fires as soon as a new session has been started.

### stream

```js
dwrtc.on("stream", stream => {})
```

Returns the partners video stream. Fires as soon as the partners stream is available.

### id

```js
dwrtc.on("id", id => {})
```

Returns the client's ID as soon as the web socket connection is initiated.

### webSocketError

```js
dwrtc.on("webSocketError", message => {})
```

Returns the error message. Fires on an error with a web socket connection, e.g. a lost connection.

### error

```js
dwrtc.on("error", message => {})
```

Returns the error message. Fires on unknown errors.
