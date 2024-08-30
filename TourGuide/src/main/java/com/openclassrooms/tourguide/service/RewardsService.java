package com.openclassrooms.tourguide.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;


@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
    private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
    private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;
	
	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;
	}
	
	public RewardsService() {
		this.gpsUtil = new GpsUtil();
		this.rewardsCentral = new RewardCentral();
	}

	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}
	 
	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}
	

	
	
	private final RewardCentralServiceAsync rewardCentralServiceAsync = new RewardCentralServiceAsync();
	private final GpsUtilServiceAsync gpsUtilServiceAsync = new GpsUtilServiceAsync();
     
	public void calculateRewards(User user) {

		
		// Récupérer les attractions en une seule fois
		List<Attraction> attractions = gpsUtilServiceAsync.getAttractionsAsync().join();

		// Utiliser un CopyOnWriteArrayList uniquement si nécessaire, sinon préférer ArrayList pour de meilleures performances
		List<VisitedLocation> userLocations = new ArrayList<>(user.getVisitedLocations());

		// Créer un ensemble pour les attractions déjà récompensées afin de réduire les appels répétitifs
		Set<String> rewardedAttractions = user.getUserRewards().stream()
		        .map(r -> r.attraction.attractionName)
		        .collect(Collectors.toSet());

		// Utilisation du parallélisme pour traiter les visites de façon concurrente
		userLocations.parallelStream().forEach(visitedLocation -> {
		    attractions.stream()
		            .filter(a -> nearAttraction(visitedLocation, a) && !rewardedAttractions.contains(a.attractionName))
		            .forEach(a -> {
		                // Calculer la récompense de façon asynchrone pour chaque attraction
		                CompletableFuture<Integer> rewardPointsFuture = rewardCentralServiceAsync
		                        .getAttractionRewardPointsAsync(a.attractionId, user.getUserId());

		                // Attendre le résultat et ajouter la récompense à l'utilisateur
		                user.addUserReward(new UserReward(visitedLocation, a, rewardPointsFuture.join()));
		            });
		});
		
		
    }

    
    
    public void rewardForListOfUser(List<User> users) {
        // Fetch attractions asynchronously
        CompletableFuture<List<Attraction>> attractionsFuture = gpsUtilServiceAsync.getAttractionsAsync();

        // Process users after attractions are fetched
        attractionsFuture.thenAccept(attractions -> {
            // Use parallel stream to process users concurrently
            users.parallelStream().forEach(user -> {
                // Calculate rewards for each user
                calculateRewards(user);
            });
        })
        .join(); // Ensure we wait for all operations to complete
    }

	
    public void calculateRewards(List<User> users) {

        // Récupérer les attractions en une seule fois
        List<Attraction> attractions = gpsUtilServiceAsync.getAttractionsAsync().join();

        // Utilisation du parallélisme pour traiter les utilisateurs de façon concurrente
        users.parallelStream().forEach(user -> {
            // Utiliser un ArrayList pour les locations de l'utilisateur
            List<VisitedLocation> userLocations = new ArrayList<>(user.getVisitedLocations());

            // Créer un ensemble pour les attractions déjà récompensées afin de réduire les appels répétitifs
            Set<String> rewardedAttractions = user.getUserRewards().stream()
                    .map(r -> r.attraction.attractionName)
                    .collect(Collectors.toSet());

            // Utilisation du parallélisme pour traiter les visites de façon concurrente
            userLocations.parallelStream().forEach(visitedLocation -> {
                attractions.stream()
                        .filter(a -> nearAttraction(visitedLocation, a) && !rewardedAttractions.contains(a.attractionName))
                        .forEach(a -> {
                            // Calculer la récompense de façon asynchrone pour chaque attraction
                            CompletableFuture<Integer> rewardPointsFuture = rewardCentralServiceAsync
                                    .getAttractionRewardPointsAsync(a.attractionId, user.getUserId());

                            // Attendre le résultat et ajouter la récompense à l'utilisateur
                            user.addUserReward(new UserReward(visitedLocation, a, rewardPointsFuture.join()));
                        });
            });
        });
    }

    
	/*

	public void calculateRewards(User user) {
	    List<VisitedLocation> userLocations = new CopyOnWriteArrayList<>(user.getVisitedLocations());
	    List<Attraction> attractions = gpsUtil.getAttractions();

	    List<UserReward> newRewards = new CopyOnWriteArrayList<>();

	    for (VisitedLocation visitedLocation : userLocations) {
	        for (Attraction attraction : attractions) {
	            boolean alreadyRewarded = user.getUserRewards().stream()
	                .anyMatch(r -> r.attraction.attractionName.equals(attraction.attractionName));
	            
	            if (!alreadyRewarded && nearAttraction(visitedLocation, attraction)) {
	                newRewards.add(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
	            }
	        }
	    }

	    user.getUserRewards().addAll(newRewards);
	}
     */
   


       /*
	public void calculateRewards(User user) {
		List<VisitedLocation> userLocations = user.getVisitedLocations();
		List<Attraction> attractions = gpsUtil.getAttractions();
		
		for(VisitedLocation visitedLocation : userLocations) {
			for(Attraction attraction : attractions) {
				if(user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
					if(nearAttraction(visitedLocation, attraction)) {
						user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
					}
				}
			}
		}
	}
	       */
	
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}
	
    
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}
	
	
	private int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}
	
	public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                               + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
	}
	
	
	
}
