import _ from 'lodash'

function classNames(...classNames) {
  return classNames.filter(className => className).join(' ')
}

function trim(str) {
  return str !== null && str !== undefined && str.trim().length > 0
    ? str.trim()
    : null
}

function empty(str) {
  return str === null || str === undefined
}

function filter(objects, value, fields) {
  if (value && value.trim().length > 0) {
    const theValue = value.trim().toUpperCase()
    return objects.filter(object => {
      return fields.some(field => {
        const objectValue = String(_.get(object, field, '')).toUpperCase()
        return objectValue.includes(theValue)
      })
    })
  } else {
    return objects
  }
}

export default {
  classNames,
  trim,
  empty,
  filter
}
