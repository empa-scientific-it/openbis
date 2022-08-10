import _ from 'lodash'

function format(value) {
  if (value === null) {
    return ''
  }

  var date = null

  if (_.isDate(value)) {
    date = value
  } else if (_.isNumber(value)) {
    date = new Date(value)
  } else {
    throw new Error('Incorrect date: ' + value)
  }

  const year = String(date.getFullYear()).padStart(4, '0')
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')

  return (
    year + '-' + month + '-' + day + ' ' + hours + ':' + minutes + ':' + seconds
  )
}

function inRange(value, from, to) {
  if (from || to) {
    if (value === null || value === undefined) {
      return false
    }
    if (from && value.getTime() < from.getTime()) {
      return false
    }
    if (to && value.getTime() > to.getTime()) {
      return false
    }
  }
  return true
}

function timezone() {
  return -(new Date().getTimezoneOffset() / 60)
}

export default {
  format,
  timezone,
  inRange
}
