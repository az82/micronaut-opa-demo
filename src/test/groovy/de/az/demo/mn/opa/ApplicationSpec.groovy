package de.az.demo.mn.opa

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.server.EmbeddedServer
import org.slf4j.LoggerFactory
import spock.lang.AutoCleanup
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpStatus.OK
import static io.micronaut.http.HttpStatus.UNAUTHORIZED
import static io.micronaut.http.MediaType.APPLICATION_JSON

/**
 * Specification for {@link Application}.
 */
class ApplicationSpec extends Specification {

    static final LOGGER = LoggerFactory.getLogger(ApplicationSpec)

    static
    final TOKEN = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJtaWNyb25hdXQtb3BhLWRlbW8iLCJuYW1lIjoiTWljcm9uYXV0IE9QQSBEZW1vIiwiaWF0IjoxNTE2MjM5MDIyfQ.2sOzCwb9777B4yAP-nU5PQPFIjulRJxS9nKDNgHOvqA'

    static WireMockServer opaMock

    @AutoCleanup
    static EmbeddedServer embeddedServer

    @AutoCleanup
    static RxHttpClient client

    void setupSpec() {
        def opaPort = getenv('demo.opa.port')
        def opaHost = getenv('demo.opa.host')

        // Only start the OPA mock if no host / port for OPA has been pre-set
        if (opaPort == null && opaHost == null) {
            LOGGER.info("Using an OPA Mock on a dynamically assigned port")
            opaMock = createOpaMock(options().dynamicPort())
            //noinspection GroovyAssignabilityCheck
            embeddedServer = ApplicationContext.run(EmbeddedServer, ['demo.opa.port': opaMock.port().toString()])
        } else {
            LOGGER.info("Using an OPA instance at {}:{}", opaHost ?: 'localhost', opaPort ?: '8181')
            embeddedServer = ApplicationContext.run(EmbeddedServer)
        }

        client = embeddedServer.applicationContext.createBean(RxHttpClient, embeddedServer.getURL())
    }

    void cleanupSpec() {
        if (opaMock != null) opaMock.stop()
    }

    def 'health endpoint'() {
        expect:
        client.toBlocking()
                .retrieve(GET('/health'), Map)
                .status == 'UP'
    }

    def 'free facts work'() {
        when:
        HttpResponse response = client.toBlocking().exchange('/free', String)

        then:
        response.status == OK
        response.getBody(String).get() =~ /Chuck Norris/
    }

    def 'OPA can block access to protected facts'() {
        when:
        client.toBlocking().exchange('/protected')

        then:
        HttpClientResponseException ex = thrown()
        ex.status == UNAUTHORIZED
    }

    def 'OPA can allow access to protected facts'() {
        when:
        HttpResponse response = client.toBlocking().exchange(
                GET('/protected').header('Authorization', "Bearer $TOKEN"),
                String)

        then:
        response.status == OK
        response.getBody(String).get() =~ /Carlos Ray Norris/
    }

    def 'index redirects to free facts'() {
        when:
        HttpResponse response = client.toBlocking().exchange('/', String)

        then:
        response.status == OK
        response.getBody(String).get() =~ /Chuck Norris/

    }

    static createOpaMock(WireMockConfiguration config) {
        WireMockServer mock = new WireMockServer(config)

        mock.stubFor(
                post('/v1/data/mn/demo/allow').atPriority(99)
                        .willReturn(
                        aResponse()
                                .withHeader('Content-Type', APPLICATION_JSON.toString())
                                .withBody('{"result":"false"}')))
        mock.stubFor(
                post('/v1/data/mn/demo/allow').atPriority(1)
                        .withRequestBody(matchingJsonPath('$.input.headers.Authorization[0]', equalTo("Bearer $TOKEN")))
                        .willReturn(
                        aResponse()
                                .withHeader('Content-Type', APPLICATION_JSON.toString())
                                .withBody('{"result":"true"}')))
        mock.start()

        return mock
    }

    def getenv(String name) {
        return System.getProperty(name, System.getenv(name.toUpperCase().replace('.', '_')))
    }

}
