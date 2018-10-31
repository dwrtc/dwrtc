# Package ch.hsr.dsl.dwrtc.util

Various bits and pieces

## TomP2P Extension Classes

To easier work with TomP2P's Futures in Kotlin, this projects includes a few extension classes.

They are documented in place, but need this bit of documentation to tie them all together

Note, that all these classes employ TomP2P semantics. Think of these as a very thin layer upon TomP2P. E.g., a failure is only raised when no connection can be made. An empty response is no failure to TomP2P. To work more nicely with those responses, have a look at the higher-level [Future] classes. 

`BaseFuture` extension classes: these work on all Futures that TomP2P returns

* [net.tomp2p.futures.BaseFuture.onComplete]
* [net.tomp2p.futures.BaseFuture.onFailure]
* [net.tomp2p.futures.BaseFuture.onSuccess]

`FutureGet` extension classes work on all Get operations that TomP2P returns. Also includes all `BaseFuture` extensions.

These fire on `BaseFuture`'s `onComplete`. Therefore, the success status still needs to be checked.

These methods differ by these orthogonal concepts:

* Get one or multiple things? Multiple things have the suffix `All`
* Is the response from the DHT transformed before returning it? Transformed responses have the suffix `Custom`
  * E.g., the DHT contains a `UserId`, but a `User` object is returned in the calling method. 
  

* [net.tomp2p.dht.FutureGet.onGet]
* [net.tomp2p.dht.FutureGet.onGetAll]
* [net.tomp2p.dht.FutureGet.onGetAllCustom]
* [net.tomp2p.dht.FutureGet.onGetCustom]

