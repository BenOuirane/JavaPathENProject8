package com.openclassrooms.tourguide.service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import rewardCentral.RewardCentral;

public class RewardCentralServiceAsync extends RewardCentral {
    public CompletableFuture<Integer> getAttractionRewardPointsAsync(UUID attractionId, UUID userId) {
        return CompletableFuture.supplyAsync(
                () -> this.getAttractionRewardPoints(attractionId, userId), Executors.newWorkStealingPool()
        );
    }
}