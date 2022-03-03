load("//rules:maven_specs.bzl", "wix")

SCALA_VERSION = "2.12"

TEST_DEPS = [
    wix.artifact(group = "org.specs2", artifact = "specs2-common_" + SCALA_VERSION, version = "4.8.3"),
    wix.artifact(group = "org.specs2", artifact = "specs2-core_" + SCALA_VERSION, version = "4.8.3"),
    wix.artifact(group = "org.specs2", artifact = "specs2-fp_" + SCALA_VERSION, version = "4.8.3"),
    wix.artifact(group = "org.specs2", artifact = "specs2-junit_" + SCALA_VERSION, version = "4.8.3"),
    wix.artifact(group = "org.specs2", artifact = "specs2-matcher_" + SCALA_VERSION, version = "4.8.3"),
    wix.artifact(group = "org.specs2", artifact = "specs2-matcher-extra_" + SCALA_VERSION, version = "4.8.3"),
    wix.artifact(group = "org.specs2", artifact = "specs2-mock_" + SCALA_VERSION, version = "4.8.3"),
    wix.artifact(group = "com.wix", artifact = "http-testkit_" + SCALA_VERSION, version = "0.1.25"),
    wix.artifact(group = "com.wix", artifact = "http-testkit-marshaller-jackson_" + SCALA_VERSION, version = "0.1.25"),
    wix.artifact(group = "junit", artifact = "junit", version = "4.13.2"),
    wix.artifact(group = "org.hamcrest", artifact = "hamcrest", version = "2.2"),
]

TOOL_DEPS = [
    wix.artifact(group = "org.slf4j", artifact = "jcl-over-slf4j", version = "1.7.25"),
    wix.artifact(group = "org.slf4j", artifact = "slf4j-api", version = "1.7.25"),
    wix.artifact(group = "org.slf4j", artifact = "slf4j-simple", version = "1.7.25"),
    wix.artifact(group = "org.slf4j", artifact = "slf4j-jdk14", version = "1.7.25"),
    wix.artifact(group = "ch.qos.logback", artifact = "logback-classic", version = "1.1.11"),
    wix.artifact(group = "com.fasterxml.jackson.core", artifact = "jackson-annotations", version = "2.9.9"),
    wix.artifact(group = "com.fasterxml.jackson.core", artifact = "jackson-databind", version = "2.9.9"),
    wix.artifact(group = "com.fasterxml.jackson.module", artifact = "jackson-module-scala_" + SCALA_VERSION, version = "2.9.9"),
    wix.artifact(group = "org.codehaus.plexus", artifact = "plexus-interpolation", version = "1.11"),
    wix.artifact(group = "org.codehaus.mojo", artifact = "mrm-servlet", version = "1.1.0", exclusions = [wix.exclusion(artifact = "servlet-api", group = "org.mortbay.jetty")]),
    wix.artifact(group = "com.github.pathikrit", artifact = "better-files_" + SCALA_VERSION, version = "3.8.0"),
    wix.artifact(group = "io.get-coursier", artifact = "coursier_" + SCALA_VERSION, version = "2.0.13"),
    wix.artifact(group = "org.apache.maven.resolver", artifact = "maven-resolver-api", version = "1.7.3"),
    wix.artifact(group = "org.apache.maven.resolver", artifact = "maven-resolver-util", version = "1.7.3"),
    wix.artifact(group = "org.apache.maven.resolver", artifact = "maven-resolver-impl", version = "1.7.3"),
    wix.artifact(group = "org.apache.maven.resolver", artifact = "maven-resolver-spi", version = "1.7.3"),
    wix.artifact(group = "org.apache.maven.resolver", artifact = "maven-resolver-transport-http", version = "1.7.3"),
    wix.artifact(group = "org.apache.maven.resolver", artifact = "maven-resolver-transport-file", version = "1.7.3"),
    wix.artifact(group = "org.apache.maven.resolver", artifact = "maven-resolver-connector-basic", version = "1.7.3"),
    wix.artifact(group = "org.apache.maven", artifact = "maven-artifact", version = "3.5.4"),
    wix.artifact(group = "org.apache.maven", artifact = "maven-model", version = "3.5.4"),
    wix.artifact(group = "org.apache.maven", artifact = "maven-model-builder", version = "3.5.4"),
    wix.artifact(group = "org.apache.maven", artifact = "maven-resolver-provider", version = "3.5.4"),
    wix.artifact(group = "commons-codec", artifact = "commons-codec", version = "1.11"),
    wix.artifact(group = "org.codehaus.mojo", artifact = "mrm-api", version = "1.1.0"),
    wix.artifact(group = "org.codehaus.mojo", artifact = "mrm-maven-plugin", version = "1.1.0", exclusions = [wix.exclusion(artifact = "servlet-api", group = "org.mortbay.jetty")]),
    wix.artifact(group = "org.codehaus.mojo", artifact = "mrm-servlet", version = "1.1.0", exclusions = [wix.exclusion(artifact = "servlet-api", group = "org.mortbay.jetty")]),
    wix.artifact(group = "com.github.scopt", artifact = "scopt_" + SCALA_VERSION, version = "3.7.1"),
    wix.artifact(group = "org.scala-lang", artifact = "scala-library", version = "2.12.13"),
    wix.artifact(group = "com.github.tomakehurst", artifact = "wiremock-jre8", version = "2.32.0"),
    wix.artifact(group = "org.eclipse.jgit", artifact = "org.eclipse.jgit", version = "5.11.0.202103091610-r"),
    wix.artifact(group = "org.scalaj", artifact = "scalaj-http_" + SCALA_VERSION, version = "2.4.2"),
    wix.artifact(group = "org.scala-lang.modules", artifact = "scala-xml_" + SCALA_VERSION, version = "2.0.1"),
    wix.artifact(group = "org.scala-lang.modules", artifact = "scala-parser-combinators_" + SCALA_VERSION, version = "1.1.2"),
    wix.artifact(group = "org.scala-lang", artifact = "scala-compiler", version = "2.12.13"),
    wix.artifact(group = "org.scala-lang", artifact = "scala-reflect", version = "2.12.13"),
]

MANAGED_DEPS = TOOL_DEPS + TEST_DEPS
