# Package ch.hsr.dsl.dwrtc.util

Various bits and pieces

## TomP2P Extension Classes

To easier work with TomP2P's Futures in Kotlin, we developed a few extension classes.

They are documented in place, but need this bit of documentation to tie them all together

Note, that all these classes employ TomP2P semantics. Think of these as a very thin layer upon TomP2P. E.g., a failure is only raised when no connection can be made. An empty response is no failure to TomP2P. To work more nicely with those responses, have a look at our higher-level [Future] classes. 

`BaseFuture` extension classes: these work on all Futures that TomP2P returns

* [net.tomp2p.futures.BaseFuture.onComplete]
* [net.tomp2p.futures.BaseFuture.onFailure]
* [net.tomp2p.futures.BaseFuture.onSuccess]

`FutureGet` extension classes: these work on all Get operations that TomP2P returns. Also includes all `BaseFuture` extensions.

These methods differ by these orthogonal concepts:

* Do you get one or multiple things? Multiple things have the suffix `All`
* In your method, do you transform the response from the DHT? Transformed responses have the suffix `Custom`
  

* [net.tomp2p.dht.FutureGet.onGet]
* [net.tomp2p.dht.FutureGet.onGetAll]
* [net.tomp2p.dht.FutureGet.onGetAllCustom]
* [net.tomp2p.dht.FutureGet.onGetCustom]

