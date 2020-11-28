function getApplicationPath() {
  return window.location.pathname.substr(
    0,
    window.location.pathname.lastIndexOf('/') + 1
  )
}

export default {
  getApplicationPath
}
