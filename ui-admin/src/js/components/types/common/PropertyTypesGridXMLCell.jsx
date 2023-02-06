import React from 'react'
import Collapse from '@material-ui/core/Collapse'
import Link from '@material-ui/core/Link'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

class PropertyTypesGridXMLCell extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {
      visible: false
    }
    this.handleVisibilityChange = this.handleVisibilityChange.bind(this)
  }

  handleVisibilityChange() {
    this.setState(state => ({
      visible: !state.visible
    }))
  }

  render() {
    logger.log(logger.DEBUG, 'PropertyTypesGridXMLCell.render')

    const { value } = this.props
    const { visible } = this.state

    if (value) {
      return (
        <div>
          <div>
            <Link
              onClick={() => {
                this.handleVisibilityChange()
              }}
            >
              {visible
                ? messages.get(messages.HIDE)
                : messages.get(messages.SHOW)}
            </Link>
          </div>
          <Collapse in={visible} mountOnEnter={true} unmountOnExit={true}>
            <pre>{value}</pre>
          </Collapse>
        </div>
      )
    } else {
      return null
    }
  }
}

export default PropertyTypesGridXMLCell
