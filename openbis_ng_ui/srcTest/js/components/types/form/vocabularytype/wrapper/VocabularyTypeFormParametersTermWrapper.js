import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import CheckboxFieldWrapper from '@srcTest/js/components/common/form/wrapper/CheckboxFieldWrapper.js'
import TextFieldWrapper from '@srcTest/js/components/common/form/wrapper/TextFieldWrapper.js'
import PageParametersPanelWrapper from '@srcTest/js/components/common/page/wrapper/PageParametersPanelWrapper'

export default class VocabularyTypeFormParametersTermWrapper extends PageParametersPanelWrapper {
  getCode() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'code' })
    )
  }

  getLabel() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'label' })
    )
  }

  getDescription() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'description' })
    )
  }

  getOfficial() {
    return new CheckboxFieldWrapper(
      this.findComponent(CheckboxField).filter({ name: 'official' })
    )
  }

  toJSON() {
    return {
      ...super.toJSON(),
      messages: this.getMessages().map(message => message.toJSON()),
      code: this.getCode().toJSON(),
      label: this.getLabel().toJSON(),
      description: this.getDescription().toJSON(),
      official: this.getOfficial().toJSON()
    }
  }
}
