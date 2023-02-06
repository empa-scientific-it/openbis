import SelectField from '@src/js/components/common/form/SelectField.jsx'
import SelectFieldWrapper from '@srcTest/js/components/common/form/wrapper/SelectFieldWrapper.js'
import PageParametersPanelWrapper from '@srcTest/js/components/common/page/wrapper/PageParametersPanelWrapper.js'

export default class UserFormParametersGroupWrapper extends PageParametersPanelWrapper {
  getCode() {
    return new SelectFieldWrapper(
      this.findComponent(SelectField).filter({ name: 'code' })
    )
  }

  toJSON() {
    return {
      ...super.toJSON(),
      code: this.getCode().toJSON()
    }
  }
}
