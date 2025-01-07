# Simple Documentum Command Line Client

Basic command line client utilities for OpenText Documentum.  Relies on OpenText Documentum DFC which are NOT provided and can ONLY be obtained from OpenText.  Unique in that it does not required a "dfc.properties" file to work.  All command line options allow you to specify a docbroker host and port, thus making it particularly useful for quickly checking docbroker connectivity, docbase status and other jobs where the overhead of setting up a config file first is a real pain.

## Requiremments

- Java 11 or Above
- Documentum Foundatoion Classes (DFC)
- Windows, Linux and MacOS Supported

## Instructions

* Make sure environment variable DM_HOME is set to the directory containing the DFC Libararies (.jar files).