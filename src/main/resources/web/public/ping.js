"use strict"

const request = url =>
  new Promise((resolve, reject) => {
    const img = new Image()
    const key = Math.random()
      .toString(36)
      .substring(5)

    img.onload = () => resolve(img)
    img.onerror = () => reject(url)
    img.src = `${url}?no-cache=${key}`
  })

const ping = url =>
  new Promise((resolve, _) => {
    const start = new Date().getTime()
    const responseSuccessful = () =>
      resolve({ url, ttl: new Date().getTime() - start, reachable: true })
    const responseFailed = () =>
      resolve({ url, ttl: Number.MAX_SAFE_INTEGER, reachable: false })

    request(url)
      .then(responseSuccessful)
      .catch(responseFailed)

    setTimeout(responseFailed, 2000)
  })

const urlPing = urls => {
  const promises = urls.map(url => ping(url))
  return Promise.all(promises).then(states =>
    states
      .filter(state => state.reachable)
      .sort((state1, state2) => state1.ttl - state2.ttl)
  )
}
