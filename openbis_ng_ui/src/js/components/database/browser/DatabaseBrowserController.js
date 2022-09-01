import BrowserController from '@src/js/components/common/browser2/BrowserController.js'

export default class UserBrowserController extends BrowserController {
  async doLoadNodes({ node, filter, offset, limit }) {
    if (node === null) {
      return [
        {
          id: 'folder1',
          text: 'Folder 1',
          object: { type: 'folder', id: '1' },
          canHaveChildren: true
        },
        {
          id: 'folder2',
          text: 'Folder 2',
          object: { type: 'folder', id: '2' },
          canHaveChildren: true
        }
      ]
    } else if (node.id === 'folder1') {
      return [
        {
          id: 'child1.1',
          text: 'Child 1.1',
          object: { type: 'child', id: '1.1' },
          canMatchFilter: true
        },
        {
          id: 'child1.2',
          text: 'Child 1.2',
          object: { type: 'child', id: '1.2' },
          canMatchFilter: true
        }
      ]
    } else if (node.id === 'folder2') {
      var children = []
      for (let i = 0; i < 1000; i++) {
        children.push({
          id: 'child2.' + i,
          text: 'Child 2.' + i,
          object: { type: 'child', id: '2.' + i },
          canMatchFilter: true
        })
      }
      return children
    } else {
      return null
    }
  }
}
