import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import flow from 'lodash/flow'

import Card from '@material-ui/core/Card'
import Typography from '@material-ui/core/Typography'
import Collapse from '@material-ui/core/Collapse'

import FormValidator from '@src/js/components/common/form/FormValidator.js'
import Container from '@src/js/components/common/form/Container.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import Button from '@src/js/components/common/form/Button.jsx'

import AppController from '@src/js/components/AppController.js'
import openbis from '@src/js/services/openbis.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

import Logo from '@src/resources/img/openbis-logo-transparent.png'

const styles = theme => ({
  card: {
    marginTop: '10%',
    marginBottom: '10em',
    width: '30em',
    margin: '0 auto'
  },
  logo: {
    display: 'flex',
    justifyContent: 'center'
  },
  header: {
    marginBottom: theme.spacing(1)
  },
  field: {
    marginBottom: theme.spacing(1)
  },
  button: {
    marginTop: theme.spacing(1)
  },
  container: {
    width: '100%',
    height: '100%',
    overflow: 'auto'
  }
})

const AUTHENTICATION_SERVICE_OPENBIS = 'openBIS'
const AUTHENTICATION_SERVICE_SWITCH_AAI = 'SWITCHaai'

class WithLogin extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      loaded: false,
      authenticationService: {
        value: null
      },
      user: {
        value: null,
        error: null
      },
      password: {
        value: null,
        error: null
      },
      selection: null,
      validate: FormValidator.MODE_BASIC
    }
    this.references = {
      authenticationService: React.createRef(),
      user: React.createRef(),
      password: React.createRef()
    }

    this.handleKeyPress = this.handleKeyPress.bind(this)
    this.handleChange = this.handleChange.bind(this)
    this.handleBlur = this.handleBlur.bind(this)
    this.handleLogin = this.handleLogin.bind(this)
  }

  async componentDidMount() {
    const serverInformation = await openbis.getServerPublicInformation()

    if (this.getSwitchAaiLink(serverInformation)) {
      this.setState({
        loaded: true,
        authenticationService: {
          value: AUTHENTICATION_SERVICE_SWITCH_AAI
        },
        selection: 'authenticationService',
        serverInformation
      })
    } else {
      this.setState({
        loaded: true,
        authenticationService: {
          value: AUTHENTICATION_SERVICE_OPENBIS
        },
        selection: 'user',
        serverInformation
      })
    }
  }

  componentDidUpdate(prevProps, prevState) {
    if (this.state.selection !== prevState.selection) {
      this.focus()
    }
  }

  focus() {
    const reference = this.references[this.state.selection]
    if (reference && reference.current) {
      reference.current.focus()
    }
  }

  validate(autofocus) {
    const validator = new FormValidator(this.state.validate)
    validator.validateNotEmpty(this.state, 'user', messages.get(messages.USER))
    validator.validateNotEmpty(
      this.state,
      'password',
      messages.get(messages.PASSWORD)
    )

    let selection = null

    if (autofocus && !_.isEmpty(validator.getErrors())) {
      selection = new String(validator.getErrors()[0].name)
    }

    this.setState({
      ...validator.withErrors(this.state),
      selection
    })

    return _.isEmpty(validator.getErrors())
  }

  handleKeyPress(event) {
    if (event.key === 'Enter') {
      this.handleLogin()
    }
  }

  handleChange(event) {
    this.setState({
      [event.target.name]: {
        ...this.state[event.target.name],
        value: event.target.value
      }
    })
  }

  handleBlur() {
    this.validate()
  }

  handleLogin() {
    if (this.props.disabled) {
      return
    }

    this.setState(
      {
        validate: FormValidator.MODE_FULL
      },
      () => {
        if (this.validate(true)) {
          AppController.getInstance().login(
            this.state.user.value,
            this.state.password.value
          )
        }
      }
    )
  }

  getSwitchAaiLink(serverInformation) {
    let link = serverInformation
      ? serverInformation['authentication-service.switch-aai.link']
      : null
    if (link) {
      link = link.replaceAll('${host}', window.location.hostname)
      link = link.replaceAll('${current-url}', window.location.href)
    }
    return link
  }

  getSwitchAaiLabel(serverInformation) {
    let label = serverInformation
      ? serverInformation['authentication-service.switch-aai.label']
      : null
    if (!label) {
      label = messages.get(messages.AUTHENTICATION_SERVICE_SWITCH_AAI)
    }
    return label
  }

  render() {
    logger.log(logger.DEBUG, 'Login.render')

    const { loaded } = this.state

    if (!loaded) {
      return null
    }

    const { classes } = this.props

    return (
      <div>
        <div className={classes.container}>
          <form>
            <Card classes={{ root: classes.card }}>
              <Container square={true}>
                <div className={classes.logo}>
                  <img src={Logo} width='200' />
                </div>
                <Typography variant='h6' classes={{ root: classes.header }}>
                  Login
                </Typography>
                {this.renderAuthenticationServiceChoice()}
                {this.renderOpenBISAuthentication()}
                {this.renderSwitchAaiAuthentication()}
              </Container>
            </Card>
          </form>
        </div>
      </div>
    )
  }

  renderAuthenticationServiceChoice() {
    const { classes } = this.props
    const { serverInformation } = this.state

    if (this.getSwitchAaiLink(serverInformation)) {
      const options = []

      options.push({
        value: AUTHENTICATION_SERVICE_SWITCH_AAI,
        label: this.getSwitchAaiLabel(serverInformation)
      })
      options.push({
        value: AUTHENTICATION_SERVICE_OPENBIS,
        label: messages.get(messages.AUTHENTICATION_SERVICE_OPENBIS)
      })

      return (
        <div className={classes.field}>
          <SelectField
            reference={this.references.authenticationService}
            id='authenticationService'
            name='authenticationService'
            label={messages.get(messages.AUTHENTICATION_SERVICE)}
            value={this.state.authenticationService.value}
            options={options}
            sort={false}
            mandatory={true}
            onChange={this.handleChange}
            onBlur={this.handleBlur}
          />
        </div>
      )
    } else {
      return null
    }
  }

  renderOpenBISAuthentication() {
    const { classes } = this.props

    return (
      <div>
        <Collapse
          in={
            this.state.authenticationService.value ===
            AUTHENTICATION_SERVICE_OPENBIS
          }
          mountOnEnter={true}
          unmountOnExit={true}
        >
          <div className={classes.field}>
            <TextField
              reference={this.references.user}
              id='standard-name'
              name='user'
              label={messages.get(messages.USER)}
              value={this.state.user.value}
              error={this.state.user.error}
              mandatory={true}
              autoComplete='username'
              onKeyPress={this.handleKeyPress}
              onChange={this.handleChange}
              onBlur={this.handleBlur}
            />
          </div>
          <div className={classes.field}>
            <TextField
              reference={this.references.password}
              id='standard-password-input'
              name='password'
              label={messages.get(messages.PASSWORD)}
              type='password'
              value={this.state.password.value}
              error={this.state.password.error}
              mandatory={true}
              autoComplete='current-password'
              onKeyPress={this.handleKeyPress}
              onChange={this.handleChange}
              onBlur={this.handleBlur}
            />
          </div>
          <Button
            label={messages.get(messages.LOGIN)}
            type='final'
            styles={{ root: classes.button }}
            onClick={this.handleLogin}
          />
        </Collapse>
      </div>
    )
  }

  renderSwitchAaiAuthentication() {
    const { classes } = this.props

    return (
      <Collapse
        in={
          this.state.authenticationService.value ===
          AUTHENTICATION_SERVICE_SWITCH_AAI
        }
        mountOnEnter={true}
        unmountOnExit={true}
      >
        <Button
          label={messages.get(messages.LOGIN)}
          type='final'
          styles={{ root: classes.button }}
          onClick={() =>
            (window.location.href = this.getSwitchAaiLink(
              this.state.serverInformation
            ))
          }
        />
      </Collapse>
    )
  }
}

export default flow(withStyles(styles))(WithLogin)
