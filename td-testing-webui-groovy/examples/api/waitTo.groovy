package api

import static com.twosigma.testing.webui.WebTestGroovyDsl.scenario
import static pages.Pages.*

scenario("""Synchronization point with dynamic UI.
It executes your matcher multiple time until it matches or time runs out.""") {
    search.submit(query: "search this")
    search.numberOfResults.waitTo == 2
}
