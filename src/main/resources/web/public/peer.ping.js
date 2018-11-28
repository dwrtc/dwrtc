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
  new Promise((resolve, reject) => {
    const start = new Date().getTime()
    const response = () => resolve(new Date().getTime() - start)

    request(url)
      .then(response)
      .catch(response)

    setTimeout(() => {
      reject(Error("timeout"))
    }, 2000)
  })
