import TextField from '@src/js/components/common/form/TextField.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import TextFieldWrapper from '@srcTest/js/components/common/form/wrapper/TextFieldWrapper.js'
import ButtonWrapper from '@srcTest/js/components/common/form/wrapper/ButtonWrapper.js'
import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'

export default class LoginWrapper extends BaseWrapper {
  getUser() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'user' })
    )
  }

  getPassword() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'password' })
    )
  }

  getButton() {
    return new ButtonWrapper(this.findComponent(Button))
  }

  toJSON() {
    if (this.wrapper.exists()) {
      return {
        user: this.getUser().toJSON(),
        password: this.getPassword().toJSON(),
        button: this.getButton().toJSON()
      }
    } else {
      return null
    }
  }
}
