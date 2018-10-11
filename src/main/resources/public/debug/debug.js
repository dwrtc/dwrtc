window.onload = () => {
  const SimplePeer = window.SimplePeer
  const connectButton = document.getElementById("connect")
  connectButton.disabled = false
  connectButton.onclick = connectClicked
}

const connectClicked = event => {
  event.preventDefault()
  navigator.mediaDevices
    .getUserMedia({ video: true, audio: true })
    .then(function(stream) {
      console.debug("Got user media")
      peer = new SimplePeer({ initiator: false, stream: stream })
      peer.on("signal", data => {
        // Peer wants to send signalling data
        console.debug(`Send Signal message: ${data}`)
      })
    })
    .catch(function(err) {
      console.error(`Could not get user media or other error in setup, ${err}`)
    })
}
