from lib2to3.pgen2.token import OP
import click
from tabulate import tabulate
from . import pybis


@click.group()
@click.option("-h", "--hostname", help="openBIS hostname")
@click.option("-u", "--username", help="Username")
@click.option("-t", "--token", help="Personal Access Token (PAT)")
@click.pass_context
def cli(ctx, hostname, username, token):
    """pybis - command line access to openBIS"""
    if hostname:
        ctx.obj["openbis"] = pybis.Openbis(url=hostname)


@cli.group()
def get():
    """get openBIS entities"""


@get.command("tokens")
@click.pass_obj
def get_tokens(ctx):
    """get openBIS session tokens"""
    tokens = pybis.get_saved_tokens()
    token_list = [[key, *tokens[key]] for key in tokens]
    print(tabulate(token_list, headers=["openBIS hostname", "token"]))


@get.command("pats")
@click.pass_obj
def get_pats(ctx):
    """get openBIS Personal Access Tokens (PAT)"""
    tokens = pybis.get_saved_tokens()
    token_list = [[key, *tokens[key]] for key in tokens]
    print(tabulate(token_list, headers=["openBIS hostname", "token"]))


@cli.group()
@click.pass_obj
def new(ctx):
    """new openBIS entity"""
    click.echo("get()")


@new.command("token")
@click.pass_obj
def new_token(ctx):
    """create new openBIS Session Token"""
    click.echo("new_token()")


@new.command("pat")
@click.pass_obj
def new_pat(ctx):
    """create new openBIS Personal Access Token"""
    click.echo("new_pat()")


@cli.group()
def up():
    """update openBIS entities"""
    click.echo("up")


@up.command("token")
@click.pass_obj
def up_token(ctx):
    """update openBIS session token"""
    click.echo("up token")


@up.command("pat")
@click.pass_obj
def up_token(ctx):
    """update openBIS Personal Access Token"""
    click.echo("up token")


@cli.group("del")
def delete():
    """delete openBIS entities"""
    click.echo("del")


@delete.command("token")
def del_token():
    """delete openBIS token"""
    click.echo("delete token")


if __name__ == "__main__":
    cli()
