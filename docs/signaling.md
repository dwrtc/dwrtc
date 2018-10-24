# Package ch.hsr.dsl.dwrtc.signaling

P2P layer. Handles sending and receiving messages through TomP2P.

## Design decisions

### Anti-cheating mechanisms

As we're in a P2P network, it is possible to add bad data. However, we wanted to make this a bit harder, so we at least don't expose it on our API.

* [SignalingMessage.recipientSessionId] and [SignalingMessage.senderSessionId] IDs are overwritten before they go to the TomP2P layer, since they could be faked by WebSocket clients
* [ClientService.removeClient] only accepts an [IInternalClient] (that you got from [ClientService.addClient]), so you cannot easily disconnect another user.
