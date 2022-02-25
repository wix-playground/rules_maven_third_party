package com.wix.build.sync.e2e

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{equalTo => equalToString, _}
import com.github.tomakehurst.wiremock.stubbing.Scenario
import com.wix.build.maven.Coordinates
import com.wix.build.maven.MavenMakers.someCoordinates
import com.wix.build.sync.ArtifactoryRemoteStorage.CoordinatesConverters
import com.wix.build.sync.e2e.ArtifactoryTestSupport.{artifactoryToken, toRelativeArtifactoryApiPath, toRelativeArtifactoryDownloadPath}

//noinspection TypeAnnotation
object WireMockTestSupport {
  val wireMockPort = 11876
  val wireMockServer = new WireMockServer(wireMockPort)

  def givenArtifactoryReturnsBinaryJarOf(forArtifact: Coordinates, jar: Array[Byte]) = {
    val relativeArtifactoryDownloadPath = toRelativeArtifactoryDownloadPath(forArtifact)

    wireMockServer.givenThat(get(urlEqualTo(relativeArtifactoryDownloadPath))
      .willReturn(aResponse().withStatus(200)
        .withBody(jar)))

  }

  def givenArtifactoryAllowsSettingSha256(forArtifact: Coordinates) = {
    val artifactPath = forArtifact.toArtifactPath

    wireMockServer.givenThat(post(urlEqualTo("/artifactory/api/checksum/sha256"))
      .inScenario("sha256 is missing").whenScenarioStateIs(Scenario.STARTED)
      .withHeader("Content-Type", equalToString("application/json"))
      .withHeader("X-JFrog-Art-Api", equalToString(artifactoryToken))
      .withRequestBody(equalToJson(
        s"""|{
            |   "repoKey":"repo1-cache",
            |   "path":"$artifactPath"
            |}
        """.stripMargin))
      .willReturn(aResponse().withStatus(200))
      .willSetStateTo("sha256 is set"))
  }

  def givenArtifactoryReturnsArtifactThatIsMissingSha256(forArtifact: Coordinates) = {
    val relativeArtifactoryPath = toRelativeArtifactoryApiPath(forArtifact)
    val artifactPath = forArtifact.toArtifactPath
    val relativeArtifactoryDownloadPath = toRelativeArtifactoryDownloadPath(forArtifact)

    wireMockServer.givenThat(get(urlEqualTo(relativeArtifactoryPath))
      .inScenario("sha256 is missing").whenScenarioStateIs(Scenario.STARTED)
      .willReturn(aResponse().withStatus(200)
        .withHeader("Content-Type", "application/vnd.org.jfrog.artifactory.storage.FileInfo+json")
        .withHeader("Server", "Artifactory/5.11.1")
        .withBody(
          s"""
             |{
             |    "repo": "repo1-cache",
             |    "path": "/$artifactPath",
             |    "created": "2016-12-13T09:27:43.961Z",
             |    "createdBy": "anonymous",
             |    "lastModified": "2010-10-04T11:49:50.000Z",
             |    "modifiedBy": "anonymous",
             |    "lastUpdated": "2016-12-13T09:27:43.992Z",
             |    "downloadUri": "https://repo.example.com$relativeArtifactoryDownloadPath",
             |    "remoteUrl": "https://repo1.maven.org/maven2/$artifactPath",
             |    "mimeType": "application/java-archive",
             |    "size": "48920",
             |    "checksums": {
             |        "sha1": "98f886f59bb0e69f8e86cdc082e69f2f4c13d648",
             |        "md5": "1d67a37a5822b12abc55e5133e47ca0e"
             |    },
             |    "originalChecksums": {
             |        "sha1": "98f886f59bb0e69f8e86cdc082e69f2f4c13d648",
             |        "md5": "1d67a37a5822b12abc55e5133e47ca0e"
             |    },
             |    "uri": "https://repo.example.com$relativeArtifactoryPath"
             |}
             |""".stripMargin)))
  }

  def givenArtifactoryReturnsSha256(sha256Checksum: String, forArtifact: Coordinates,
                                    inScenario: String = "sha256 is already set",
                                    stateIs: String = Scenario.STARTED) = {
    val relativeArtifactoryPath = toRelativeArtifactoryApiPath(forArtifact)
    val artifactPath = forArtifact.toArtifactPath
    val relativeArtifactoryDownloadPath = toRelativeArtifactoryDownloadPath(forArtifact)

    wireMockServer.givenThat(get(urlEqualTo(relativeArtifactoryPath))
      .inScenario(inScenario).whenScenarioStateIs(stateIs)
      .willReturn(aResponse().withStatus(200)
        .withHeader("Content-Type", "application/vnd.org.jfrog.artifactory.storage.FileInfo+json")
        .withHeader("Server", "Artifactory/5.11.1")
        .withBody(
          s"""
             |{
             |    "repo": "repo1-cache",
             |    "path": "/$artifactPath",
             |    "created": "2018-07-09T05:50:45.383Z",
             |    "createdBy": "anonymous",
             |    "lastModified": "2018-07-06T15:56:53.000Z",
             |    "modifiedBy": "anonymous",
             |    "lastUpdated": "2018-07-09T05:50:45.394Z",
             |    "downloadUri": "https://repo.example.com$relativeArtifactoryDownloadPath",
             |    "remoteUrl": "https://repo1.maven.org/maven2/$artifactPath",
             |    "mimeType": "application/java-archive",
             |    "size": "49738",
             |    "checksums": {
             |        "sha1": "0cab752d2b772c02b0bb165acf7bebc432f17a85",
             |        "md5": "64e742109d98181ca06dd8b51d412a33",
             |        "sha256": "$sha256Checksum"
             |    },
             |    "originalChecksums": {
             |        "sha1": "0cab752d2b772c02b0bb165acf7bebc432f17a85",
             |        "md5": "64e742109d98181ca06dd8b51d412a33",
             |        "sha256": "$sha256Checksum"
             |    },
             |    "uri": "https://repo.example.com$relativeArtifactoryPath"
             |}
             |""".stripMargin)))
  }

  // TODO: refactor so will return what was requested...
  def alwaysReturnSha256Checksums(sha256Checksum: String = "somechecksum", forArtifact: Coordinates = someCoordinates("someArtifact"),
                                    inScenario: String = "sha256 is already set",
                                    stateIs: String = Scenario.STARTED) = {
    val relativeArtifactoryPath = toRelativeArtifactoryApiPath(forArtifact)
    val artifactPath = forArtifact.toArtifactPath
    val relativeArtifactoryDownloadPath = toRelativeArtifactoryDownloadPath(forArtifact)

    wireMockServer.givenThat(get(anyUrl())
      .inScenario(inScenario).whenScenarioStateIs(stateIs)
      .willReturn(aResponse().withStatus(200)
        .withHeader("Content-Type", "application/vnd.org.jfrog.artifactory.storage.FileInfo+json")
        .withHeader("Server", "Artifactory/5.11.1")
        .withBody(
          s"""
             |{
             |     {{request.path.[2]}}
             |    "repo": "repo1-cache",
             |    "path": "/$artifactPath",
             |    "created": "2018-07-09T05:50:45.383Z",
             |    "createdBy": "anonymous",
             |    "lastModified": "2018-07-06T15:56:53.000Z",
             |    "modifiedBy": "anonymous",
             |    "lastUpdated": "2018-07-09T05:50:45.394Z",
             |    "downloadUri": "https://repo.example.com$relativeArtifactoryDownloadPath",
             |    "remoteUrl": "https://repo1.maven.org/maven2/$artifactPath",
             |    "mimeType": "application/java-archive",
             |    "size": "49738",
             |    "checksums": {
             |        "sha1": "0cab752d2b772c02b0bb165acf7bebc432f17a85",
             |        "md5": "64e742109d98181ca06dd8b51d412a33",
             |        "sha256": "$sha256Checksum"
             |    },
             |    "originalChecksums": {
             |        "sha1": "0cab752d2b772c02b0bb165acf7bebc432f17a85",
             |        "md5": "64e742109d98181ca06dd8b51d412a33",
             |        "sha256": "$sha256Checksum"
             |    },
             |    "uri": "https://repo.example.com$relativeArtifactoryPath"
             |}
             |""".stripMargin)))
  }

  def givenMetaArtifactNotFoundInArtifactoryRestApi(forArtifact: Coordinates): Any = {
    val relativeArtifactoryPath = toRelativeArtifactoryApiPath(forArtifact)

    givenItemNotFoundFor(relativeArtifactoryPath)
  }

  private def givenItemNotFoundFor(relativeArtifactoryPath: String) = {
    wireMockServer.givenThat(get(urlEqualTo(relativeArtifactoryPath))
      .willReturn(aResponse().withStatus(404)
        .withHeader("Content-Type", "application/json")
        .withHeader("Server", "Artifactory/5.11.1")
        .withBody(
          s"""
             |{
             |    "errors": [
             |        {
             |            "status": 404,
             |            "message": "Unable to find item"
             |        }
             |    ]
             |}
             |""".stripMargin)))
  }

  def givenJarArtifactNotFoundInArtifactory(forArtifact: Coordinates): Any = {
    val relativeArtifactoryPath = toRelativeArtifactoryDownloadPath(forArtifact)

    givenItemNotFoundFor(relativeArtifactoryPath)
  }
}

//noinspection TypeAnnotation
object ArtifactoryTestSupport {
  val artifactoryToken = "EXPECTED_TOKEN"

  def toRelativeArtifactoryApiPath(forArtifact: Coordinates, repo: String = "repo1-cache") = {
    val artifactPath = forArtifact.toArtifactPath
    s"/artifactory/api/storage/$repo/$artifactPath"
  }

  def toRelativeArtifactoryDownloadPath(forArtifact: Coordinates, repo: String = "libs-snapshots") = {
    val artifactPath = forArtifact.toArtifactPath
    s"/artifactory/$repo/" + artifactPath
  }
}