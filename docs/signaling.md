# Package ch.hsr.dsl.dwrtc.signaling

P2P layer. Handles sending and receiving messages through TomP2P.

## Design decisions

* [SignalingMessage]'s [recipient][SignalingMessage.recipientSessionId] and [sender][SignalingMessage.senderSessionId] IDs are overwritten before they go to the TomP2P layer, since they could be faked by WebSocket clients
