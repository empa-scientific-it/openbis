import SelectField from '@src/js/components/common/form/SelectField.jsx'
import SelectFieldWrapper from '@srcTest/js/components/common/form/wrapper/SelectFieldWrapper.js'
import AutocompleterField from '@src/js/components/common/form/AutocompleterField.jsx'
import AutocompleterFieldWrapper from '@srcTest/js/components/common/form/wrapper/AutocompleterFieldWrapper.js'
import PageParametersPanelWrapper from '@srcTest/js/components/common/page/wrapper/PageParametersPanelWrapper.js'

export default class PluginFormEvaluateParametersWrapper extends PageParametersPanelWrapper {
  getEntityKind() {
    return new SelectFieldWrapper(
      this.findComponent(SelectField).filter({ name: 'entityKind' })
    )
  }

  getEntity() {
    return new AutocompleterFieldWrapper(
      this.findComponent(AutocompleterField).filter({ name: 'entity' })
    )
  }

  toJSON() {
    return {
      ...super.toJSON(),
      entityKind: this.getEntityKind().toJSON(),
      entity: this.getEntity().toJSON()
    }
  }
}
