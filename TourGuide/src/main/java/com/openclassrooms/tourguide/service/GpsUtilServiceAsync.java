package com.openclassrooms.tourguide.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;

import gpsUtil.GpsUtil;

public class GpsUtilServiceAsync  extends GpsUtil {
	
    public CompletableFuture<VisitedLocation> getUserLocationAsync(UUID userId) {
        return CompletableFuture.supplyAsync(
                () -> this.getUserLocation(userId), Executors.newWorkStealingPool()
        );
    }

    public CompletableFuture<List<Attraction>> getAttractionsAsync() {
        return CompletableFuture.supplyAsync(this::getAttractions, Executors.newWorkStealingPool());
    }
}