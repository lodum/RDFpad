RDFpad
======

RDFpad is a Jena-based Linked Data server that reads RDF in Turtle notation from any etherpad and provides it in various RDF serializations (each served with the corresponding content-type headers), plus a human-friendly HTML view.

RDFpad therefore supports quick and dirty vocabulary development, without the hassle of bringing your code online in a second step – it is already online while you develop it.

## Features

*Reads turtle code from any etherpad and checks it for errors
*Acts as a proxy to serve the corresponding RDF model as RDF/XML, N-Triples, N3 (and Turtle, of course) with the correct content-type headers
*Proper 303 redirects; the same URI forwards to different encodings, depending on the Accept-header sent by the client
*Provides a human-friendly HTML view of the code, with direct access to the various RDF serializations and a forwarding option to the Sindice Inspector

## Links

A running demo is available at http://rdfpad.lodum.de/
This [blog post](http://lodum.de/rdfpad/) explains the idea behind RDFpad in a bit more detail

## Required Libraries
You'll need the libs that come with [Jena](http://jena.sourceforge.net/) (tested with version 2.6.4) and [Jetty](http://jetty.codehaus.org/jetty/) (tested with version 7.3.0). RDFpad is an embedded server, so there is no need for a servlet container (Tomcat or the like).

##Running RDFpad

Assuming you have the required libs on your path, simply start the server via

java de.lodum.rdfpad.RDFpadServer [port]

For example, `java de.lodum.rdfpad.RDFpadServer 8080` . Note that there are some caveats if you want to run RDFpad on port 80 on a Unix-based system.

## License

RDFpad is open source software under a [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0).

