import _ from 'lodash'
import openbis from '@src/js/services/openbis.js'
import PageControllerChange from '@src/js/components/common/page/PageControllerChange.js'
import TypeFormSelectionType from '@src/js/components/types/form/TypeFormSelectionType.js'
import TypeFormPropertyScope from '@src/js/components/types/form/TypeFormPropertyScope.js'
import TypeFormUtil from '@src/js/components/types/form/TypeFormUtil.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import users from '@src/js/common/consts/users.js'

export default class TypeFormControllerChange extends PageControllerChange {
  async execute(type, params) {
    if (type === TypeFormSelectionType.TYPE) {
      await this._handleChangeType(params)
    } else if (type === TypeFormSelectionType.SECTION) {
      await this._handleChangeSection(params)
    } else if (type === TypeFormSelectionType.PROPERTY) {
      await this._handleChangeProperty(params)
    } else if (type === TypeFormSelectionType.PREVIEW) {
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
      const {
        newCollection,
        oldObject,
        newObject
      } = FormUtil.changeCollectionItemField(
        state.properties,
        params.id,
        params.field,
        params.value
      )

      this._handleChangePropertyScope(oldObject, newObject)
      this._handleChangePropertyDataType(oldObject, newObject)
      this._handleChangePropertyMandatory(oldObject, newObject)

      return {
        properties: newCollection
      }
    })
    await this.controller.changed(true)
  }

  _handleChangePropertyScope(oldProperty, newProperty) {
    const { type, assignments } = this.context.getState()

    const oldScope = oldProperty.scope.value
    const newScope = newProperty.scope.value

    const oldCode = oldProperty.code.value
    const newCode = newProperty.code.value

    if (oldScope !== newScope || oldCode !== newCode) {
      let isGlobal = null

      if (oldScope !== newScope) {
        this._copyPropertyFieldValues(
          {
            scope: newProperty.scope
          },
          newProperty
        )
      }

      if (oldCode !== newCode && newScope === TypeFormPropertyScope.GLOBAL) {
        const { globalPropertyTypes } = this.controller.getDictionaries()

        let oldExisting = globalPropertyTypes.find(
          propertyType => propertyType.code === oldCode
        )
        let newExisting = globalPropertyTypes.find(
          propertyType => propertyType.code === newCode
        )

        if (oldExisting && !newExisting) {
          this._copyPropertyFieldValues(
            {
              scope: newProperty.scope,
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
            }
          }

          this._copyPropertyFieldValues(
            {
              scope: newProperty.scope,
              code: newProperty.code,
              ...newExisting
            },
            newProperty
          )

          isGlobal = true
        }
      }

      const propertyCode =
        newScope === TypeFormPropertyScope.LOCAL
          ? TypeFormUtil.addTypePrefix(type.code.value, newProperty.code.value)
          : newProperty.code.value

      const propertyAssignments =
        (assignments && assignments[propertyCode]) || 0

      _.assign(newProperty, {
        internal: {
          ...newProperty.internal,
          visible:
            newScope === TypeFormPropertyScope.GLOBAL && this.isSystemUser(),
          enabled:
            newScope === TypeFormPropertyScope.GLOBAL &&
            !isGlobal &&
            this.isSystemUser()
        },
        label: {
          ...newProperty.label,
          enabled: !newProperty.internal.value
        },
        description: {
          ...newProperty.description,
          enabled: !newProperty.internal.value
        },
        dataType: {
          ...newProperty.dataType,
          enabled: !newProperty.internal.value
        },
        schema: {
          ...newProperty.schema,
          enabled: !newProperty.internal.value
        },
        transformation: {
          ...newProperty.transformation,
          enabled: !newProperty.internal.value
        },
        vocabulary: {
          ...newProperty.vocabulary,
          enabled: !newProperty.internal.value && !isGlobal
        },
        materialType: {
          ...newProperty.materialType,
          enabled: !newProperty.internal.value && !isGlobal
        },
        sampleType: {
          ...newProperty.sampleType,
          enabled: !newProperty.internal.value && !isGlobal
        },
        assignments: propertyAssignments
      })

      newProperty.originalGlobal = isGlobal ? _.cloneDeep(newProperty) : null
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

        facade.loadTypeUsages(object).then(typeUsages => {
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
      scope: {
        ...dest.scope,
        value: _.get(src, 'scope.value', null)
      },
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

  isSystemUser() {
    return (
      this.context.getProps().session &&
      this.context.getProps().session.userName === users.SYSTEM
    )
  }
}
