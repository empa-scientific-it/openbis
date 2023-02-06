import _ from 'lodash'
import messages from '@src/js/common/messages.js'

const CODE_PATTERN = /^[A-Z0-9_\-.]+$/i
const INTERNAL_CODE_PATTERN = /^\$[A-Z0-9_\-.]+$/i
const TERM_CODE_PATTERN = /^[A-Z0-9_\-.:]+$/i
const USER_CODE_PATTERN = /^[A-Z0-9_\-.@]+$/i

class FormValidator {
  static MODE_BASIC = 'basic'
  static MODE_FULL = 'full'

  constructor(mode) {
    this.mode = mode
    this.errors = new Map()
  }

  isBasicMode() {
    return this.mode === FormValidator.MODE_BASIC
  }

  isFullMode() {
    return this.mode === FormValidator.MODE_FULL
  }

  validateNotEmpty(object, name, label) {
    if (!this.isFullMode()) {
      return
    }

    const field = object[name]

    if (
      field.value === null ||
      field.value === undefined ||
      (_.isString(field.value) && field.value.trim() === '')
    ) {
      this.addError(
        object,
        name,
        messages.get(messages.VALIDATION_CANNOT_BE_EMPTY, label)
      )
    }
  }

  validateDateNotEmpty(object, name, label) {
    if (!this.isFullMode()) {
      return
    }

    const field = object[name]

    if (
      field.value === null ||
      field.value === undefined ||
      field.value.dateObject === null ||
      field.value.dateObject === undefined
    ) {
      this.addError(
        object,
        name,
        messages.get(messages.VALIDATION_CANNOT_BE_EMPTY, label)
      )
    }
  }

  validatePattern(object, name, error, pattern) {
    const field = object[name]

    if (
      field.value === null ||
      field.value === undefined ||
      field.value.trim() === ''
    ) {
      return
    } else {
      if (!pattern.test(field.value)) {
        this.addError(object, name, error)
      }
    }
  }

  validateCode(object, name, label) {
    this.validatePattern(
      object,
      name,
      messages.get(messages.VALIDATION_CODE_PATTERN, label),
      CODE_PATTERN
    )
  }

  validateInternalCode(object, name, label) {
    this.validatePattern(
      object,
      name,
      messages.get(messages.VALIDATION_INTERNAL_CODE_PATTERN, label),
      INTERNAL_CODE_PATTERN
    )
  }

  validateTermCode(object, name, label) {
    this.validatePattern(
      object,
      name,
      messages.get(messages.VALIDATION_TERM_CODE_PATTERN, label),
      TERM_CODE_PATTERN
    )
  }

  validateUserCode(object, name, label) {
    this.validatePattern(
      object,
      name,
      messages.get(messages.VALIDATION_USER_CODE_PATTERN, label),
      USER_CODE_PATTERN
    )
  }

  addError(object, name, error) {
    let objectErrors = this.errors.get(object)

    if (!objectErrors) {
      objectErrors = []
      this.errors.set(object, objectErrors)
    }

    objectErrors.push({ object, name, error })
  }

  getErrors(object) {
    if (object) {
      return this.errors.get(object) || []
    } else {
      return Array.from(this.errors.values()).reduce(
        (allErrors, objectErrors) => {
          allErrors.push(...objectErrors)
          return allErrors
        },
        []
      )
    }
  }

  withErrors(object) {
    if (_.isArray(object)) {
      const newObject = []
      let changed = false

      for (let i = 0; i < object.length; i++) {
        const item = object[i]
        const newItem = this.withErrors(item)

        newObject.push(newItem)

        if (item !== newItem) {
          changed = true
        }
      }

      if (changed) {
        return newObject
      } else {
        return object
      }
    } else if (_.isObject(object)) {
      const objectErrors = this.getErrors(object)

      if (object.errors === 0 && objectErrors.length === 0) {
        return object
      } else {
        const objectErrorsMap = objectErrors.reduce((map, objectError) => {
          map[objectError.name] = objectError
          return map
        }, {})

        let changed = object.errors !== objectErrors.length

        const newObject = {
          ...object,
          errors: objectErrors.length
        }

        _.keys(object).forEach(key => {
          let oldError = null
          let newError = null

          if (object[key] && object[key].error) {
            oldError = object[key].error
          }
          if (objectErrorsMap[key] && objectErrorsMap[key].error) {
            newError = objectErrorsMap[key].error
          }

          if (oldError !== newError) {
            newObject[key] = {
              ...object[key],
              error: newError
            }
            changed = true
          }
        })

        if (changed) {
          return newObject
        } else {
          return object
        }
      }
    }
  }
}

export default FormValidator
