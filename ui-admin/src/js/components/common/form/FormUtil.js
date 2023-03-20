import _ from 'lodash'

class FormUtil {
  createField(params = {}) {
    return {
      value: null,
      visible: true,
      enabled: true,
      ...params
    }
  }

  changeObjectField(object, field, value) {
    const newObject = {
      ...object,
      [field]: {
        ...object[field],
        value
      }
    }
    return {
      oldObject: object,
      newObject
    }
  }

  changeCollectionItemField(collection, itemId, field, value) {
    const index = collection.findIndex(item => item.id === itemId)

    const { oldObject, newObject } = this.changeObjectField(
      collection[index],
      field,
      value
    )

    const newCollection = Array.from(collection)
    newCollection[index] = newObject

    return {
      oldCollection: collection,
      newCollection,
      oldObject,
      newObject,
      index
    }
  }

  getFieldValue(object, path) {
    const field = !_.isNil(object) ? _.get(object, path, null) : null

    if (!_.isNil(field)) {
      const value = field.value
      if (_.isNil(value)) {
        return null
      } else if (_.isString(value) && _.isEmpty(value.trim())) {
        return null
      } else {
        return value
      }
    } else {
      return null
    }
  }

  hasFieldChanged(currentObject, originalObject, path) {
    const currentValue = this.getFieldValue(currentObject, path)
    const originalValue = this.getFieldValue(originalObject, path)
    return originalValue !== currentValue
  }

  haveFieldsChanged(currentObject, originalObject, paths) {
    return _.some(paths, path =>
      this.hasFieldChanged(currentObject, originalObject, path)
    )
  }

  trimFields(object) {
    const trimString = str => {
      const trimmed = str.trim()
      return trimmed.length > 0 ? trimmed : null
    }

    const trimField = field => {
      if (field) {
        if (_.isString(field)) {
          return trimString(field)
        } else if (_.isObject(field) && _.isString(field.value)) {
          return {
            ...field,
            value: trimString(field.value)
          }
        }
      }
      return field
    }

    return _.mapValues(
      {
        ...object
      },
      trimField
    )
  }
}

export default new FormUtil()
