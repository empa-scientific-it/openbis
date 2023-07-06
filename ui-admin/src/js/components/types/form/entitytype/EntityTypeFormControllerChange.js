import _ from 'lodash'
import openbis from '@src/js/services/openbis.js'
import AppController from '@src/js/components/AppController.js'
import PageControllerChange from '@src/js/components/common/page/PageControllerChange.js'
import EntityTypeFormSelectionType from '@src/js/components/types/form/entitytype/EntityTypeFormSelectionType.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class EntityTypeFormControllerChange extends PageControllerChange {
  async execute(type, params) {
    if (type === EntityTypeFormSelectionType.TYPE) {
      await this._handleChangeType(params)
    } else if (type === EntityTypeFormSelectionType.SECTION) {
      await this._handleChangeSection(params)
    } else if (type === EntityTypeFormSelectionType.PROPERTY) {
      await this._handleChangeProperty(params)
    } else if (type === EntityTypeFormSelectionType.PREVIEW) {
      await this._handleChangePreview(params)
    }
  }

  async _handleChangeType(params) {
    await this.context.setState(state => {
      const { newObject } = FormUtil.changeObjectField(
        state.type,
        params.field,
        params.value
      )
      return {
        type: newObject
      }
    })
    await this.controller.changed(true)
  }

  async _handleChangeSection(params) {
    await this.context.setState(state => {
      const { newCollection } = FormUtil.changeCollectionItemField(
        state.sections,
        params.id,
        params.field,
        params.value
      )
      return {
        sections: newCollection
      }
    })
    await this.controller.changed(true)
  }

  async _handleChangeProperty(params) {
    await this.context.setState(state => {
      const { newCollection, oldObject, newObject } =
        FormUtil.changeCollectionItemField(
          state.properties,
          params.id,
          params.field,
          params.value
        )

      this._handleChangePropertyCode(oldObject, newObject)
      this._handleChangePropertyDataType(oldObject, newObject)
      this._handleChangePropertyMandatory(oldObject, newObject)

      return {
        properties: newCollection
      }
    })
    await this.controller.changed(true)
  }

  _handleChangePropertyCode(oldProperty, newProperty) {
    const { assignments } = this.context.getState()

    const oldCode = oldProperty.code.value
    const newCode = newProperty.code.value

    let oldExisting = null
    let newExisting = null

    if (oldCode !== newCode) {
      const { propertyTypes } = this.controller.getDictionaries()

      oldExisting = propertyTypes.find(
        propertyType => propertyType.code === oldCode
      )
      newExisting = propertyTypes.find(
        propertyType => propertyType.code === newCode
      )

      if (oldExisting && !newExisting) {
        this._copyPropertyFieldValues(
          {
            code: newProperty.code
          },
          newProperty
        )
      } else if (newExisting) {
        newExisting = {
          internal: {
            value: _.get(newExisting, 'managedInternally', false)
          },
          label: {
            value: _.get(newExisting, 'label', null)
          },
          description: {
            value: _.get(newExisting, 'description', null)
          },
          dataType: {
            value: _.get(newExisting, 'dataType', null)
          },
          schema: {
            value: _.get(newExisting, 'schema', null)
          },
          transformation: {
            value: _.get(newExisting, 'transformation', null)
          },
          vocabulary: {
            value: _.get(newExisting, 'vocabulary.code', null)
          },
          materialType: {
            value: _.get(newExisting, 'materialType.code', null)
          },
          sampleType: {
            value: _.get(newExisting, 'sampleType.code', null)
          },
          isMultiValue: {
            value: _.get(newExisting, 'multiValue', false),
            enabled: false
          }
        }

        this._copyPropertyFieldValues(
          {
            code: newProperty.code,
            ...newExisting
          },
          newProperty
        )
      }

      const propertyAssignments =
        (assignments && assignments[newProperty.code.value]) || 0

      _.assign(newProperty, {
        internal: {
          ...newProperty.internal,
          visible: AppController.getInstance().isSystemUser(),
          enabled: AppController.getInstance().isSystemUser() && !newExisting
        },
        label: {
          ...newProperty.label,
          enabled:
            !newProperty.internal.value ||
            AppController.getInstance().isSystemUser()
        },
        description: {
          ...newProperty.description,
          enabled:
            !newProperty.internal.value ||
            AppController.getInstance().isSystemUser()
        },
        dataType: {
          ...newProperty.dataType,
          enabled:
            !newProperty.internal.value ||
            AppController.getInstance().isSystemUser()
        },
        schema: {
          ...newProperty.schema,
          enabled:
            !newProperty.internal.value ||
            AppController.getInstance().isSystemUser()
        },
        transformation: {
          ...newProperty.transformation,
          enabled:
            !newProperty.internal.value ||
            AppController.getInstance().isSystemUser()
        },
        vocabulary: {
          ...newProperty.vocabulary,
          enabled: !newExisting
        },
        materialType: {
          ...newProperty.materialType,
          enabled: !newExisting
        },
        sampleType: {
          ...newProperty.sampleType,
          enabled: !newExisting
        },
        assignments: propertyAssignments
      })

      newProperty.originalGlobal = newExisting ? _.cloneDeep(newProperty) : null
    }
  }

  _handleChangePropertyDataType(oldProperty, newProperty) {
    const oldDataType = oldProperty.dataType.value
    const newDataType = newProperty.dataType.value

    if (oldDataType !== newDataType) {
      _.assign(newProperty, {
        vocabulary: {
          ...newProperty.vocabulary,
          visible: newDataType === openbis.DataType.CONTROLLEDVOCABULARY
        },
        materialType: {
          ...newProperty.materialType,
          visible: newDataType === openbis.DataType.MATERIAL
        },
        sampleType: {
          ...newProperty.sampleType,
          visible: newDataType === openbis.DataType.SAMPLE
        },
        schema: {
          ...newProperty.schema,
          visible: newDataType === openbis.DataType.XML
        },
        transformation: {
          ...newProperty.transformation,
          visible: newDataType === openbis.DataType.XML
        }
      })
    }
  }

  _handleChangePropertyMandatory(oldProperty, newProperty) {
    const oldMandatory = oldProperty.mandatory.value
    const newMandatory = newProperty.mandatory.value

    if (oldMandatory !== newMandatory) {
      const { type } = this.context.getState()

      const typeIsNew = !type.original
      const propertyIsNew = !newProperty.original
      const propertyIsMandatory = newProperty.mandatory.value
      const propertyWasMandatory = newProperty.original
        ? newProperty.original.mandatory.value
        : false

      if (
        !typeIsNew &&
        propertyIsMandatory &&
        (propertyIsNew || !propertyWasMandatory)
      ) {
        const { object, facade } = this.controller

        facade
          .loadTypeUsages(object)
          .then(typeUsages => {
            this.context.setState(state => {
              const index = state.properties.findIndex(
                property => property.id === newProperty.id
              )
              if (index === -1) {
                return {}
              }
              const newProperties = Array.from(state.properties)
              newProperties[index] = {
                ...newProperties[index],
                initialValueForExistingEntities: {
                  ...newProperties[index].initialValueForExistingEntities,
                  visible: typeUsages > 0
                }
              }
              return {
                properties: newProperties
              }
            })
          })
          .catch(error => {
            AppController.getInstance().errorChange(error)
          })
      } else {
        _.assign(newProperty, {
          initialValueForExistingEntities: {
            ...newProperty.initialValueForExistingEntities,
            visible: false
          }
        })
      }
    }
  }

  async _handleChangePreview(params) {
    await this.context.setState(state => {
      const { newObject } = FormUtil.changeObjectField(
        state.preview,
        params.field,
        params.value
      )
      return {
        preview: newObject
      }
    })
    await this.controller.changed(true)
  }

  _copyPropertyFieldValues(src, dest) {
    _.assign(dest, {
      code: {
        ...dest.code,
        value: _.get(src, 'code.value', null)
      },
      internal: {
        ...dest.internal,
        value: _.get(src, 'internal.value', false)
      },
      label: {
        ...dest.label,
        value: _.get(src, 'label.value', null)
      },
      description: {
        ...dest.description,
        value: _.get(src, 'description.value', null)
      },
      dataType: {
        ...dest.dataType,
        value: _.get(src, 'dataType.value', null)
      },
      schema: {
        ...dest.schema,
        value: _.get(src, 'schema.value', null)
      },
      transformation: {
        ...dest.transformation,
        value: _.get(src, 'transformation.value', null)
      },
      vocabulary: {
        ...dest.vocabulary,
        value: _.get(src, 'vocabulary.value', null)
      },
      materialType: {
        ...dest.materialType,
        value: _.get(src, 'materialType.value', null)
      },
      sampleType: {
        ...dest.sampleType,
        value: _.get(src, 'sampleType.value', null)
      },
      plugin: {
        ...dest.plugin,
        value: _.get(src, 'plugin.value', null)
      },
      mandatory: {
        ...dest.mandatory,
        value: _.get(src, 'mandatory.value', false)
      },
      isMultiValue: {
        ...dest.isMultiValue,
        value: _.get(src, 'isMultiValue.value', false),
        enabled: _.get(src, 'isMultiValue.enabled', true)
      },
      showInEditView: {
        ...dest.showInEditView,
        value: _.get(src, 'showInEditView.value', true)
      },
      showRawValueInForms: {
        ...dest.showRawValueInForms,
        value: _.get(src, 'showRawValueInForms.value', false)
      },
      initialValueForExistingEntities: {
        ...dest.initialValueForExistingEntities,
        value: _.get(src, 'initialValueForExistingEntities.value', null)
      }
    })
  }
}
