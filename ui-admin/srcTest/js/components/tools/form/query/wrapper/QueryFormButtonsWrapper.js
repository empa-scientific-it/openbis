import Button from '@src/js/components/common/form/Button.jsx'
import ButtonWrapper from '@srcTest/js/components/common/form/wrapper/ButtonWrapper.js'
import PageButtonsWrapper from '@srcTest/js/components/common/page/wrapper/PageButtonsWrapper.js'

export default class QueryFormButtonsWrapper extends PageButtonsWrapper {
  getExecute() {
    return new ButtonWrapper(
      this.findComponent(Button).filter({ name: 'execute' })
    )
  }

  toJSON() {
    return {
      ...super.toJSON(),
      execute: this.getExecute().toJSON()
    }
  }
}
