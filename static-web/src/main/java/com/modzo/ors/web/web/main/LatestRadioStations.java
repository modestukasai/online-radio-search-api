package com.modzo.ors.web.web.main;

import com.modzo.ors.web.web.ApplicationProperties;
import com.modzo.ors.web.web.api.RadioStationResponse;
import com.modzo.ors.web.web.api.RadioStationsClient;
import com.modzo.ors.web.web.api.RestPageImpl;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
class LatestRadioStations {

    private final ApplicationProperties properties;

    private final RadioStationsClient client;

    LatestRadioStations(ApplicationProperties properties,
                        RadioStationsClient client) {
        this.properties = properties;
        this.client = client;
    }

    List<Data> retrieve() {
        RestPageImpl<RadioStationResponse> stations = client.getStations();
        return stations.getContent()
                .stream()
                .map(response -> new Data(
                        response.getId(),
                        response.getUniqueId(),
                        response.getTitle(),
                        response.getWebsite()
                ))
                .collect(Collectors.toList());
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