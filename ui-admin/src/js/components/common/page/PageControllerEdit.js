import PageMode from '@src/js/components/common/page/PageMode.js'

export default class PageControllerEdit {
  constructor(controller) {
    this.controller = controller
    this.context = controller.getContext()
  }

  execute() {
    return this.controller.load().then(() => {
      return this.context.setState(state => ({
        ...state,
        mode: PageMode.EDIT
      }))
    })
  }
}
