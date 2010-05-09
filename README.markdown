netty-tools
============

A small collection of tools useful when working with [JBoss Netty](http://www.jboss.org/netty).
The HTTP tools are useful for adding simple web features to your server.

### Included
* **HTTP File Server**<br>
 A file serving handler based on Trustin Lee's example. Supports serving from file system or class path.
* **HTTP Cache**<br>
A simple cache to be used in conjunction with the file server (see above). Will cache served files in-memory. Intended for caching smaller files.
* **HTTP Router**<br>
Simple router that can route incoming HTTP requests to different ChannelHandlers.
Supports very basic matching rules.
* **Bandwidth meter**<br>
A monitor to measure the use of bandwidth in your Netty application. Place it first in your pipeline and it will measure the size of sent/received ChannelBuffers.

Netty is not a web server and these tools are quite "raw" in the HTTP sense. If you intend to do anything moderately complex, please look [elsewhere](http://jetty.codehaus.org/jetty/).
Usually, what you want is to add a small web interface to your Netty application for administration purposes. That's where these tools may come in handy.

Getting started
----------
To get up and running try one of the following:
### A) Download the binary
Get the pre-compiled JAR and put it on your class path. Easy as pie!
### B) Compile it yourself
You can also compile it yourself using Maven. Get the source code and run:

    mvn install

This will install the library in your local Maven repository.
Use the following dependency block to import:

    <dependency>
        <groupId>se.cgbystrom.netty</groupId>
        <artifactId>netty-tools</artifactId>
        <version>1.0.0</version>
    </dependency>

## Examples
As of now, please see the tests for example usage.

## Authors

- Carl Bystr&ouml;m <http://www.pedantique.org/>

## License

Open source licensed under the MIT license (see _LICENSE_ file for details).
