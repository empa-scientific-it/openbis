import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import Header from '@src/js/components/common/form/Header.jsx'
import SourceCodeField from '@src/js/components/common/form/SourceCodeField.jsx'
import SourceCodeFieldWrapper from '@srcTest/js/components/common/form/wrapper/SourceCodeFieldWrapper.js'

export default class PluginFormScriptWrapper extends BaseWrapper {
  getTitle() {
    return this.findComponent(Header)
  }

  getScript() {
    return new SourceCodeFieldWrapper(this.findComponent(SourceCodeField))
  }

  toJSON() {
    if (this.wrapper.exists()) {
      return {
        title: this.getTitle().exists() ? this.getTitle().text() : null,
        script: this.getScript().toJSON()
      }
    } else {
      return null
    }
  }
}
