import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Popover from '@material-ui/core/Popover'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import Container from '@src/js/components/common/form/Container.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import GridExportOptions from '@src/js/components/common/grid/GridExportOptions.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    padding: theme.spacing(1),
    paddingLeft: 0
  },
  popup: {
    maxWidth: '300px'
  },
  field: {
    paddingBottom: theme.spacing(1)
  }
})

class GridExports extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {
      el: null,
      columns: {
        value: GridExportOptions.VISIBLE
      },
      rows: {
        value: GridExportOptions.VISIBLE
      },
      values: {
        value: GridExportOptions.RICH_TEXT
      }
    }
    this.handleOpen = this.handleOpen.bind(this)
    this.handleClose = this.handleClose.bind(this)
    this.handleChange = this.handleChange.bind(this)
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

  handleChange(event) {
    this.setState({
      [event.target.name]: {
        value: event.target.value
      }
    })
  }

  handleExport() {
    const { onExport } = this.props
    this.handleClose()
    if (onExport) {
      const { columns, rows, values } = this.state
      onExport({
        columns: columns.value,
        rows: rows.value,
        values: values.value
      })
    }
  }

  render() {
    logger.log(logger.DEBUG, 'GridExports.render')

    const { disabled, classes } = this.props
    const { el, columns, rows, values } = this.state

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
          <Container className={classes.popup}>
            <div className={classes.field}>
              <SelectField
                label={messages.get(messages.COLUMNS)}
                name='columns'
                options={[
                  {
                    label: messages.get(messages.ALL),
                    value: GridExportOptions.ALL
                  },
                  {
                    label: messages.get(messages.VISIBLE),
                    value: GridExportOptions.VISIBLE
                  }
                ]}
                value={columns.value}
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
                    value: GridExportOptions.ALL
                  },
                  {
                    label: messages.get(messages.VISIBLE),
                    value: GridExportOptions.VISIBLE
                  }
                ]}
                value={rows.value}
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
                    value: GridExportOptions.PLAIN_TEXT
                  },
                  {
                    label: messages.get(messages.RICH_TEXT),
                    value: GridExportOptions.RICH_TEXT
                  }
                ]}
                value={values.value}
                variant='standard'
                onChange={this.handleChange}
              />
            </div>
            {values.value === GridExportOptions.PLAIN_TEXT && (
              <div className={classes.field}>
                <Message type='warning'>
                  {messages.get(messages.EXPORT_PLAIN_TEXT_WARNING)}
                </Message>
              </div>
            )}
            <div className={classes.field}>
              <Button
                label={messages.get(messages.EXPORT)}
                type='neutral'
                onClick={this.handleExport}
              />
            </div>
          </Container>
        </Popover>
      </div>
    )
  }
}

export default _.flow(withStyles(styles))(GridExports)
