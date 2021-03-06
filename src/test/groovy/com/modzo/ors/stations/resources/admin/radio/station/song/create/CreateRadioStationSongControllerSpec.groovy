package com.modzo.ors.stations.resources.admin.radio.station.song.create

import com.modzo.ors.HttpEntityBuilder
import com.modzo.ors.TestUsers
import com.modzo.ors.stations.domain.radio.station.RadioStation
import com.modzo.ors.stations.domain.song.Song
import com.modzo.ors.stations.resources.IntegrationSpec
import org.springframework.http.ResponseEntity

import static java.time.ZonedDateTime.now
import static org.springframework.http.HttpMethod.POST
import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.OK

class CreateRadioStationSongControllerSpec extends IntegrationSpec {

    void 'admin should create radio station song'() {
        given:
            RadioStation radioStation = testRadioStation.create()
        and:
            Song song = testSong.create()
        and:
            CreateRadioStationSongRequest request = new CreateRadioStationSongRequest(
                    songId: song.id,
                    playedTime: now()
            )
        when:
            ResponseEntity<String> response = restTemplate.exchange(
                    "/admin/radio-stations/${radioStation.id}/songs",
                    POST,
                    HttpEntityBuilder.builder()
                            .bearer(token(TestUsers.ADMIN))
                            .body(request)
                            .build(),
                    String
            )
        then:
            response.statusCode == CREATED
        and:
            restTemplate.getForEntity(response.headers.getLocation().path, String).statusCode == OK
    }
}
