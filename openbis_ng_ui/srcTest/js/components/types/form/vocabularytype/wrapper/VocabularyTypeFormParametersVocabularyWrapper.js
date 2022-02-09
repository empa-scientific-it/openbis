import TextField from '@src/js/components/common/form/TextField.jsx'
import TextFieldWrapper from '@srcTest/js/components/common/form/wrapper/TextFieldWrapper.js'
import PageParametersPanelWrapper from '@srcTest/js/components/common/page/wrapper/PageParametersPanelWrapper'

export default class VocabularyTypeFormParametersVocabularyWrapper extends PageParametersPanelWrapper {
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

  getUrlTemplate() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'urlTemplate' })
    )
  }

  toJSON() {
    return {
      ...super.toJSON(),
      code: this.getCode().toJSON(),
      description: this.getDescription().toJSON(),
      urlTemplate: this.getUrlTemplate().toJSON()
    }
  }
}
