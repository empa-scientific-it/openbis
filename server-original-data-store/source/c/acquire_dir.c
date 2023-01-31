/*
 * Copyright 2010 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 /*
  * Tool to recursively change the ownership of files to the user running this tool.
  *
  * Usage: acquire_dir <dirname>
  *
  * Compilation: cc -o acquire_dir acquire_dir.c
  *
  * It is supposed to be run SUID root, thus is very restrictive in what user is permitted to run this tool and on what directory it may be run.
  *
  * Author: Bernd Rinn
  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <dirent.h>
#include <unistd.h>
#include <pwd.h>
#include <sys/types.h>
#include <sys/dir.h>
#include <sys/param.h>
#include <sys/stat.h>

/* The prefix a path has to start with in order to be permitted for this tool. */
#define PERMITTED_PATH_PREFIX "/misc/nas/openbis"

/* The name of the user permitted to run this tool. */
#define PERMITTED_USER "openbis"

char warnings;
char workpath[MAXPATHLEN];
struct stat stat_buf;
int uid, gid;

/*
 * Checks that dir is in the permitted path and exits otherwise.
 */
void check_dir_permitted(char *dir)
{
	if (strncmp(dir, PERMITTED_PATH_PREFIX, strlen(PERMITTED_PATH_PREFIX)) != 0)
	{
		fprintf(stderr, "Directory '%s' not permitted.\n", workpath);
		exit(255);
	}
}

/*
 * Checks that the user running this program is the permitted user and exits otherwise.
 *
 * Sets uid and gid as a side effect.
 */
void check_user_permitted()
{
    struct passwd *pw;
    
    uid = getuid();
    pw = getpwuid(uid);
    if (pw == NULL)
    {
    	perror(NULL);
    	exit(255);
    }
    gid = pw->pw_gid;
    if (strcmp(pw->pw_name, PERMITTED_USER) != 0)
    {
    	fprintf(stderr, "User '%s' not permitted.\n", pw->pw_name);
    	exit(255);
    }
}

/*
 * Visit all files and directories below the current working directory and changes their ownership to uid:gid.
 */
void visit_files()
{
	DIR *dirfp;
	struct dirent *de;
	int start_fd = open(".", O_RDONLY);

    /* Change ownership of directory. */
	if (fchown(start_fd, uid, gid) < 0)
	{
		perror(NULL);
		warnings = 1;
	}
	if( (dirfp = opendir(".")) == NULL)
  	{
		perror(NULL);
		warnings = 1;
		return;
  	}

	while( (de = readdir(dirfp)) != NULL)
	{
		if (strcmp (de->d_name, ".") == 0 || strcmp(de->d_name, "..") == 0)
		{
			continue;
		}
		if (lstat(de->d_name, &stat_buf) < 0)
		{
			perror(de->d_name);
			warnings = 1;
		} else
		{
			if (S_ISDIR(stat_buf.st_mode))
			{
			 	if (chdir(de->d_name) < 0)
 				{
 					perror(de->d_name);
					warnings = 1;
		 			continue;
		 		}
				visit_files();
			 	if (fchdir(start_fd) < 0)
 				{
 					perror(NULL);
					warnings = 1;
		 			continue;
		 		}
			} else
			{
			    /* Change ownership of file. */
				if (lchown(de->d_name, uid, gid) < 0)
				{
					perror(de->d_name);
					warnings = 1;
				}
			}
		}
	}

	closedir(dirfp);
	close(start_fd);
}

int main(int argc, char *argv[]) 
{ 
 	if (argc != 2)
 	{
 		fprintf(stderr, "Usage: %s <dirname>\n", argv[0]);
 		return 254;
 	}
 	if (chdir(argv[1]) < 0)
 	{
 		perror(argv[1]);
 		return 255;
 	}
 	if (getcwd(workpath, MAXPATHLEN) == NULL )
	{
		perror(NULL);
		return 255;
	}
	check_user_permitted();
	check_dir_permitted(workpath);
	warnings = 0;
	visit_files();
	
	return warnings ? 1 : 0;
}
