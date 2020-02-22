package com.modzo.ors.web.web.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "default", url = "${application.apiUrl}")
public interface RadioStationsClient {

    @GetMapping("/radio-stations")
    RestPageImpl<RadioStationResponse> getStations();

    @GetMapping("/radio-stations/{id}")
    RadioStationResponse getStation(@PathVariable("id") long id);
}