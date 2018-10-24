# Package ch.hsr.dsl.dwrtc.signaling

P2P layer. Handles sending and receiving messages through TomP2P.

You need an instance of [ClientService]. Through this, you will do all your operations.

Specifically, you can [add a new client][ClientService.addClient]. This will get you an [InternalClient]. You then add a message listener to this client, which will relay the messages you receive via the TomP2P layer.

To connect to other clients, you [find clients][ClientService.findClient]. This will get you an [ExternalClient]. You can then use an InternalClient to [send messages][InternalClient.sendMessage] to this ExternalClient. (This will call the message listener on the other peer's InternalClient you're sending messages to.)

## Design decisions

### Anti-cheating mechanisms

As we're in a P2P network, it is possible to add bad data. However, we wanted to make this a bit harder, so we at least don't expose it on our API.

* [SignalingMessage.recipientSessionId] and [SignalingMessage.senderSessionId] IDs are overwritten before they go to the TomP2P layer, since they could be faked by WebSocket clients
* [ClientService.removeClient] only accepts an [IInternalClient] (that you got from [ClientService.addClient]), so you cannot easily disconnect another user.
