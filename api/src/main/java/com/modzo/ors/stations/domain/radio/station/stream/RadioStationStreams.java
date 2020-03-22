package com.modzo.ors.stations.domain.radio.station.stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface RadioStationStreams extends CrudRepository<RadioStationStream, Long> {

    Optional<RadioStationStream> findByIdAndRadioStation_Id(long streamId, long radioStationId);

    Optional<RadioStationStream> findByRadioStation_IdAndUrl(long radioStationId, String url);

    Page<RadioStationStream> findAllByRadioStation_Id(long radioStationId, Pageable pageable);

    Optional<RadioStationStream> findOneBySongsCheckedIsBeforeOrSongsCheckedIsNullOrderBySongsCheckedAsc(
            ZonedDateTime songCheckedBefore
    );

    Optional<RadioStationStream> findOneByInfoCheckedIsBeforeOrInfoCheckedIsNullOrderByInfoCheckedAsc(
            ZonedDateTime songCheckedBefore
    );
}
