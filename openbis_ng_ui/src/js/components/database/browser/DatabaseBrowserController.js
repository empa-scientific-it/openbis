import pages from '@src/js/common/consts/pages.js'
import BrowserController from '@src/js/components/common/browser2/BrowserController.js'

export default class UserBrowserController extends BrowserController {
  doGetPage() {
    return pages.DATABASE
  }

  async doLoadNodes() {
    let nodes = [
      {
        id: 'folder1',
        text: 'Folder 1',
        object: { type: 'folder', id: '1' },
        children: []
      },
      {
        id: 'folder2',
        text: 'Folder 2',
        object: { type: 'folder', id: '2' },
        children: []
      }
    ]

    return nodes
  }
}
