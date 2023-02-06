import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import PluginFormScript from '@src/js/components/tools/form/plugin/PluginFormScript.jsx'
import PluginFormScriptWrapper from '@srcTest/js/components/tools/form/plugin/wrapper/PluginFormScriptWrapper.js'
import PluginFormParameters from '@src/js/components/tools/form/plugin/PluginFormParameters.jsx'
import PluginFormParametersWrapper from '@srcTest/js/components/tools/form/plugin/wrapper/PluginFormParametersWrapper.js'
import PluginFormButtons from '@src/js/components/tools/form/plugin/PluginFormButtons.jsx'
import PluginFormButtonsWrapper from '@srcTest/js/components/tools/form/plugin/wrapper/PluginFormButtonsWrapper.js'
import PluginFormEvaluateParameters from '@src/js/components/tools/form/plugin/PluginFormEvaluateParameters.jsx'
import PluginFormEvaluateParametersWrapper from '@srcTest/js/components/tools/form/plugin/wrapper/PluginFormEvaluateParametersWrapper.js'
import PluginFormEvaluateResults from '@src/js/components/tools/form/plugin/PluginFormEvaluateResults.jsx'
import PluginFormEvaluateResultsWrapper from '@srcTest/js/components/tools/form/plugin/wrapper/PluginFormEvaluateResultsWrapper.js'

export default class PluginFormWrapper extends BaseWrapper {
  getScript() {
    return new PluginFormScriptWrapper(this.findComponent(PluginFormScript))
  }

  getParameters() {
    return new PluginFormParametersWrapper(
      this.findComponent(PluginFormParameters)
    )
  }

  getEvaluateParameters() {
    return new PluginFormEvaluateParametersWrapper(
      this.findComponent(PluginFormEvaluateParameters)
    )
  }

  getEvaluateResults() {
    return new PluginFormEvaluateResultsWrapper(
      this.findComponent(PluginFormEvaluateResults)
    )
  }

  getButtons() {
    return new PluginFormButtonsWrapper(this.findComponent(PluginFormButtons))
  }

  toJSON() {
    return {
      script: this.getScript().toJSON(),
      parameters: this.getParameters().toJSON(),
      evaluateParameters: this.getEvaluateParameters().toJSON(),
      evaluateResults: this.getEvaluateResults().toJSON(),
      buttons: this.getButtons().toJSON()
    }
  }
}
