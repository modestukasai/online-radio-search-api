package com.modzo.ors.search

import com.modzo.ors.search.domain.RadioStationDocument
import com.modzo.ors.search.domain.RadioStationsRepository
import com.modzo.ors.stations.resources.IntegrationSpec
import org.springframework.beans.factory.annotation.Autowired
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils

class RadioStationsRepositorySpec extends IntegrationSpec {
    @Autowired
    RadioStationsRepository repository

    void 'should create radio station document in repository'() {
        when:
            RadioStationDocument document = repository.save(
                    new RadioStationDocument(
                            RandomStringUtils.randomNumeric(10) as long,
                            UUID.randomUUID(),
                            RandomStringUtils.randomAlphanumeric(10),
                            true
                    )
            )
        then:
            repository.findByUniqueId(document.uniqueId).get().title == document.title
    }
}
