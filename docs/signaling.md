# Package ch.hsr.dsl.dwrtc.signaling

P2P layer. Handles sending and receiving messages through TomP2P.

An instance of [ClientService] is needed. All operations are done through this.

Specifically, one can [add a new client][ClientService.addClient]. This will return an [InternalClient]. A message listener can then be added to this client, which will relay the messages the client receives via the TomP2P layer.

To connect to other clients, [find clients][ClientService.findClient]. This will return an [ExternalClient]. An [InternalClient] can then be used to [send messages][InternalClient.sendMessage] to this ExternalClient. (This will call the message listener on the other peer's InternalClient).

## Design decisions

### User Input 

* [ClientMessage.recipientSessionId] and [ClientMessage.senderSessionId] IDs are overwritten before they are sent to the TomP2P layer.
* [ClientService.removeClient] only accepts an [IInternalClient] (that was received from [ClientService.addClient]), so one cannot disconnect another user via the API.

### Messaging Format

The [Message] class uses a `type` as its discriminator. The availability of other fields depends on the `type`. This allows for a very flexible format that also ensures type-safe casting .

Developers using DWRTC can define their own message types with the payload residing in the message body (see XXX)


### Bootstrapping

The [ClientService] class supports two bootstrapping mechanisms: bootstrapping with a given TomP2P `PeerAddress` and bootstrapping using a normal IP/port pair (using [PeerConnectionDetails])

* The `PeerAddress` bootstrap mechanism is meant for tests, when the peer's address is already available in the correct, technical format
* The IP/port pair bootstrap mechanism is meant for user input

### ClientService

The [ClientService] class is the one-stop starting point for all P2P/DHT operations. It is a service object that creates and bootstraps the TomP2P peer. All objects are created through methods of this object.

### Futures

The [signaling] layer contains the high-level [Future]s. These are a rework of the TomP2P `Future`s.

The extension classes that build right on top of TomP2P are available in the the [util][ch.hsr.dsl.dwrtc.util] layer. The [Future] classes build on top of these.

### InternalClient/ExternalClient

An [InternalClient] is created when a new WebSocket session is started. It is able to send and receive messages. 
An [ExternalClient] is created when an [InternalClient] wants to send messages to it.
An [ExternalClient] can only receive messages. On the other peer, the messages are then routed to a corresponding [InternalClient].

Note: an [ExternalClient] *can* be on the same server.

### Message Routing

Whenever an [InternalClient] is created the [ClientService] registers its session ID in a message dispatcher table. The dispatcher then sends all the received messages to the correct [InternalClient] 


### Interfaces

All classes define an interface. These interfaces allow the substitution of the underlying P2P layer. The interface is agnostic to the P2P layer.
