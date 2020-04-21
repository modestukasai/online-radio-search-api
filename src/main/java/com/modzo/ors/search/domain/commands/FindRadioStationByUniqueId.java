package com.modzo.ors.search.domain.commands;

import com.modzo.ors.search.domain.RadioStationDocument;
import com.modzo.ors.search.domain.RadioStationsRepository;
import com.modzo.ors.search.domain.ReadModelException;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

public class FindRadioStationByUniqueId {

    private final String radioStationUniqueId;

    public FindRadioStationByUniqueId(String radioStationUniqueId) {
        this.radioStationUniqueId = radioStationUniqueId;
    }

    public String getRadioStationUniqueId() {
        return radioStationUniqueId;
    }

    @Component
    public static class Handler {
        private final RadioStationsRepository radioStationsRepository;

        public Handler(RadioStationsRepository radioStationsRepository) {
            this.radioStationsRepository = radioStationsRepository;
        }

        public RadioStationDocument handle(FindRadioStationByUniqueId command) {
            return radioStationsRepository
                    .findByUniqueId(command.radioStationUniqueId)
                    .orElseThrow(() -> new ReadModelException(
                            "RADIO_STATION_BY_UNIQUE_ID_NOT_FOUND",
                            format("Radio station by unique id `%s` was not found",
                                    command.radioStationUniqueId)
                    ));
        }
    }
}
