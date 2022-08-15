import _ from 'lodash'
import messages from './messages'

const MILLIS_PER_SECOND = 1000
const MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND
const MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE
const MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR
const MILLIS_PER_YEAR = 365 * MILLIS_PER_DAY

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

function duration(millis) {
  const units = [
    { millisPerUnit: MILLIS_PER_YEAR, message: messages.YEAR_OR_YEARS },
    { millisPerUnit: MILLIS_PER_DAY, message: messages.DAY_OR_DAYS },
    { millisPerUnit: MILLIS_PER_HOUR, message: messages.HOUR_OR_HOURS },
    { millisPerUnit: MILLIS_PER_MINUTE, message: messages.MINUTE_OR_MINUTES },
    { millisPerUnit: MILLIS_PER_SECOND, message: messages.SECOND_OR_SECONDS }
  ]

  let text = ''

  units.forEach(unit => {
    if (millis > unit.millisPerUnit) {
      const value = millis / unit.millisPerUnit
      if (text.length > 0) {
        text += ' '
      }
      text += messages.get(unit.message, value)
      millis = millis % unit.millisPerUnit
    }
  })

  return text
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
  duration,
  timezone,
  inRange
}
