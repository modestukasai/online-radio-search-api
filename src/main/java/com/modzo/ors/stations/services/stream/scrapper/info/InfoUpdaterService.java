package com.modzo.ors.stations.services.stream.scrapper.info;

import com.modzo.ors.stations.domain.radio.station.RadioStation;
import com.modzo.ors.stations.domain.radio.station.commands.GetRadioStation;
import com.modzo.ors.stations.domain.radio.station.commands.UpdateRadioStation;
import com.modzo.ors.stations.domain.radio.station.genre.Genre;
import com.modzo.ors.stations.domain.radio.station.genre.commands.CreateGenre;
import com.modzo.ors.stations.domain.radio.station.genre.commands.FindGenre;
import com.modzo.ors.stations.domain.radio.station.stream.RadioStationStream;
import com.modzo.ors.stations.domain.radio.station.stream.StreamUrl;
import com.modzo.ors.stations.domain.radio.station.stream.commands.GetRadioStationStream;
import com.modzo.ors.stations.domain.radio.station.stream.commands.UpdateRadioStationStream;
import com.modzo.ors.stations.domain.radio.station.stream.commands.UpdateStreamUrlCheckedTime;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

@Component
public class InfoUpdaterService {

    private static final Logger log = LoggerFactory.getLogger(InfoUpdaterService.class);

    private final GetRadioStationStream.Handler radioStationStream;

    private final GetRadioStation.Handler radioStation;

    private final UpdateRadioStationStream.Handler updateRadioStationStream;

    private final UpdateRadioStation.Handler updateRadioStation;

    private final StreamScrapper streamScrapper;

    private final CreateGenre.Handler createGenre;

    private final FindGenre.Handler findGenre;

    private final UpdateStreamUrlCheckedTime.Handler updateStreamUrlCheckedTime;

    InfoUpdaterService(GetRadioStationStream.Handler radioStationStream,
                       GetRadioStation.Handler radioStation,
                       UpdateRadioStationStream.Handler updateRadioStationStream,
                       UpdateRadioStation.Handler updateRadioStation,
                       StreamScrapper streamScrapper,
                       CreateGenre.Handler createGenre,
                       FindGenre.Handler findGenre,
                       UpdateStreamUrlCheckedTime.Handler updateStreamUrlCheckedTime) {
        this.radioStationStream = radioStationStream;
        this.radioStation = radioStation;
        this.updateRadioStationStream = updateRadioStationStream;
        this.updateRadioStation = updateRadioStation;
        this.streamScrapper = streamScrapper;
        this.createGenre = createGenre;
        this.findGenre = findGenre;
        this.updateStreamUrlCheckedTime = updateStreamUrlCheckedTime;
    }

    public void update(long radioStationId, long streamId) {
        log.info("Processing radio station `{}` and stream `{}`", radioStationId, streamId);
        RadioStationStream stream = radioStationStream.handle(
                new GetRadioStationStream(radioStationId, streamId)
        );

        stream.findUrl(StreamUrl.Type.INFO)
                .ifPresent(streamUrl -> updateInfo(streamUrl, stream));


        log.info("Finished processing radio station `{}` and stream `{}`", radioStationId, streamId);
    }

    private void updateInfo(StreamUrl streamUrl, RadioStationStream stream) {

        updateStreamUrlCheckedTime.handle(new UpdateStreamUrlCheckedTime(streamUrl.getId(), ZonedDateTime.now()));

        Optional<StreamScrapper.Response> scrappedPage = streamScrapper.scrap(
                new StreamScrapper.Request(streamUrl.getUrl())
        );

        if (scrappedPage.isPresent()) {
            var response = scrappedPage.get();
            updateRadioStreamInfo(response, stream);
            updateRadioStationInfo(response, stream.getRadioStationId());
        }
    }

    private void updateRadioStationInfo(StreamScrapper.Response response, long radioStationId) {
        RadioStation currentRadioStation = radioStation.handle(new GetRadioStation(radioStationId));

        String radioStationName = resolveString(currentRadioStation.getTitle(), response.getStreamName());
        String website = resolveString(currentRadioStation.getWebsite(), response.getWebsite());

        var genres = resolveSet(currentRadioStation.getGenres(), genres(response.getGenres()))
                .stream()
                .map(genre -> new UpdateRadioStation.Data.Genre(genre.getId()))
                .collect(Collectors.toSet());

        UpdateRadioStation.Data data = new UpdateRadioStation.DataBuilder()
                .setTitle(radioStationName)
                .setWebsite(website)
                .setEnabled(true)
                .setGenres(genres)
                .build();

        updateRadioStation.handle(new UpdateRadioStation(radioStationId, data));
    }

    private Set<Genre> resolveSet(Set<Genre> previousSet, Set<Genre> newSet) {
        if (!isEmpty(previousSet) && isEmpty(newSet)) {
            return previousSet;
        }

        return newSet;
    }

    private String resolveString(String previousString, String newString) {
        if (isNotBlank(previousString) && isBlank(newString)) {
            return previousString;
        }

        return newString;
    }

    private Set<Genre> genres(List<String> genres) {
        return genres.stream()
                .filter(StringUtils::isNotBlank)
                .map(this::findOrCreateGenre)
                .collect(toSet());
    }

    private Genre findOrCreateGenre(String genre) {
        Optional<Genre> foundGenre = findGenre.handle(new FindGenre(genre));
        if (foundGenre.isPresent()) {
            return foundGenre.get();
        } else {
            createGenre.handle(new CreateGenre(genre));
            return findGenre.handle(new FindGenre(genre)).get();
        }
    }

    private void updateRadioStreamInfo(StreamScrapper.Response response, RadioStationStream stream) {
        UpdateRadioStationStream.Data data = new UpdateRadioStationStream.DataBuilder()
                .setUrl(stream.getUrl())
                .setBitRate(response.getBitrate())
                .setType(resolve(response.getFormat()))
                .setWorking(true)
                .build();

        UpdateRadioStationStream update = new UpdateRadioStationStream(
                stream.getRadioStationId(),
                stream.getId(),
                data
        );
        updateRadioStationStream.handle(update);
    }

    private RadioStationStream.Type resolve(StreamScrapper.Response.Format format) {
        switch (format) {
            case AAC:
                return RadioStationStream.Type.ACC;
            case MPEG:
                return RadioStationStream.Type.MPEG;
            case MP3:
                return RadioStationStream.Type.MP3;
            case UNKNOWN:
                return RadioStationStream.Type.UNKNOWN;
            default:
                throw new IllegalArgumentException(format("Failed to resolve %s", format));
        }
    }
}
