import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Popover from '@material-ui/core/Popover'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import Container from '@src/js/components/common/form/Container.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    padding: theme.spacing(1),
    paddingLeft: 0
  },
  field: {
    paddingBottom: theme.spacing(1)
  }
})

const ALL = 'ALL'
const VISIBLE = 'VISIBLE'
const PLAIN_TEXT = 'PLAIN_TEXT'
const RICH_TEXT = 'RICH_TEXT'

class GridExports extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {
      el: null
    }
    this.handleOpen = this.handleOpen.bind(this)
    this.handleClose = this.handleClose.bind(this)
    this.handleExport = this.handleExport.bind(this)
  }

  handleOpen(event) {
    this.setState({
      el: event.currentTarget
    })
  }

  handleClose() {
    this.setState({
      el: null
    })
  }

  handleExport(action) {
    const { onExport } = this.props
    this.handleClose()
    if (onExport) {
      onExport(action)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'GridExports.render')

    const { disabled, classes } = this.props
    const { el } = this.state

    return (
      <div className={classes.container}>
        <Button
          label={messages.get(messages.EXPORTS)}
          color='default'
          disabled={disabled}
          onClick={this.handleOpen}
        />
        <Popover
          open={Boolean(el)}
          anchorEl={el}
          onClose={this.handleClose}
          anchorOrigin={{
            vertical: 'bottom',
            horizontal: 'left'
          }}
          transformOrigin={{
            vertical: 'top',
            horizontal: 'left'
          }}
        >
          <Container>
            <div className={classes.field}>
              <SelectField
                label={messages.get(messages.COLUMNS)}
                name='columns'
                options={[
                  {
                    label: messages.get(messages.ALL),
                    value: ALL
                  },
                  {
                    label: messages.get(messages.VISIBLE),
                    value: VISIBLE
                  }
                ]}
                value={VISIBLE}
                variant='standard'
                onChange={this.handleChange}
              />
            </div>
            <div className={classes.field}>
              <SelectField
                label={messages.get(messages.ROWS)}
                name='rows'
                options={[
                  {
                    label: messages.get(messages.ALL),
                    value: ALL
                  },
                  {
                    label: messages.get(messages.VISIBLE),
                    value: VISIBLE
                  }
                ]}
                value={VISIBLE}
                variant='standard'
                onChange={this.handleChange}
              />
            </div>
            <div className={classes.field}>
              <SelectField
                label={messages.get(messages.VALUES)}
                name='values'
                options={[
                  {
                    label: messages.get(messages.PLAIN_TEXT),
                    value: PLAIN_TEXT
                  },
                  {
                    label: messages.get(messages.RICH_TEXT),
                    value: RICH_TEXT
                  }
                ]}
                value={RICH_TEXT}
                variant='standard'
                onChange={this.handleChange}
              />
            </div>
            <div className={classes.field}>
              <Button label={messages.get(messages.EXPORT)} type='neutral' />
            </div>
          </Container>
        </Popover>
      </div>
    )
  }
}

export default _.flow(withStyles(styles))(GridExports)
