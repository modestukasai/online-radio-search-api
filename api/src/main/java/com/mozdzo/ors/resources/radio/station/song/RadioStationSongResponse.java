package com.mozdzo.ors.resources.radio.station.song;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mozdzo.ors.domain.radio.station.song.RadioStationSong;

import java.time.ZonedDateTime;

public class RadioStationSongResponse {

    private final long id;

    private final long songId;

    private final ZonedDateTime playedTime;

    @JsonCreator
    private RadioStationSongResponse(@JsonProperty("id") long id,
                                     @JsonProperty("songId") long songId,
                                     @JsonProperty("playedTime") ZonedDateTime playedTime) {
        this.id = id;
        this.songId = songId;
        this.playedTime = playedTime;
    }

    static RadioStationSongResponse create(RadioStationSong radioStationSong) {
        return new RadioStationSongResponse(
                radioStationSong.getId(),
                radioStationSong.getSongId(),
                radioStationSong.getPlayedTime()
        );
    }

    public long getId() {
        return id;
    }

    public long getSongId() {
        return songId;
    }

    public ZonedDateTime getPlayedTime() {
        return playedTime;
    }
}
