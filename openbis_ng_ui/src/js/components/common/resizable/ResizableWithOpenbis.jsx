import _ from 'lodash'
import React from 'react'
import { Resizable } from 're-resizable'
import AppController from '@src/js/components/AppController.js'
import logger from '@src/js/common/logger.js'

class ResizableClass extends React.PureComponent {
  constructor(props) {
    super(props)

    this.state = {
      width: this.props.width || '25%'
    }

    this.handleResize = this.handleResize.bind(this)
  }

  async componentDidMount() {
    const { id } = this.props

    if (id) {
      const setting = await AppController.getInstance().getSetting(id)

      if (setting && _.isNumber(setting.width)) {
        this.setState({
          width: setting.width
        })
      }
    }
  }

  handleResize(event, direction, ref) {
    const width = ref.offsetWidth

    this.setState({
      width
    })

    const { id } = this.props

    if (id) {
      AppController.getInstance().setSetting(id, { width })
    }
  }

  render() {
    logger.log(logger.DEBUG, 'Resizable.render')

    const { direction = {}, children } = this.props
    const { width } = this.state

    const enable = {
      left: false,
      top: false,
      right: false,
      bottom: false,
      topRight: false,
      bottomRight: false,
      bottomLeft: false,
      topLeft: false,
      ...direction
    }

    return (
      <Resizable
        size={{
          width: width,
          height: '100%'
        }}
        enable={enable}
        onResizeStop={this.handleResize}
      >
        {children}
      </Resizable>
    )
  }
}

export default ResizableClass
