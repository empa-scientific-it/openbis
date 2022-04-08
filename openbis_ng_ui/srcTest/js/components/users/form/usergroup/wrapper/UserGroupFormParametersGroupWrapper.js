import TextField from '@src/js/components/common/form/TextField.jsx'
import TextFieldWrapper from '@srcTest/js/components/common/form/wrapper/TextFieldWrapper.js'
import PageParametersPanelWrapper from '@srcTest/js/components/common/page/wrapper/PageParametersPanelWrapper.js'

export default class UserGroupFormParametersGroupWrapper extends PageParametersPanelWrapper {
  getCode() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'code' })
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
      code: this.getCode().toJSON(),
      description: this.getDescription().toJSON()
    }
  }
}
