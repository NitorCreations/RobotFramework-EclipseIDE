# license-maintainer
Maintains copyright/license preamble in source files etc in your project.

This project provides a tool that hooks into the `git commit` process to maintain copyright/license preamble in the files in your project.

When committing files, the tool inspects the files you are about to commit to see if an expected license header is present. If there is no license header, it is added, and if it already exists, it updates it to make sure that:

* the license is formatted properly
* the copyright year list contains the current year
* the list of organizations/individuals holding the copyright contains the origanization/individual represented by the user of the git checkout

Additionally, it also scans files that are not about to be committed, and warns if there are any files that don't contain the expected license. The warning includes instructions how to automatically add licenses to those files as a separate commit.

You can freely configure which files (using wildcards) should contain a license, and in what format the license should be presented using license template files. The license template files may contain the dynamic variables AUTHORS and YEARS that are then updated on each commit. Example Apache 2.0 license templates are included in a few different presentations.

In the suggested default configuration, the tool is imported to be a part of your project's source tree, and can be updated at will from this (or your own forked version of this) repository.

# Requirements

* Perl 5
* Bash (for automatic install)

Honestly I have only tested this on Linux so far. Please file any issues you have with any operating system you end up using this tool in.

# Overview of the relevant part of this repository

* `.githooks/`
* `.githooks/README.md` - this file
* `.githooks/LICENSE` - license by which the license-maintainer is distributed
* `.githooks/install` - script for automatic install
* `.githooks/pre-commit` - entry point for "pre-commit" git hook, has the git-specific parts
* `.githooks/license.pm` - perl module for adding & updating license in a single file at a time
* `.githooks/LICENSE-hash` - sample (Apache 2.0) license file formatted for inclusion in files using `#` for end-of-line comments
* `.githooks/LICENSE-javadoc` - sample (Apache 2.0) license file formatted for inclusion with javadoc style comments

# Importing the license maintainer into your project

* **one-shot**: If you want, you can just copy the `.githooks` directory into the root of your project. Jump over the rest of this section and continue from the "Enabling the license maintainer in a git repository" section after copying the directory.
* **the git way**: I recommend importing it using git itself, which will allow you to update it easily later, should you want to. Continue with the instructions below.

The "master" branch of this project contains both the .githooks directory (containing the files you want) and other project files such as this README file. But for your own project you want just the .githooks directory. So for easy deployment into your project, there is a separate "hooks-only" branch which contains just the .githooks directory.

## Adding the license-maintainer repository as a remote repository in your project

So, we add a git remote called `githooks-license-maintainer` for the hooks-only branch from this repository.

    git remote add --no-tags -t hooks-only githooks-license-maintainer https://github.com/NitorCreations/license-maintainer

    # or if you have a github account set up:

    git remote add --no-tags -t hooks-only githooks-license-maintainer git@github.com:NitorCreations/license-maintainer.git

## Importing / updating the license-maintainer code into your project

To import and later update the license-maintainer code in your project, execute these commands in your "master" branch of your project:

    git fetch githooks-license-maintainer
    git merge githooks-license-maintainer/hooks-only

Now you have the code in your repository. The next step is to enable it.

# Configuration

Now we configure which files should have automatic copyright/license maintenance, and which style of copyright/license should be used for each.

*The license maintainer configuration is currently inside the pre-commit git hook script - hopefully we will later extract the configuration to a separate file or use some existing git construct to configure it.*

So, open the `pre-commit` script, and locate the following snippet:

    sub resolveLicenseFormat {
        my $file = $_[0];
        my $licenseFormat;
        if ($file =~ m!\.java$!) {
            $licenseFormat = 'javadoc';
        } elsif ($file =~ m!\.txt$! || $file =~ m!\.p[lm]$! || $file eq '.githooks/pre-commit' || $file eq '.githooks/install') {
            $licenseFormat = 'hash';
        }
        return $licenseFormat;
    }

It is a function that is called once for every file for which it needs to decide what license template to use for that file, if any.
    
The default configuration (as pictured above) uses the `javadoc` license format for files ending in `.java` and the `hash` license format for files ending in `.txt` `.pm` `.pl` and additionally the license mainainer scripts. For all other files, no format is set.

The license maintainer then takes the thus determined format, adds the prefix "LICENSE-" to it. This is then the filename it will use as template for the license/copyright for the file. For example for a .java file the LICENSE-javadoc file would be used.

# Enabling the license maintainer in a git repository

Using the the license maintainer is an opt-in procedure that everybody using your repository (or rather every clone of the repository) will need to do once in order for the license maintainer to do its work.

There are two ways to do it, automatic or manual. If you don't have any other git hooks in your project, the automatic installation will do fine. However if you already have git hooks in use, you might want to manually integrate the license maintainer with your existing hooks.

## Automatic install

To automatically enable the license maintainer in your git checkout, run the following command in the base directory of your project.

    .githooks/install

This will create a symbolic link from .git/hooks/pre-commit to .githooks/pre-commit

## Manual install

If you already have a `pre-commit` hook in use, you just need to call the `.githooks/pre-commit` script at some point during the execution of your `pre-commit` script (perhaps preferrably as late as possible), and make sure that your script also exits with failure if the license-maintainer pre-commit script exits with failure when you call it.

