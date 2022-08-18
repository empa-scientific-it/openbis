from lib2to3.pgen2.token import OP
import click
from tabulate import tabulate
from . import pybis


def common_options(func):
    options = [
        click.argument("hostname", required=False),
        click.option("-u", "--username", help="Username"),
        click.option("-t", "--token", help="Session Token or Personal Access Token"),
        click.option(
            "--verify-certificate",
            is_flag=True,
            default=True,
            help="Verify SSL certificate of openBIS host",
        ),
    ]
    # we use reversed(options) to keep the options order in --help
    for option in reversed(options):
        func = option(func)
    return func


@click.group()
@click.pass_context
def cli(ctx):
    """pybis - command line access to openBIS"""
    pass


@cli.group()
def get():
    """get openBIS entities"""


@get.command("tokens")
@common_options
@click.pass_obj
def get_tokens(ctx, **kwargs):
    """get openBIS Session Tokens"""
    tokens = pybis.get_saved_tokens()
    token_list = [[key, *tokens[key]] for key in tokens]
    print(tabulate(token_list, headers=["openBIS hostname", "token"]))


@get.command("pats")
@click.option("--remote", "-r", is_flag=True, default=False, help="")
@common_options
@click.argument("session-name", required=False)
@click.pass_obj
def get_pats(ctx, hostname, session_name=None, **kwargs):
    """get openBIS Personal Access Tokens (PAT)"""
    tokens = pybis.get_saved_pats(hostname=hostname, sessionName=session_name)
    headers = ["permId", "hostname", "sessionName", "validToDate"]
    token_list = [[token[key] for key in headers] for token in tokens]
    print(
        tabulate(
            token_list,
            headers=["token", "openBIS hostname", "sessionName", "valid until"],
        )
    )


@cli.group()
@click.pass_obj
def new(ctx):
    """new openBIS entity"""
    click.echo("get()")


@new.command("token")
@common_options
@click.pass_obj
def new_token(ctx, **kwargs):
    """create new openBIS Session Token"""
    click.echo("new_token()")


@new.command("pat")
@common_options
@click.pass_obj
def new_pat(ctx, **kwargs):
    """create new openBIS Personal Access Token"""
    click.echo("new_pat()")


@cli.group()
def up():
    """update openBIS entities"""
    click.echo("up")


@up.command("token")
@common_options
@click.pass_obj
def up_token(ctx, **kwargs):
    """update openBIS session token"""
    click.echo("up token")


@up.command("pat")
@click.pass_obj
def up_token(ctx):
    """update openBIS Personal Access Token"""
    click.echo("up token")


@cli.group("del")
@common_options
@click.pass_obj
def delete(ctx, **kwargs):
    """delete openBIS entities"""
    click.echo("del")


@delete.command("token")
def del_token():
    """delete openBIS token"""
    click.echo("delete token")


if __name__ == "__main__":
    cli()
