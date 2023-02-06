import React from 'react'
import Link from '@material-ui/core/Link'
import Collapse from '@material-ui/core/Collapse'
import messages from '@src/js/common/messages.js'

export default class HistoryGridContentCell extends React.PureComponent {
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
    const { value } = this.props
    const { visible } = this.state

    if (value) {
      return (
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
