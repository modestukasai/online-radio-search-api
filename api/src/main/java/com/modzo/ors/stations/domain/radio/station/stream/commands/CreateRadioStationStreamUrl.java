package com.modzo.ors.stations.domain.radio.station.stream.commands;

import com.modzo.ors.commons.Urls;
import com.modzo.ors.events.domain.RadioStationStreamUrlCreated;
import com.modzo.ors.stations.domain.DomainException;
import com.modzo.ors.stations.domain.radio.station.RadioStations;
import com.modzo.ors.stations.domain.radio.station.stream.RadioStationStreams;
import com.modzo.ors.stations.domain.radio.station.stream.StreamUrl;
import com.modzo.ors.stations.domain.radio.station.stream.StreamUrls;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.isNull;

public class CreateRadioStationStreamUrl {

    private final long radioStationId;

    private final long streamId;

    private final StreamUrl.Type type;

    private final String url;

    public CreateRadioStationStreamUrl(
            long radioStationId,
            long streamId,
            StreamUrl.Type type,
            String url) {
        this.radioStationId = radioStationId;
        this.streamId = streamId;
        this.type = type;
        this.url = url;
    }

    public long getRadioStationId() {
        return radioStationId;
    }

    public long getStreamId() {
        return streamId;
    }

    public StreamUrl.Type getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    @Component
    public static class Handler {

        private final RadioStationStreams radioStationStreams;

        private final Validator validator;

        private final StreamUrls streamUrls;

        private final ApplicationEventPublisher applicationEventPublisher;

        public Handler(RadioStationStreams radioStationStreams,
                       Validator validator,
                       StreamUrls streamUrls,
                       ApplicationEventPublisher applicationEventPublisher) {
            this.radioStationStreams = radioStationStreams;
            this.validator = validator;
            this.streamUrls = streamUrls;
            this.applicationEventPublisher = applicationEventPublisher;
        }

        @Transactional
        public CreateRadioStationStreamUrl.Result handle(CreateRadioStationStreamUrl command) {
            validator.validate(command);

            var radioStationStream = radioStationStreams.findByIdAndRadioStation_Id(
                    command.radioStationId,
                    command.streamId
            ).get();

            var streamUrl = new StreamUrl(command.type, command.url);
            streamUrl.setStream(radioStationStream);

            StreamUrl savedUrl = streamUrls.save(streamUrl);

            radioStationStream.getUrls().put(command.type, savedUrl);

            applicationEventPublisher.publishEvent(
                    new RadioStationStreamUrlCreated(
                            savedUrl,
                            new RadioStationStreamUrlCreated.Data(
                                    savedUrl.getId(),
                                    savedUrl.getUniqueId(),
                                    savedUrl.getCreated(),
                                    savedUrl.getStream().getId(),
                                    savedUrl.getStream().getUniqueId(),
                                    savedUrl.getUrl(),
                                    savedUrl.getType()
                            )
                    )
            );
            return new CreateRadioStationStreamUrl.Result(savedUrl.getId());
        }
    }


    @Component
    private static class Validator {

        private final RadioStations radioStations;

        private final RadioStationStreams radioStationStreams;

        public Validator(RadioStations radioStations, RadioStationStreams radioStationStreams) {
            this.radioStations = radioStations;
            this.radioStationStreams = radioStationStreams;
        }

        void validate(CreateRadioStationStreamUrl command) {

            if (isNull(command.type)) {
                throw new DomainException(
                        "FIELD_TYPE_CANNOT_BE_NULL",
                        String.format(
                                "Radio station stream with id = `%s` for radio station with id = `%s` "
                                        + "type cannot be null",
                                command.streamId,
                                command.radioStationId
                        )
                );
            }

            if (Urls.isNotValid(command.url)) {
                throw new DomainException(
                        "FIELD_URL_IS_NOT_VALID",
                        String.format(
                                "Radio station stream with id = `%s` for radio station with id = `%s` url is not valid",
                                command.streamId,
                                command.radioStationId
                        )
                );
            }

            radioStations.findById(command.radioStationId)
                    .orElseThrow(() -> radioStationWithIdDoesNotExist(command));

            radioStationStreams.findById(command.streamId)
                    .orElseThrow(() -> radioStationStreamWithIdDoesNotExist(command));
        }

        private DomainException radioStationWithIdDoesNotExist(CreateRadioStationStreamUrl command) {
            return new DomainException(
                    "RADIO_STATION_WITH_ID_DOES_NOT_EXIST",
                    String.format("Radio station with id = `%s` does not exist", command.radioStationId)
            );
        }

        private DomainException radioStationStreamWithIdDoesNotExist(CreateRadioStationStreamUrl command) {
            return new DomainException(
                    "RADIO_STATION_STREAM_FOR_RADIO_STATION_DOES_NOT_EXIST",
                    String.format(
                            "Radio station stream with id = `%s` does not exist for radio station with id = `%s`",
                            command.streamId,
                            command.radioStationId
                    )
            );
        }
    }

    public static class Result {
        public final long id;

        Result(long id) {
            this.id = id;
        }
    }
}