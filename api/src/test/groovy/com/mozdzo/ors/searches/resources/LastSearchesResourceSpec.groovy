package com.mozdzo.ors.searches.resources

import com.mozdzo.ors.HttpEntityBuilder
import com.mozdzo.ors.resources.IntegrationSpec
import com.mozdzo.ors.searches.TestSearchQuery
import com.mozdzo.ors.searches.domain.SearchedQuery
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity

import static java.time.ZoneId.systemDefault
import static org.springframework.hateoas.Link.REL_SELF
import static org.springframework.http.HttpMethod.GET
import static org.springframework.http.HttpStatus.OK

class LastSearchesResourceSpec extends IntegrationSpec {

    @Autowired
    TestSearchQuery testSearchQuery

    void 'should not fail to retrieve last searches, when none exist'() {
        given:
            String url = '/last-searches'
        when:
            ResponseEntity<LastSearchesResource> result = restTemplate.exchange(
                    "${url}?size=100&page=0",
                    GET,
                    HttpEntityBuilder.builder().build(),
                    LastSearchesResource
            )
        then:
            result.statusCode == OK
        and:
            with(result.body as LastSearchesResource) {
                it.content != null

                links.first().rel == REL_SELF
                links.first().href.endsWith(url)
            }
    }

    void 'anyone should retrieve last search'() {
        given:
            SearchedQuery searchedQuery = testSearchQuery.create()
        and:
            String url = '/last-searches'
        when:
            ResponseEntity<LastSearchesResource> result = restTemplate.exchange(
                    "${url}?size=100&page=0",
                    GET,
                    HttpEntityBuilder.builder().build(),
                    LastSearchesResource
            )
        then:
            result.statusCode == OK
        and:
            with(result.body as LastSearchesResource) {
                LastSearchResponse response = it.content
                        .find { it.lastSearch.id == searchedQuery.id }.lastSearch

                response
                response.id == searchedQuery.id
                response.query == searchedQuery.query
                response.date.withZoneSameInstant(systemDefault()) == searchedQuery.date

                links.first().rel == REL_SELF
                links.first().href.endsWith(url)
            }
    }
}