import os
import syslog
from datetime import datetime

import click
from dateutil.relativedelta import relativedelta
from tabulate import tabulate

from . import pybis

syslog.openlog("pyBIS")


def openbis_conn_options(func):
    options = [
        click.option("-h", "--hostname", help="Hostname OPENBIS_HOSTNAME"),
        click.option("-u", "--username", help="Username OPENBIS_USERNAME"),
        click.option("-p", "--password", help="Password OPENBIS_PASSWORD"),
        click.option(
            "--ignore-certificate",
            is_flag=True,
            default=True,
            help="Ignore SSL certificate of openBIS host",
        ),
    ]
    # we use reversed(options) to keep the options order in --help
    for option in reversed(options):
        func = option(func)
    return func


def login_options(func):
    options = [
        click.argument("hostname"),
        click.option(
            "--ignore-certificate",
            is_flag=True,
            default=True,
            help="Ignore SSL certificate of openBIS host",
        ),
    ]
    # we use reversed(options) to keep the options order in --help
    for option in reversed(options):
        func = option(func)
    return func


def get_openbis(
    hostname=None,
    username=None,
    password=None,
    ignore_certificate=False,
    session_token_needed=False,
):
    """Order of priorities:
    1. direct specification via --hostname https://openbis.domain
    2. Environment variable: OPENBIS_HOST=https://openbis.domain
    3. local pybis configuration (hostname.pybis.json)
    4. local obis configuration (in .obis)
    5. global pybis configuration (~/.pybis/hostname.pybis.json)
    6.
    """

    config = pybis.get_local_config()

    if not hostname:
        hostname = os.getenv("OPENBIS_HOSTNAME")
    if not hostname:
        hostname = config.get("hostname")
    if not hostname:
        hostname = click.prompt("openBIS hostname")

    token = pybis.get_token_for_hostname(
        hostname, session_token_needed=session_token_needed
    )
    openbis = pybis.Openbis(
        url=hostname,
        verify_certificates=not ignore_certificate,
    )
    if token:
        try:
            openbis.set_token(token)
            return openbis
        except Exception:
            pass

    if not username:
        username = os.getenv("OPENBIS_USERNAME")
    if not username:
        username = click.prompt("Username")

    if not password:
        password = os.getenv("OPENBIS_PASSWORD")
    if not password:
        password = click.prompt("Password", hide_input=True)
    try:
        openbis.login(
            username=username,
            password=password,
            save_token=True,
        )
        return openbis
    except ValueError as exc:
        click.echo(f"Failed to login to {hostname}")


@click.group()
@click.version_option()
def cli():
    """pybis - command line access to openBIS"""


@cli.group()
@click.pass_obj
def space(ctx):
    """manage spaces"""
    pass


@space.command("get")
@openbis_conn_options
@click.argument("identifier", required=True)
def get_space(identifier, **kwargs):
    """get a space by its identifier"""
    openbis = get_openbis(**kwargs)
    try:
        space = openbis.get_space(identifier)
    except ValueError as exc:
        raise click.ClickException(exc)
    click.echo(space.__repr__())


@space.command("list")
@openbis_conn_options
def get_spaces(**kwargs):
    """get all spaces in an openBIS instance"""
    openbis = get_openbis(**kwargs)
    try:
        spaces = openbis.get_spaces()
    except ValueError as exc:
        raise click.ClickException(exc)
    click.echo(spaces.__repr__())


@cli.group()
@click.pass_obj
def project(ctx):
    """manage projects"""
    pass


@project.command("get")
@openbis_conn_options
@click.argument("identifier", required=True)
def get_project(identifier, **kwargs):
    """get a project by its identifier"""
    openbis = get_openbis(**kwargs)
    try:
        project = openbis.get_project(identifier)
    except ValueError as exc:
        raise click.ClickException(exc)
    click.echo(project.__repr__())


@project.command("list")
@openbis_conn_options
@click.argument("space", required=True)
def get_projects(space, **kwargs):
    """get all projects of a given space"""
    openbis = get_openbis(**kwargs)
    try:
        projects = openbis.get_projects(space=space)
    except ValueError as exc:
        raise click.ClickException(exc)
    click.echo(projects.__repr__())


@cli.group()
@click.pass_obj
def collection(ctx):
    """manage collections"""
    pass


@collection.command("get")
@openbis_conn_options
@click.argument("identifier", required=True)
def get_collection(identifier, **kwargs):
    """get collection by its identifier or permId"""
    openbis = get_openbis(**kwargs)
    try:
        coll = openbis.get_collection(identifier)
    except ValueError as exc:
        raise click.ClickException(exc)
    click.echo(collection.__repr__())


@collection.command("list")
@openbis_conn_options
@click.argument("identifier", required=True)
def get_collections(identifier, **kwargs):
    """list all collections in a given project or space"""
    openbis = get_openbis(**kwargs)
    try:
        entity = openbis.get_space(identifier)
    except ValueError as exc:
        try:
            entity = openbis.get_project(identifier)
        except ValueError as exc:
            raise click.ClickException(f"No space or project found for {identifier}")
    colls = entity.get_collections()
    click.echo(colls.__repr__())


@collection.command("get")
@openbis_conn_options
@click.argument("identifier", required=True)
def get_collection(identifier, **kwargs):
    """get collection by its identifier or permId"""
    openbis = get_openbis(**kwargs)
    try:
        coll = openbis.get_collection(identifier)
    except ValueError as exc:
        raise click.ClickException(exc)
    click.echo(coll.__repr__())


@collection.command("datasets")
@openbis_conn_options
@click.argument("identifier", required=True)
def list_datasets_in_collection(identifier, **kwargs):
    """list all datasets of a given collection"""
    openbis = get_openbis(**kwargs)
    try:
        coll = openbis.get_collection(identifier)
    except ValueError as exc:
        raise click.ClickException(exc)
    datasets = coll.get_datasets()
    click.echo(datasets.__repr__())


@collection.command("download")
@openbis_conn_options
@click.argument("identifier", required=True)
def download_dataset_in_collection(identifier, **kwargs):
    """download all datasets of a given collection"""
    openbis = get_openbis(**kwargs)
    try:
        coll = openbis.get_collection(identifier)
    except ValueError as exc:
        raise click.ClickException(exc)
    datasets = coll.get_datasets()
    for dataset in datasets:
        click.echo(dataset.permId, nl=False)
        if dataset.status != "AVAILABLE":
            click.echo(f"dataset is not AVAILABLE: {dataset.status}")
            continue
        dest = dataset.download()
        click.echo(f" {dest}")
        syslog.syslog(
            syslog.LOG_INFO,
            f"{openbis.hostname} | {openbis.username} | {dataset.permId}",
        )


@cli.group()
@click.pass_obj
def sample(ctx):
    """manage samples"""
    pass


@sample.command("get")
@openbis_conn_options
@click.argument("identifier", required=True)
def get_sample(identifier, **kwargs):
    """get sample by its identifier or permId"""
    openbis = get_openbis(**kwargs)
    try:
        sample = openbis.get_sample(identifier)
    except ValueError as exc:
        raise click.ClickException(exc)
    click.echo(sample.__repr__())


@sample.command("list")
@openbis_conn_options
@click.argument("identifier", required=True)
def get_samples(identifier, **kwargs):
    """list all samples of a given space, project or collection"""
    openbis = get_openbis(**kwargs)
    try:
        entity = openbis.get_space(identifier)
    except ValueError as exc:
        try:
            entity = openbis.get_project(identifier)
        except ValueError as exc:
            try:
                entity = openbis.get_collection(identifier)
            except ValueError as exc:
                raise click.ClickException(
                    f"could not find any space, project or collection for {identifier}"
                )
    samples = entity.get_samples()
    click.echo(samples.__repr__())


@sample.command("datasets")
@openbis_conn_options
@click.argument("identifier", required=True)
def list_datasets_in_sample(identifier, **kwargs):
    """list all datasets of a sample"""
    openbis = get_openbis(**kwargs)
    try:
        sample = openbis.get_sample(identifier)
    except ValueError as exc:
        raise click.ClickException(exc)
    datasets = sample.get_datasets()
    click.echo(datasets.__repr__())


@sample.command("download")
@openbis_conn_options
@click.argument("identifier", required=True)
def download_datasets_in_sample(identifier, **kwargs):
    """download all datasets of a sample"""
    openbis = get_openbis(**kwargs)
    try:
        sample = openbis.get_sample(identifier)
    except ValueError as exc:
        raise click.ClickException(exc)
    datasets = sample.get_datasets()
    for dataset in datasets:
        click.echo(dataset.permId, nl=False)
        if dataset.status != "AVAILABLE":
            click.echo(f"dataset is not AVAILABLE: {dataset.status}")
            continue
        dest = dataset.download()
        click.echo(f" {dest}")
        syslog.syslog(
            syslog.LOG_INFO,
            f"{openbis.hostname} | {openbis.username} | {dataset.permId}",
        )


@cli.group()
@click.pass_obj
def dataset(ctx):
    """manage dataset"""
    pass


@dataset.command("get")
@openbis_conn_options
@click.argument("permid", required=True)
def get_dataset(permid, **kwargs):
    """get dataset meta-information by its permId"""
    openbis = get_openbis(**kwargs)
    try:
        ds = openbis.get_dataset(permid)
    except ValueError as exc:
        raise click.ClickException(exc)
    click.echo(ds.__repr__())
    click.echo("")
    click.echo("Files in this dataset")
    click.echo("---------------------")
    click.echo(ds.get_files())


@dataset.command("download")
@openbis_conn_options
@click.argument("permid", required=True)
@click.argument("fileno", nargs=-1)
@click.option(
    "--destination",
    "-d",
    type=click.Path(exists=True),
    help="where to download your dataset",
)
def download_dataset(permid, destination, fileno, **kwargs):
    """download dataset files"""
    openbis = get_openbis(**kwargs)
    try:
        dataset = openbis.get_dataset(permid)
    except ValueError as exc:
        raise click.ClickException(exc)

    create_default_folders = False if destination else True
    if fileno:
        all_files = dataset.get_files()
        files = []
        for loc in fileno:
            files.append(all_files.loc[int(loc)]["pathInDataSet"])
        click.echo(files)
        dataset.download(
            destination=destination,
            create_default_folders=create_default_folders,
            files=files,
        )
    else:
        dataset.download(
            destination=destination, create_default_folders=create_default_folders
        )
    syslog.syslog(
        syslog.LOG_INFO,
        f"{openbis.hostname} | {openbis.username} | {dataset.permId}",
    )


@cli.command("local", context_settings=dict(ignore_unknown_options=True))
@click.argument("hostname", required=False)
@click.argument("token", required=False, type=click.UNPROCESSED)
@click.option("--info", is_flag=True, help="get more detailed information")
def get_set_hostname(hostname, token, info):
    """show or set hostname and token that is used locally."""
    if hostname:
        if token and token.startswith("-"):
            token = "$pat" + token
        pybis.set_local_config(hostname=hostname, token=token)
    else:
        # get hostname and token stored in .pybis.json
        config = pybis.get_local_config()
        if info:
            o = pybis.Openbis(url=config.get("hostname", ""))
            session_info = o.get_session_info(token=config.get("token"))
            click.echo(session_info)
        else:
            click.echo(
                tabulate(
                    [[config.get("hostname", ""), config.get("token", "")]],
                    headers=["openBIS hostname", "token"],
                )
            )


@cli.group()
@click.pass_obj
def token(ctx):
    """manage openBIS tokens"""
    pass


@token.command("pats")
@click.argument("hostname", required=False)
@click.argument("session-name", required=False)
@click.pass_obj
def get_pats(ctx, hostname, session_name=None):
    """list stored openBIS Personal Access Tokens (PAT)"""
    tokens = pybis.get_saved_pats(hostname=hostname, sessionName=session_name)
    headers = ["hostname", "permId", "sessionName", "validToDate"]
    token_list = [[token[key] for key in headers] for token in tokens]
    click.echo(
        tabulate(
            token_list,
            headers=[
                "openBIS hostname",
                "personal access token",
                "sessionName",
                "valid until",
            ],
        )
    )


@token.command("session")
@click.pass_obj
def new_token(ctx, **kwargs):
    """create new openBIS Session Token"""
    click.echo("new_token()")


@token.command("sessions")
@click.pass_obj
def get_tokens(ctx, **kwargs):
    """list stored openBIS Session Tokens"""
    tokens = pybis.get_saved_tokens()
    token_list = [[key, tokens[key]] for key in tokens]
    click.echo(tabulate(token_list, headers=["openBIS hostname", "session token"]))


@token.command("pat")
@login_options
@click.argument("session-name")
@click.option("--validity-days", help="Number of days the token is valid")
@click.option("--validity-weeks", help="Number of weeks the token is valid")
@click.option("--validity-months", help="Number of months the token is valid")
@click.pass_obj
def new_pat(ctx, hostname, session_name, **kwargs):
    """create new openBIS Personal Access Token"""
    validTo = datetime.now()
    if kwargs.get("validity_months"):
        validTo += relativedelta(months=int(kwargs.get("validity_months")))
    elif kwargs.get("validity_weeks"):
        validTo += relativedelta(weeks=int(kwargs.get("validity_weeks")))
    elif kwargs.get("validity_days"):
        validTo += relativedelta(days=int(kwargs.get("validity_days")))
    else:
        validTo += relativedelta(years=1)
    o = get_openbis(hostname=hostname, session_token_needed=True, **kwargs)
    try:
        new_pat = o.new_personal_access_token(sessionName=session_name, validTo=validTo)
    except Exception as exc:
        raise click.ClickException(
            f"Creation of new personal access token failed: {exc}"
        )
    click.echo(new_pat)
    o.get_personal_access_tokens(save_to_disk=True)


if __name__ == "__main__":
    cli()
