package com.modzo.ors.stations.resources.admin.radio.station.data.importer;

import com.modzo.ors.stations.domain.DomainException;
import com.modzo.ors.stations.domain.radio.station.RadioStation;
import com.modzo.ors.stations.domain.radio.station.RadioStations;
import com.modzo.ors.stations.domain.radio.station.commands.CreateRadioStation;
import com.modzo.ors.stations.domain.radio.station.commands.FindRadioStationByTitle;
import com.modzo.ors.stations.domain.radio.station.commands.UpdateRadioStation;
import com.modzo.ors.stations.domain.radio.station.stream.RadioStationStream;
import com.modzo.ors.stations.domain.radio.station.stream.commands.CreateRadioStationStream;
import com.modzo.ors.stations.domain.radio.station.stream.commands.FindRadioStationStreamByUrl;
import com.modzo.ors.stations.domain.radio.station.stream.commands.UpdateRadioStationStream;
import com.modzo.ors.stations.resources.admin.radio.station.data.BackupData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;

@Component
class ImporterService {

    private static final Logger logger = LoggerFactory.getLogger(ImporterService.class);

    private final FindRadioStationByTitle.Handler findRadioStationByTitleHandler;

    private final CreateRadioStation.Handler createRadioStationHandler;

    private final CreateRadioStationStream.Handler createRadioStationStreamHandler;

    private final FindRadioStationStreamByUrl.Handler findRadioStationStreamByUrlHandler;

    private final UpdateRadioStationStream.Handler updateRadioStationStreamHandler;

    private final UpdateRadioStation.Handler updateRadioStationHandler;

    private final RadioStations radioStations;

    ImporterService(FindRadioStationByTitle.Handler findRadioStationByTitleHandler,
                    CreateRadioStation.Handler createRadioStationHandler,
                    CreateRadioStationStream.Handler createRadioStationStreamHandler,
                    FindRadioStationStreamByUrl.Handler findRadioStationStreamByUrlHandler,
                    UpdateRadioStationStream.Handler updateRadioStationStreamHandler,
                    UpdateRadioStation.Handler updateRadioStationHandler,
                    RadioStations radioStations) {
        this.findRadioStationByTitleHandler = findRadioStationByTitleHandler;
        this.createRadioStationHandler = createRadioStationHandler;
        this.createRadioStationStreamHandler = createRadioStationStreamHandler;
        this.findRadioStationStreamByUrlHandler = findRadioStationStreamByUrlHandler;
        this.updateRadioStationStreamHandler = updateRadioStationStreamHandler;
        this.updateRadioStationHandler = updateRadioStationHandler;
        this.radioStations = radioStations;
    }

    void run(MultipartFile file, boolean importUniqueIds) {
        try {

            List<BackupData> data = JsonReader.read(file);
            ForkJoinPool customThreadPool = new ForkJoinPool(4);
            customThreadPool
                    .submit(() -> data.parallelStream().forEach(entry -> doImport(entry, importUniqueIds)))
                    .get();
        } catch (Exception exception) {
            logger.error("Failed to import radio stations", exception);
            throw new DomainException(
                    "FAILED_TO_IMPORT_RADIO_STATIONS",
                    "file",
                    exception.getMessage()
            );
        }
    }

    private void doImport(BackupData entry, boolean importUniqueIds) {
        String radioStationName = entry.getTitle();

        if (entry.getStreams().isEmpty()) {
            logger.warn(
                    "Radio station name `{}` does not have importable streams. Skipping creation.",
                    radioStationName
            );
            return;
        }

        if (importUniqueIds) {
            Optional<RadioStation> existingStationByUniqueId = radioStations
                    .findByUniqueId(UUID.fromString(entry.getUniqueId()));
            if (existingStationByUniqueId.isPresent()) {
                logger.warn("Radio station uuid `{}` already exists. Skipping creation.", entry.getUniqueId());
                createStreamUrls(existingStationByUniqueId.get().getId(), entry.getStreams());
                return;
            }
        }

        Optional<RadioStation> existingStationByTitle = findRadioStationByTitleHandler.handle(
                new FindRadioStationByTitle(radioStationName)
        );

        if (existingStationByTitle.isPresent()) {
            logger.warn("Radio station name `{}` already exists. Skipping creation.", radioStationName);
            createStreamUrls(existingStationByTitle.get().getId(), entry.getStreams());
        } else {
            CreateRadioStation.Result result = createRadioStationHandler.handle(
                    importUniqueIds
                            ? new CreateRadioStation(UUID.fromString(entry.getUniqueId()), radioStationName)
                            : new CreateRadioStation(radioStationName)
            );
            createStreamUrls(result.id, entry.getStreams());

            RadioStation currentRadioStation = radioStations.findById(result.id).get();

            updateRadioStationHandler.handle(new UpdateRadioStation(result.id, new UpdateRadioStation.DataBuilder()
                    .fromCurrent(currentRadioStation)
                    .setEnabled(entry.isEnabled())
                    .build())
            );
        }
    }

    private void createStreamUrls(Long radioStationId, List<BackupData.Stream> streams) {
        streams.forEach(stream -> createStreamUrl(radioStationId, stream.getUrl(), stream.isWorking()));
    }

    private void createStreamUrl(Long radioStationId, String streamUrl, boolean isWorking) {
        if (findRadioStationStreamByUrlHandler.handle(new FindRadioStationStreamByUrl(streamUrl)).isPresent()) {
            logger.warn("Stream url `{}` already exists. Skipping creation.", streamUrl);
            return;
        }

        CreateRadioStationStream.Result result = createRadioStationStreamHandler.handle(
                new CreateRadioStationStream(radioStationId, streamUrl)
        );

        RadioStationStream savedStream = findRadioStationStreamByUrlHandler
                .handle(new FindRadioStationStreamByUrl(streamUrl)).get();

        updateRadioStationStreamHandler.handle(
                new UpdateRadioStationStream(
                        radioStationId,
                        result.id,
                        new UpdateRadioStationStream.DataBuilder()
                                .setUrl(savedStream.getUrl())
                                .setBitRate(savedStream.getBitRate())
                                .setType(savedStream.getType().orElse(null))
                                .setWorking(isWorking)
                                .build()
                )
        );
    }
}
