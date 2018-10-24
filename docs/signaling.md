# Package ch.hsr.dsl.dwrtc.signaling

P2P layer. Handles sending and receiving messages through TomP2P.

## Design decisions

* [SignalingMessage.recipientSessionId] and [SignalingMessage.senderSessionId] IDs are overwritten before they go to the TomP2P layer, since they could be faked by WebSocket clients
* [ClientService.removeClient] only accepts an [IInternalClient] (that you got from [ClientService.addClient]), so you cannot easily disconnect another user.
