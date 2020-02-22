package com.modzo.ors.web.web.radio.stations;

import com.modzo.ors.web.web.api.RadioStationResponse;
import com.modzo.ors.web.web.api.RadioStationsClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class RadioStationService {
    private final RadioStationsClient client;

    public RadioStationService(RadioStationsClient client) {
        this.client = client;
    }

    Data retrieve(Long id) {
        RadioStationResponse station = client.getStation(id);
        return new Data(
                station.getId(),
                station.getUniqueId(),
                station.getTitle(),
                station.getWebsite()
        );
    }

    public static class Data {

        private final long id;

        private final String uniqueId;

        private final String title;

        private final String website;

        public Data(long id, String uniqueId, String title, String website) {
            this.id = id;
            this.uniqueId = uniqueId;
            this.title = title;
            this.website = website;
        }

        public long getId() {
            return id;
        }

        public String getUniqueId() {
            return uniqueId;
        }

        public String getTitle() {
            return title;
        }

        public String getWebsite() {
            return website;
        }
    }
}
