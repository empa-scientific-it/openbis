import TextField from '@src/js/components/common/form/TextField.jsx'
import TextFieldWrapper from '@srcTest/js/components/common/form/wrapper/TextFieldWrapper.js'
import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import CheckboxFieldWrapper from '@srcTest/js/components/common/form/wrapper/CheckboxFieldWrapper.js'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import SelectFieldWrapper from '@srcTest/js/components/common/form/wrapper/SelectFieldWrapper.js'
import PageParametersPanelWrapper from '@srcTest/js/components/common/page/wrapper/PageParametersPanelWrapper.js'

export default class UserFormParametersUserWrapper extends PageParametersPanelWrapper {
  getUserId() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'userId' })
    )
  }

  getFirstName() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'firstName' })
    )
  }

  getLastName() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'lastName' })
    )
  }

  getEmail() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'email' })
    )
  }

  getHomeSpace() {
    return new SelectFieldWrapper(
      this.findComponent(SelectField).filter({ name: 'space' })
    )
  }

  getActive() {
    return new CheckboxFieldWrapper(
      this.findComponent(CheckboxField).filter({ name: 'active' })
    )
  }

  toJSON() {
    return {
      ...super.toJSON(),
      userId: this.getUserId().toJSON(),
      firstName: this.getFirstName().toJSON(),
      lastName: this.getLastName().toJSON(),
      email: this.getEmail().toJSON(),
      homeSpace: this.getHomeSpace().toJSON(),
      active: this.getActive().toJSON()
    }
  }
}
