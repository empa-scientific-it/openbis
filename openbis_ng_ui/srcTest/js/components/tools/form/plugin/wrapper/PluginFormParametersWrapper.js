import TextField from '@src/js/components/common/form/TextField.jsx'
import TextFieldWrapper from '@srcTest/js/components/common/form/wrapper/TextFieldWrapper.js'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import SelectFieldWrapper from '@srcTest/js/components/common/form/wrapper/SelectFieldWrapper.js'
import PageParametersPanelWrapper from '@srcTest/js/components/common/page/wrapper/PageParametersPanelWrapper.js'

export default class PluginFormParametersWrapper extends PageParametersPanelWrapper {
  getName() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'name' })
    )
  }

  getEntityKind() {
    return new SelectFieldWrapper(
      this.findComponent(SelectField).filter({ name: 'entityKind' })
    )
  }

  getDescription() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'description' })
    )
  }

  toJSON() {
    return {
      ...super.toJSON(),
      name: this.getName().toJSON(),
      entityKind: this.getEntityKind().toJSON(),
      description: this.getDescription().toJSON()
    }
  }
}
