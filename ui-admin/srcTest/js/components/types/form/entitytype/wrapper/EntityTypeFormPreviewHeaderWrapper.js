import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import Header from '@src/js/components/common/form/Header.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import TextFieldWrapper from '@srcTest/js/components/common/form/wrapper/TextFieldWrapper.js'

export default class EntityTypeFormPreviewHeaderWrapper extends BaseWrapper {
  getTitle() {
    return this.findComponent(Header)
  }

  getCode() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'code' })
    )
  }

  getParents() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'parents' })
    )
  }

  getContainer() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'container' })
    )
  }

  toJSON() {
    const title = this.getTitle().text().trim()
    return {
      title: title.length > 0 ? title : null,
      code: this.getCode().toJSON(),
      parents: this.getParents().toJSON(),
      container: this.getContainer().toJSON()
    }
  }
}
