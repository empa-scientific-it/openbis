import BrowserController from '@src/js/components/common/browser2/BrowserController.js'

export default class UserBrowserController extends BrowserController {
  async doLoadNodes({ node, filter, offset, limit }) {
    if (node === null) {
      const folders = [
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

      if (filter) {
        return folders.filter(
          folder =>
            folder.text.toLowerCase().indexOf(filter.toLowerCase()) !== -1
        )
      } else {
        return folders
      }
    } else if (node.id === 'folder1') {
      return [
        {
          id: 'child1.1',
          text: 'Child 1.1',
          object: { type: 'child', id: '1.1' }
        },
        {
          id: 'child1.2',
          text: 'Child 1.2',
          object: { type: 'child', id: '1.2' }
        }
      ]
    } else if (node.id === 'folder2') {
      var children = []
      for (let i = 0; i < 100; i++) {
        children.push({
          id: 'child2.' + i,
          text: 'Child 2.' + i,
          object: { type: 'child', id: '2.' + i }
        })
      }
      return children
    } else {
      return null
    }
  }
}
