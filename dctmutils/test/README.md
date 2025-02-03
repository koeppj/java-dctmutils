# Documentum Utilities

Basic command line utilities to test connectivity to Documentum and to export content and metadata 
from Documentum.

## Supported Platforms and Dependenencies

* Supported Platforms: Windows, Linux and MacOS.
* Java: Compiled for Java 11 or above.
* Documentum Foundation Classes: Tested with Version 22, likely support for older or new versions.

## Setup

* Unzip or Untar distribution to location of your choice (hereafter referred to as <install_dir>).
* For Linux/MacOS only, run chmod u+x <install_dit>/bin/dctmutils.
* Ensure that the environment variable DM_MODE points to a directory containig the Documentum DFC Java Libriaries (*.jar files).
* Using as templates the *.sample files in <install_dir>/etc, create .properties for yur dfc and log4j configurations.

## Running

* Run <install_dir>/bin\dctmutils.bat --help (windows) or <install_dir>/bin/dctmutils --help to get a list of commands.
* Run <install_dir>/bin/dctmutils.bat <command> --help to get help on a specific command.
* Parameters may be specified as named arguments on the command line or specified using key/value pairs in a configuration file idenfitied using --config <path to config file> (see <install_dir>/etc/config.properties.sample for an example).
