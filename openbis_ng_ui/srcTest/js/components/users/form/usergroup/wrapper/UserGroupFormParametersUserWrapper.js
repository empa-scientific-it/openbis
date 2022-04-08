import SelectField from '@src/js/components/common/form/SelectField.jsx'
import SelectFieldWrapper from '@srcTest/js/components/common/form/wrapper/SelectFieldWrapper.js'
import PageParametersPanelWrapper from '@srcTest/js/components/common/page/wrapper/PageParametersPanelWrapper.js'

export default class UserGroupFormParametersUserWrapper extends PageParametersPanelWrapper {
  getUserId() {
    return new SelectFieldWrapper(
      this.findComponent(SelectField).filter({ name: 'userId' })
    )
  }

  toJSON() {
    return {
      ...super.toJSON(),
      userId: this.getUserId().toJSON()
    }
  }
}
