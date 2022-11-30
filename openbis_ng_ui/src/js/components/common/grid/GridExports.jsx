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
    display: 'flex',
    alignItems: 'center'
  },
  popup: {
    minWidth: '200px',
    maxWidth: '300px'
  },
  field: {
    paddingBottom: theme.spacing(1),
    '&:first-child': {
      paddingTop: 0
    },
    '&:last-child': {
      paddingBottom: 0
    }
  },
  button: {
    paddingTop: theme.spacing(1)
  }
})

class GridExports extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {
      el: null
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
    const { exportOptions, onExportOptionsChange } = this.props

    if (onExportOptionsChange) {
      const newExportOptions = {
        ...exportOptions,
        [event.target.name]: event.target.value
      }
      onExportOptionsChange(newExportOptions)
    }
  }

  handleExport() {
    const { onExport } = this.props
    this.handleClose()
    if (onExport) {
      onExport()
    }
  }

  render() {
    logger.log(logger.DEBUG, 'GridExports.render')

    const {
      id,
      exportable,
      exportOptions,
      disabled,
      multiselectable,
      multiselectedRows,
      classes
    } = this.props
    const { el } = this.state

    const rowsOptions = [
      {
        label: messages.get(messages.ALL_PAGES),
        value: GridExportOptions.ALL_PAGES
      },
      {
        label: messages.get(messages.CURRENT_PAGE),
        value: GridExportOptions.CURRENT_PAGE
      }
    ]

    if (multiselectable) {
      rowsOptions.push({
        label: messages.get(messages.SELECTED_ROWS),
        value: GridExportOptions.SELECTED_ROWS
      })
    }

    const isTSVExport =
      exportable.fileFormat === GridExportOptions.TSV_FILE_FORMAT

    const isXLSEntityExport =
      exportable.fileFormat === GridExportOptions.XLS_FILE_FORMAT &&
      exportable.fileContent === GridExportOptions.ENTITIES_CONTENT

    const isXLSTypesExport =
      exportable.fileFormat === GridExportOptions.XLS_FILE_FORMAT &&
      exportable.fileContent === GridExportOptions.TYPES_CONTENT

    const isXLSVocabulariesExport =
      exportable.fileFormat === GridExportOptions.XLS_FILE_FORMAT &&
      exportable.fileContent === GridExportOptions.VOCABULARIES_CONTENT

    return (
      <div className={classes.container}>
        <Button
          id={id + '.exports-button-id'}
          label={messages.get(messages.EXPORTS)}
          color='default'
          disabled={disabled}
          variant='outlined'
          onClick={this.handleOpen}
        />
        <Popover
          id={id + '.exports-popup-id'}
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
          <Container square={true} className={classes.popup}>
            <div>
              {(isTSVExport || isXLSEntityExport) && (
                <div className={classes.field}>
                  <SelectField
                    label={messages.get(messages.COLUMNS)}
                    name='columns'
                    options={[
                      {
                        label: messages.get(messages.ALL_COLUMNS),
                        value: GridExportOptions.ALL_COLUMNS
                      },
                      {
                        label: messages.get(messages.VISIBLE_COLUMNS),
                        value: GridExportOptions.VISIBLE_COLUMNS
                      }
                    ]}
                    value={exportOptions.columns}
                    variant='standard'
                    onChange={this.handleChange}
                  />
                </div>
              )}
              <div className={classes.field}>
                <SelectField
                  label={messages.get(messages.ROWS)}
                  name='rows'
                  options={rowsOptions}
                  value={exportOptions.rows}
                  variant='standard'
                  onChange={this.handleChange}
                />
              </div>
              {(isTSVExport || isXLSEntityExport) && (
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
                    value={exportOptions.values}
                    variant='standard'
                    onChange={this.handleChange}
                  />
                </div>
              )}
              {exportOptions.values === GridExportOptions.PLAIN_TEXT && (
                <div className={classes.field}>
                  <Message type='warning'>
                    {messages.get(messages.EXPORT_PLAIN_TEXT_WARNING)}
                  </Message>
                </div>
              )}
            </div>
            {(isXLSTypesExport || isXLSVocabulariesExport) && (
              <div className={classes.field}>
                <SelectField
                  label={messages.get(messages.INCLUDE_DEPENDENCIES)}
                  name='includeDependencies'
                  options={[
                    {
                      label: messages.get(messages.YES),
                      value: true
                    },
                    {
                      label: messages.get(messages.NO),
                      value: false
                    }
                  ]}
                  value={exportOptions.includeDependencies}
                  variant='standard'
                  onChange={this.handleChange}
                />
              </div>
            )}
            <div className={classes.button}>
              <Button
                id={id + '.trigger-exports-button-id'}
                label={messages.get(messages.EXPORT)}
                type='neutral'
                disabled={
                  exportOptions.rows === GridExportOptions.SELECTED_ROWS &&
                  _.isEmpty(multiselectedRows)
                }
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
