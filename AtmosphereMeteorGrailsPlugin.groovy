import grails.util.BuildSettingsHolder
import grails.util.Environment
import grails.util.Holders
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import javax.servlet.ServletContext
import javax.servlet.ServletRegistration

import org.grails.plugins.atmosphere_meteor.AtmosphereConfigurationHolder
import org.grails.plugins.atmosphere_meteor.MeteorHandlerArtefactHandler
import org.grails.plugins.atmosphere_meteor.MeteorServletArtefactHandler

class AtmosphereMeteorGrailsPlugin {
	def version = "0.8.4"
	def grailsVersion = "2.1 > *"
	def pluginExcludes = [
			"web-app/css/**",
			"web-app/images/**",
			"web-app/js/application.js"
	]

	def title = "Atmosphere Meteor Plugin"
	def author = "Ken Siprell"
	def authorEmail = "ken.siprell@gmail.com"

	def description = '''
This plugin incorporates the [Atmosphere Framework|https://github.com/Atmosphere/atmosphere], which includes client and server-side components for building asynchronous web applications.
'''

	def applicationContext	
	def artefacts = [MeteorHandlerArtefactHandler, MeteorServletArtefactHandler]
	def watchedResources = [
			"file:./grails-app/atmosphere/**/*MeteorHandler.groovy",
			"file:./grails-app/atmosphere/**/*MeteorServlet.groovy",
			"file:./grails-app/conf/AtmosphereMeteorConfig.groovy"
	]

	def onChange = { event ->
		// Change in AtmosphereMeteorConfig.groovy
		if (event.source.name == "AtmosphereMeteorConfig") {
			println "\nChanges to AtmosphereMeteorConfig.groovy will be implemented when the application is restarted.\n"
		}

		// Change in a MeteorHandler
		if (application.isArtefactOfType(MeteorHandlerArtefactHandler.TYPE, event.source.name)) {
			def oldClass = application.getMeteorHandlerClass(event.source.name)
			application.addArtefact(MeteorHandlerArtefactHandler.TYPE, event.source)
			application.meteorHandlerClasses.each {
				if (it.clazz != event.source && oldClass.clazz.isAssignableFrom(it.clazz)) {
					def newClass = application.classLoader.reloadClass(it.clazz.name)
					application.addArtefact(MeteorHandlerArtefactHandler.TYPE, newClass)
				}
			}
		}

		// Change in a MeteorServlet
		if (application.isArtefactOfType(MeteorServletArtefactHandler.TYPE, event.source.name)) {
			def oldClass = application.getMeteorServletClass(event.source.name)
			application.addArtefact(MeteorServletArtefactHandler.TYPE, event.source)
			application.meteorServletClasses.each {
				if (it.clazz != event.source && oldClass.clazz.isAssignableFrom(it.clazz)) {
					def newClass = application.classLoader.reloadClass(it.clazz.name)
					application.addArtefact(MeteorServletArtefactHandler.TYPE, newClass)
				}
			}
		}
	}

	def doWithDynamicMethods = { applicationContext ->
		def config = AtmosphereConfigurationHolder.atmosphereMeteorConfig
		def environment = Environment.current.name
		
		
		// Check for configuration errors
		if (environment == "development") {
			printConfigurationErrors()
		}

		// Configure servlets
		// dynamic registration not possible during integration tests
		// https://github.com/kensiprell/grails-atmosphere-meteor/issues/35
		/*
		def servletContext = applicationContext.servletContext
		def serverInfo = serverInfo()
		config?.servlets?.each { name, parameters ->
			ServletRegistration servletRegistration = servletContext.addServlet(name, parameters.className)
			servletRegistration.addMapping(parameters.mapping)
			servletRegistration.setAsyncSupported(Boolean.TRUE)
			servletRegistration.setLoadOnStartup(1)
			if (environment == "test") {
				if (serverInfo.serverName == "jetty") {
					servletRegistration.setInitParameter("org.atmosphere.cpr.asyncSupport", "org.atmosphere.container.JettyServlet30AsyncSupportWithWebSocket")
				}
				if (serverInfo.serverName == "tomcat") {
					servletRegistration.setInitParameter("org.atmosphere.cpr.asyncSupport", "org.atmosphere.container.Tomcat7Servlet30SupportWithWebSocket")
				}
			}
			def initParams = parameters.initParams
			if (initParams != "none") {
				initParams?.each { param, value ->
					servletRegistration.setInitParameter(param, value)
				}
			}
		}
		*/
	}

	def doWithSpring = {
		// Register AtmosphereConfigurationHolder bean
		applicationContextHolder(AtmosphereConfigurationHolder) { bean ->
			bean.factoryMethod = "getInstance"
		}
	}

	def doWithWebDescriptor = { webXml ->
		def config = AtmosphereConfigurationHolder.atmosphereMeteorConfig
		//def environment = Environment.current.name
		//def serverInfo = serverInfo()
		//def servletContainerName = System.getProperty("atmosphereMeteorServletContainerName")
		
		if (config) {
			config.servlets.each { name, parameters ->
				log.debug "doWithWebDescriptor: $name -> $parameters"
				def initParams = parameters.initParams
				
				appendToWebDescriptor(webXml, "servlet", {
					servlet {
						"servlet-name"(name)
						"servlet-class"(parameters.className)
						"async-supported"("true")
						"load-on-startup"(1)
						if (initParams != "none") {
							initParams?.each { param, value ->
								"init-param" {
									"param-name"(param)
									"param-value"(value)
								}
							}
						}
/*						if (environment == "test") {
							if (servletContainerName == "jetty") {
								"init-param" {
									"param-name"("org.atmosphere.cpr.asyncSupport")
									"param-value"("org.atmosphere.container.JettyServlet30AsyncSupportWithWebSocket")
								}
							}
							if (servletContainerName == "tomcat") {
								"init-param" {
									"param-name"("org.atmosphere.cpr.asyncSupport")
									"param-value"("org.atmosphere.container.Tomcat7Servlet30SupportWithWebSocket")
								}
							}
						}	
*/
					}
				})
				appendToWebDescriptor(webXml, "servlet-mapping", ["servlet-name": name, "url-pattern": parameters.mapping])
			}
		} else {
			log.error("AtmosphereConfigurationHolder.atmosphereMeteorConfig: config not found.")
		}
	}

	protected static lastChild(def node, def tag) {
		def children = node[tag]
		children[children.size() - 1]
	}

	protected static appendToWebDescriptor(def node, def tag, Closure append) {
		lastChild(node, tag) + append
	}

    protected static appendToWebDescriptor(def node, def tag, Map append) {
		lastChild(node, tag) + {
			"$tag" {
				append.each { k, v ->
					"$k"(v)
				}  
			}
		}
	}

	protected static serverInfo() {
		def servletContext = Holders.servletContext
		if (!servletContext) return
		def apiVersion = servletContext.effectiveMajorVersion
		List serverInfo = servletContext.serverInfo.tokenize("/")
		def serverName = serverInfo[0]
		def serverVersion = serverInfo[1]
		if (serverName.contains("jetty")) {
			serverName = "jetty"
		}
		if (serverName.contains("Tomcat")) {
			serverName = "tomcat"
		}
		[
			apiVersion   : apiVersion as Integer,
			serverName   : serverName,
			serverVersion: serverVersion
		]
	}

	protected static printConfigurationErrors() {
		def serverInfo = serverInfo()
		Log log = LogFactory.getLog(this)
		def settings = BuildSettingsHolder.settings
		def tomcatNio = settings?.config?.grails?.tomcat?.nio
		def tomcatErrors = false
		def tomcatErrorNio = ""
		def tomcatErrorApi = ""

		// Tomcat errors
		if (serverInfo.serverName == "tomcat" && serverInfo.serverVersion.startsWith("7")) {
			if (tomcatNio != true) {
				log.error("The atmosphere-meteor plugin requires in your BuildConfig.groovy: grails.tomcat.nio = true")
				tomcatErrors = true
				tomcatErrorNio = """
* grails.tomcat.nio = true					 *"""
			}
			if (serverInfo.apiVersion < 3) {
				log.error("The atmosphere-meteor plugin requires in your BuildConfig.groovy:  grails.servlet.version = '3.0'")
				tomcatErrors = true
				tomcatErrorApi = """
* grails.servlet.version = "3.0"				   *"""
			}
			if (tomcatErrors) {
				println """
********************************************************************
* The atmosphere-meteor plugin requires the following settings in  *
* your grails-app/conf/BuildConfig.groovy:			 *$tomcatErrorNio$tomcatErrorApi
********************************************************************
"""
			}
		}

		// Jetty errors
		if (serverInfo.serverName == "jetty") {
			def jettyVersion = serverInfo.serverVersion.getAt(0) as Integer
			if (jettyVersion < 8) {
				def versionLine = "* It appears you are using version $jettyVersion.".padRight(67, " ")
				println """
********************************************************************
* The atmosphere-meteor plugin requires at least Jetty version 8.  *
$versionLine*
* Jetty documentation:					     *
* https://github.com/kensiprell/grails-atmosphere-meteor#jetty     *
********************************************************************
"""
			}
		}
	}
}
