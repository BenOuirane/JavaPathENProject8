package com.openclassrooms.tourguide.record;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record NearByAttraction(
		    String attractionName,
	        @JsonIgnore
	        UUID attractionId,
	        double latitude,
	        double longitude,
	        Double distance,
	        int reward
	) {
	    public NearByAttraction withReward(int reward) {
	        return new NearByAttraction(attractionName(), attractionId(), latitude(), longitude(), distance(), reward);
	    }
	}