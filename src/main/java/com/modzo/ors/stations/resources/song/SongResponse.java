package com.modzo.ors.stations.resources.song;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.modzo.ors.stations.domain.song.Song;

import java.time.ZonedDateTime;
import java.util.UUID;

class SongResponse {

    private final long id;

    private final UUID uniqueId;

    private final ZonedDateTime created;

    private final String title;

    @JsonCreator
    private SongResponse(@JsonProperty("id") long id,
                         @JsonProperty("uniqueId") UUID uniqueId,
                         @JsonProperty("created") ZonedDateTime created,
                         @JsonProperty("title") String title) {
        this.id = id;
        this.uniqueId = uniqueId;
        this.created = created;
        this.title = title;
    }

    static SongResponse create(Song song) {
        return new SongResponse(song.getId(), song.getUniqueId(), song.getCreated(), song.getTitle());
    }

    public long getId() {
        return id;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public String getTitle() {
        return title;
    }
}
