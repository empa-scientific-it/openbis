import AutocompleterField from '@src/js/components/common/form/AutocompleterField.jsx'
import AutocompleterFieldWrapper from '@srcTest/js/components/common/form/wrapper/AutocompleterFieldWrapper.js'
import TextField from '@src/js/components/common/form/TextField.jsx'
import TextFieldWrapper from '@srcTest/js/components/common/form/wrapper/TextFieldWrapper.js'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import SelectFieldWrapper from '@srcTest/js/components/common/form/wrapper/SelectFieldWrapper.js'
import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import CheckboxFieldWrapper from '@srcTest/js/components/common/form/wrapper/CheckboxFieldWrapper.js'
import PageParametersPanelWrapper from '@srcTest/js/components/common/page/wrapper/PageParametersPanelWrapper.js'

export default class QueryFormParametersWrapper extends PageParametersPanelWrapper {
  getName() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'name' })
    )
  }

  getDescription() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'description' })
    )
  }

  getDatabase() {
    return new SelectFieldWrapper(
      this.findComponent(SelectField).filter({ name: 'databaseId' })
    )
  }

  getQueryType() {
    return new SelectFieldWrapper(
      this.findComponent(SelectField).filter({ name: 'queryType' })
    )
  }

  getEntityTypeCodePattern() {
    return new AutocompleterFieldWrapper(
      this.findComponent(AutocompleterField).filter({
        name: 'entityTypeCodePattern'
      })
    )
  }

  getPublicFlag() {
    return new CheckboxFieldWrapper(
      this.findComponent(CheckboxField).filter({ name: 'publicFlag' })
    )
  }

  toJSON() {
    return {
      ...super.toJSON(),
      name: this.getName().toJSON(),
      description: this.getDescription().toJSON(),
      database: this.getDatabase().toJSON(),
      queryType: this.getQueryType().toJSON(),
      entityTypeCodePattern: this.getEntityTypeCodePattern().toJSON(),
      publicFlag: this.getPublicFlag().toJSON()
    }
  }
}
