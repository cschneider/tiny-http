# Tiny HTTP server

Minimal implementation of a http server for OSGi or standalone usage.

## Features

* Can be dynamically configured using configuration admin. Supports Handler SPI that allows to configure which url patterns are served by which Handler implemention.
* Built in FileHandler to server static files. Supports a small set of mime types.
* Works with typical browsers as clients.
* Fully automated integration tests in plain java and OSGi.
* Packaging as bndtools runnable jar for simple manual testing.
* Uses java sockets with configurable number of receive threads.

## Limitations

Only very simple support for headers in FileHandler (Only Statusline and Content-Type).

## Build

	mvn clean install

## Test

	cd index
	java -jar target/tinyhttp.jar

Runs web server with some static files in the web directory.

Use browser to test URL

	http://localhost:8080

## Test from eclipse and bndtools

Import as existing maven projects on top level.
Open file index/tinyhttp.bndrun
Run or Debug from bndtools editor.

## Dynamic configuration

Can be tested by changing the config in index/etc.

## License

The code is offered under the Apache License v2.
The example image wispy-cirrus-clouds is from http://www.photos-public-domain.com/2016/02/23/wispy-cirrus-clouds/.

