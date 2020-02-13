package com.modzo.ors.search.domain;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Optional;

public interface RadioStationsRepository extends ElasticsearchRepository<RadioStationDocument, String> {

    Optional<RadioStationDocument> findByUniqueId(String uniqueId);

    void deleteByUniqueId(String uniqueId);

}
