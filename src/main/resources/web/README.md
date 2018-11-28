# dwrtc.js

JS helper for the [DWRTC server](https://github.com/dwrtc/dwrtc) to initiate WebRTC connections over a P2P network.

## Prerequisites

To be able to run dwrtc.js a running [DWRTC server](https://github.com/dwrtc/dwrtc) (check [dwrtc.net](dwrtc.net) for available servers or deploy your own) and [simple-peer](https://github.com/feross/simple-peer) are required.

## Usage

```html
<html>
  <body>
    <div class="container"></div>

    <script src="simple-peer.js"></script>
    <script src="dwrtc.js"></script>
    <script>
        const dwrtc = new DWRTC(initiator, initialPeerId, webSocketUrl)

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
dwrtc.on("webSocketError", stream => {})
```

Returns the failing stream. Fires on an error with a web socket connection as a lost connection.

### error

```js
dwrtc.on("error", stream => {})
```

Returns the failing stream. Fires on unknow errors.
