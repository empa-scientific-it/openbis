// Functions for working with cookies (see http://www.quirksmode.org/js/cookies.html)

const OPENBIS_COOKIE = 'openbis'

function create(name, value, days) {
  let expires = ''

  if (days) {
    var date = new Date()
    date.setTime(date.getTime() + days * 24 * 60 * 60 * 1000)
    expires = '; expires=' + date.toGMTString()
  }

  document.cookie = name + '=' + value + expires + '; path=/'
}

function read(name) {
  var nameEQ = name + '='
  var ca = document.cookie.split(';')
  for (var i = 0; i < ca.length; i++) {
    var c = ca[i]
    while (c.charAt(0) === ' ') c = c.substring(1, c.length)
    if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length)
  }
  return null
}

function erase(name) {
  create(name, '', -1)
}

export default {
  OPENBIS_COOKIE,
  create,
  read,
  erase
}
