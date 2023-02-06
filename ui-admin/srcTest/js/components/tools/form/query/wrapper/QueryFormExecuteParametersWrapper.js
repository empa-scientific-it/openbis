import TextField from '@src/js/components/common/form/TextField.jsx'
import TextFieldWrapper from '@srcTest/js/components/common/form/wrapper/TextFieldWrapper.js'
import PageParametersPanelWrapper from '@srcTest/js/components/common/page/wrapper/PageParametersPanelWrapper.js'

export default class QueryFormExecuteParametersWrapper extends PageParametersPanelWrapper {
  getParameters() {
    const parameters = []
    this.findComponent(TextField).forEach(parameterWrapper => {
      parameters.push(new TextFieldWrapper(parameterWrapper))
    })
    return parameters
  }

  toJSON() {
    return {
      ...super.toJSON(),
      parameters: this.getParameters().map(parameter => parameter.toJSON())
    }
  }
}
