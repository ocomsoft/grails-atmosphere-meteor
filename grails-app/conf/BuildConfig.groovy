grails.servlet.version = "3.0"
grails.tomcat.nio = true
grails.project.work.dir = "target"
grails.project.dependency.resolver = "maven"

grails.project.dependency.distribution = {
	remoteRepository(id: "localPluginReleases", url: "http://localhost:8081/artifactory/plugins-release-local/")
	remoteRepository(id: "localPluginSnapshots", url: "http://localhost:8081/artifactory/plugins-snapshot-local/")
}

grails.project.dependency.resolution = {
	inherits "global"
	log "warn"
	//legacyResolve true

	repositories {
		grailsCentral()
		mavenLocal()
		mavenCentral()
	}

	dependencies {
		// TODO update versions here and in README.md
		compile "org.atmosphere:atmosphere-runtime-native:2.0.3", {
			excludes "slf4j-api"
		}
		compile "org.codehaus.jackson:jackson-core-asl:1.9.13"
	}

	plugins {
		build ":release:3.0.1", ":rest-client-builder:2.0.0", ":tomcat:7.0.42", {
			export = false
		}
s	}
}
