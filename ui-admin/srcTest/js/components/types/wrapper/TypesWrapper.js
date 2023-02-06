import Content from '@src/js/components/common/content/Content.jsx'
import { TypeBrowser } from '@src/js/components/types/browser/TypeBrowser.jsx'
import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import BrowserWrapper from '@srcTest/js/components/common/browser/wrapper/BrowserWrapper.js'
import ContentWrapper from '@srcTest/js/components/common/content/wrapper/ContentWrapper.js'

export default class TypesWrapper extends BaseWrapper {
  getBrowser() {
    return new BrowserWrapper(this.findComponent(TypeBrowser))
  }

  getContent() {
    return new ContentWrapper(this.findComponent(Content))
  }

  toJSON() {
    if (this.wrapper.exists()) {
      return {
        browser: this.getBrowser().toJSON(),
        content: this.getContent().toJSON()
      }
    } else {
      return null
    }
  }
}
