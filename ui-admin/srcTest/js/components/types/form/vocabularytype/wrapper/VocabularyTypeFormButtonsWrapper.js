import Button from '@src/js/components/common/form/Button.jsx'
import ButtonWrapper from '@srcTest/js/components/common/form/wrapper/ButtonWrapper.js'
import PageButtonsWrapper from '@srcTest/js/components/common/page/wrapper/PageButtonsWrapper.js'

export default class VocabularyTypeFormButtonsWrapper extends PageButtonsWrapper {
  getAddTerm() {
    return new ButtonWrapper(
      this.findComponent(Button).filter({ name: 'addTerm' })
    )
  }

  getRemoveTerm() {
    return new ButtonWrapper(
      this.findComponent(Button).filter({ name: 'removeTerm' })
    )
  }

  toJSON() {
    return {
      ...super.toJSON(),
      addTerm: this.getAddTerm().toJSON(),
      removeTerm: this.getRemoveTerm().toJSON()
    }
  }
}
