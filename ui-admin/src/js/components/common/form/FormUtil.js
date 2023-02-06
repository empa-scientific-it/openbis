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

  hasFieldChanged(currentObject, originalObject, path) {
    const currentValue = _.get(currentObject, path)
    const originalValue = originalObject ? _.get(originalObject, path) : null
    return originalValue.value !== currentValue.value
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
