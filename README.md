## Grails plugin for integrating the Atmosphere Framework
https://github.com/Atmosphere/atmosphere/wiki

The plugin use the following pieces of the Atmosphere Framework:

* jquery.atmosphere.js (https://github.com/Atmosphere/atmosphere/wiki/jQuery.atmosphere.js-API)

* MeteorServlet (http://atmosphere.github.com/atmosphere/apidocs/org/atmosphere/cpr/MeteorServlet.html)


* ReflectorServletProcessor (http://atmosphere.github.com/atmosphere/apidocs/org/atmosphere/handler/ReflectorServletProcessor.html)

* DefaultBroadcaster (http://atmosphere.github.com/atmosphere/apidocs/org/atmosphere/cpr/DefaultBroadcaster.html)

* SimpleBroadcaster (http://atmosphere.github.com/atmosphere/apidocs/org/atmosphere/util/SimpleBroadcaster.html)

The plugin source can be downloaded and used as a standalone Grails application. I suggest running it first before installing the plugin and reviewing the files below to understand how it all works. Note that many of the files are not packaged into the finished plugin.

* grails-app/atmosphere/org/grails/plugins/atmosphere2/DefaultMeteorHandler.groovy

* grails-app/atmosphere/org/grails/plugins/atmosphere2/DefaultMeteorServlet.groovy

* grails-app/conf/Atmosphere2Config.groovy

* grails-app/controllers/org/grails/plugins/atmosphere2/AtmosphereTestController.groovy

* grails-app/services/org/grails/plugins/atmosphere2/AtmosphereTestService.groovy

* grails-app/views/AtmosphereTest/index.gsp: This file contains all internal JavaScript.

* src/groovy/org/grails/plugins/atmosphere2/ApplicationContextHolder (Burt Beckwith)

### Standalone Application Installation

1. Clone and extract the repository

2. cd /path/to/grails-atmosphere2

3. grails run-app

You will have a simple application that performs the following tasks out of the box. Please note that this sample is not production ready. It merely incorporates some of the lessons I have learned and provides a point of departure for your own application.

* Chat (open two different browsers on your computer and start chatting)

* One-time triggered notification

* Automatically updates the web page at predefined intervals

### Plugin Installation

The instructions assume you are using Tomcat as the servlet container.

1. cd /path/to/your/application

2. grails install-plugin atmosphere2

3. Create a MeteorServlet:
    grails create-meteor-servlet com.example.Default

4. Create a handler:
    grails create-meteor-handler com.example.Default

5. Edit grails-app/conf/Atmosphere2Config.groovy
```groovy
    import com.example.DefaultMeteorHandler

    defaultUrl = "/jabber/*"

    servlets = [
        MeteorServlet: [
        description: "MeteorServlet Default",
        className: "com.example.DefaultMeteorServlet",
        urlPattern: "/jabber*/",
        handler: DefaultMeteorHandler
        ]
    ]
```
6. Note the changes the plugin installation made to grails-app/conf/BuildConfig.groovy
```groovy
    grails.servlet.version = "3.0"
    grails.tomcat.nio = true

    grails.project.dependency.resolution = {
        dependencies {
            compile('org.atmosphere:atmosphere-runtime:1.0.9') {
                excludes 'slf4j-api', 'atmosphere-ping'
            }
        }
    }
```

7. Use the JavaScript code in grails-app/views/atmosphereTest/index.gsp to get you started with your own client implementation.

### To Do

* Write the _Uninstall.groovy script

  * Items not changed by the user are deleted or restored to their original condition

  * Provide instructions for completely removing the plugin

* Provide a generic class that implements javax.servlet.http.HttpSessionListener to clean-up AtmosphereResource and Broadcaster resources when a user session ends

Your comments, questions, and suggestions are very welcome!
