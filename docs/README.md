# MQTT Command Line Interface Documentation

The documentation uses [Jekyll](https://jekyllrb.com/) as a static site generator.

## Setup

In order to get this handbook built or work on it, you'll need [rbenv](https://github.com/rbenv/rbenv), a version
switcher for the ruby programming language and [bundler](https://bundler.io/), a package repository for ruby gems.

### rbenv

- MacOS:
    1. Install [Homebrew](_dev/environment/brew.md)
    2. `brew install rbenv ruby-build`
    3. `rbenv init`
- Linux:
    1. `sudo apt install rbenv`
    2. `rbenv init`
- Windows: Please follow [rbenv-win](https://github.com/nak1114/rbenv-win)

In the project directory execute: `rbenv install`

This uses the version that's hard linked in this directory's `.ruby-version` file. The ruby version is also bumped via
the `.ruby-version` file.

### bundler

In the project directory execute:

1. `gem install --user-install bundler`
2. `bundle install`

## Build

1. `bundler exec jekyll serve` (add `--incremental` for incremental and shorter builds)
2. Open your browser at http://localhost:4000/
