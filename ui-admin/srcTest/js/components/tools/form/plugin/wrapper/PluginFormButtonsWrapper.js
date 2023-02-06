import Button from '@src/js/components/common/form/Button.jsx'
import ButtonWrapper from '@srcTest/js/components/common/form/wrapper/ButtonWrapper.js'
import PageButtonsWrapper from '@srcTest/js/components/common/page/wrapper/PageButtonsWrapper.js'

export default class PluginFormButtonsWrapper extends PageButtonsWrapper {
  getEvaluate() {
    return new ButtonWrapper(
      this.findComponent(Button).filter({ name: 'evaluate' })
    )
  }

  toJSON() {
    return {
      ...super.toJSON(),
      evaluate: this.getEvaluate().toJSON()
    }
  }
}
