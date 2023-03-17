import _ from 'lodash'
import messages from './messages'

const MILLIS_PER_SECOND = 1000
const MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND
const MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE
const MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR
const MILLIS_PER_YEAR = 365 * MILLIS_PER_DAY

function format(value) {
  if (_.isNil(value)) {
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

  for (let i = 0; i < units.length; i++) {
    const unit = units[i]
    if (millis >= unit.millisPerUnit * 0.99) {
      const value = Math.round(millis / unit.millisPerUnit)
      return messages.get(unit.message, value)
    }
  }

  return '0'
}

function inRange(value, from, to) {
  if (from || to) {
    if (_.isNil(value)) {
      return false
    }

    var time = null

    if (_.isNumber(value)) {
      time = value
    } else if (_.isDate(value)) {
      time = value.getTime()
    } else {
      return false
    }

    if (from && time < from.getTime()) {
      return false
    }
    if (to && time > to.getTime()) {
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
