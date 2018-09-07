package de.az.demo.mn.opa;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import javax.inject.Inject;
import java.net.URI;

import static io.micronaut.http.MediaType.TEXT_PLAIN;

/**
 * Demo controller for the OPA / Micronaut demo.
 */
@Controller(value = "/", produces = TEXT_PLAIN)
public class DemoController {

    private final ChuckNorrisFacts chuckNorrisFacts;

    /**
     * Constructor.
     *
     * @param chuckNorrisFacts Chuck Norris Facts provider
     */
    @Inject
    public DemoController(ChuckNorrisFacts chuckNorrisFacts) {
        this.chuckNorrisFacts = chuckNorrisFacts;
    }

    /**
     * Index page.
     *
     * @return redirects to the free chuck norris facts
     */
    @Get
    public HttpResponse<String> index() {
        return HttpResponse.permanentRedirect(URI.create("/free"));
    }

    /**
     * Free Chuck Norris facts.
     *
     * @return fact as String
     */
    @Get("/free")
    public String free() {
        return chuckNorrisFacts.getRandom();
    }

    /**
     * Protected Chuck Norris facts.
     *
     * @return fact with Chuck Norris real name as String
     */
    @Get("/protected")
    public String protectedd() {
        return chuckNorrisFacts.getRandom().replace("Chuck Norris", "Carlos Ray Norris");
    }

}