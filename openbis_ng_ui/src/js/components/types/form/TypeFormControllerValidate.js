import PageControllerValidate from '@src/js/components/common/page/PageConrollerValidate.js'
import TypeFormControllerStrategies from './TypeFormControllerStrategies.js'

export default class TypeFormControllerValidate extends PageControllerValidate {
  validate(validator) {
    const { type, properties } = this.context.getState()

    const newType = {
      ...type
    }
    const newProperties = properties.map(property => ({
      ...property
    }))

    this._validateType(validator, newType)
    this._validateProperties(validator, newProperties)

    return {
      type: newType,
      properties: newProperties
    }
  }

  async select(newState, firstError) {
    if (firstError.object === newState.type) {
      await this.setSelection({
        type: 'type',
        params: {
          part: firstError.name
        }
      })
    } else if (newState.properties.includes(firstError.object)) {
      await this.setSelection({
        type: 'property',
        params: {
          id: firstError.object.id,
          part: firstError.name
        }
      })
    }
  }

  _validateType(validator, type) {
    const strategy = this._getStrategy()
    validator.validateNotEmpty(type, 'code', 'Code')
    validator.validateCode(type, 'code', 'Code')
    strategy.validateTypeAttributes(validator, type)
  }

  _validateProperties(validator, properties) {
    properties.forEach(property => {
      this._validateProperty(validator, property)
    })
  }

  _validateProperty(validator, property) {
    validator.validateNotEmpty(property, 'code', 'Code')
    validator.validateCode(property, 'code', 'Code')
    validator.validateNotEmpty(property, 'label', 'Label')
    validator.validateNotEmpty(property, 'description', 'Description')
    validator.validateNotEmpty(property, 'dataType', 'Data Type')

    if (property.vocabulary.visible) {
      validator.validateNotEmpty(property, 'vocabulary', 'Vocabulary')
    }
    if (property.materialType.visible) {
      validator.validateNotEmpty(property, 'materialType', 'Material Type')
    }
    if (property.initialValueForExistingEntities.visible) {
      validator.validateNotEmpty(
        property,
        'initialValueForExistingEntities',
        'Initial Value'
      )
    }
  }

  _getStrategy() {
    const strategies = new TypeFormControllerStrategies()
    strategies.extendObjectTypeStrategy(new ObjectTypeStrategy())
    strategies.extendCollectionTypeStrategy(new CollectionTypeStrategy())
    strategies.extendDataSetTypeStrategy(new DataSetTypeStrategy())
    strategies.extendMaterialTypeStrategy(new MaterialTypeStrategy())
    return strategies.getStrategy(this.object.type)
  }
}

class ObjectTypeStrategy {
  validateTypeAttributes(validator, type) {
    validator.validateNotEmpty(
      type,
      'generatedCodePrefix',
      'Generated code prefix'
    )
  }
}

class CollectionTypeStrategy {
  validateTypeAttributes() {}
}

class DataSetTypeStrategy {
  validateTypeAttributes() {}
}

class MaterialTypeStrategy {
  validateTypeAttributes() {}
}
