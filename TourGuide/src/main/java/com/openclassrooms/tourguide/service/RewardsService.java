package com.openclassrooms.tourguide.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
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
	
	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}
	 
	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}
	
  //  private static final Logger logger = Logger.getLogger(RewardsService.class.getName());
	/*
	private final ReentrantLock lock = new ReentrantLock();

	public void calculateRewards(User user) {
	    lock.lock();
	    try {
	        List<VisitedLocation> userLocations = new ArrayList<>(user.getVisitedLocations());
	        List<Attraction> attractions = gpsUtil.getAttractions();

	        Set<String> rewardedAttractions = user.getUserRewards().stream()
	            .map(reward -> reward.attraction.attractionName)
	            .collect(Collectors.toSet());

	        List<UserReward> rewardsToAdd = new ArrayList<>();
	        int totalRewards = 0; 

	        for (VisitedLocation visitedLocation : userLocations) {
	            for (Attraction attraction : attractions) {
	                if (!rewardedAttractions.contains(attraction.attractionName) &&
	                    nearAttraction(visitedLocation, attraction)) {

	                    rewardsToAdd.add(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
	                    rewardedAttractions.add(attraction.attractionName);
	                    totalRewards++; 
	                }
	            }
	        }

	        user.getUserRewards().addAll(rewardsToAdd);
	        System.out.println("Total rewards to add: " + totalRewards); 
	    } finally {
	        lock.unlock();
	    }
	}
*/
	
	
	private final ReentrantLock lock = new ReentrantLock();
    public void calculateRewards(User user) {
    	 synchronized (lock) {
        List<VisitedLocation> userLocations = new ArrayList<>(user.getVisitedLocations());
        List<Attraction> attractions = gpsUtil.getAttractions();
        
        Set<String> rewardedAttractions = user.getUserRewards().stream()
            .map(reward -> reward.attraction.attractionName)
            .collect(Collectors.toSet());

        List<UserReward> rewardsToAdd = new ArrayList<>();
        int totalRewards = 0; // Ajoutez cette variable pour suivre le nombre total de récompenses

        for (VisitedLocation visitedLocation : userLocations) {
            for (Attraction attraction : attractions) {
                if (!rewardedAttractions.contains(attraction.attractionName) &&
                    nearAttraction(visitedLocation, attraction)) {

                    // Ajoute la récompense
                    rewardsToAdd.add(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
                    rewardedAttractions.add(attraction.attractionName);
                    totalRewards++; // Incrémentez le total
                }
            }
        }

        user.getUserRewards().addAll(rewardsToAdd);
        System.out.println("Total rewards to add: " + totalRewards); // Affichez le total des récompenses
    }
    }


	
	/*
	public void calculateRewards(User user) {
	    List<VisitedLocation> userLocations = new ArrayList<>(user.getVisitedLocations());
	    List<Attraction> attractions = gpsUtil.getAttractions();

	    Set<String> rewardedAttractions = user.getUserRewards().stream()
	        .map(reward -> reward.attraction.attractionName)
	        .collect(Collectors.toSet());

	    List<UserReward> rewardsToAdd = Collections.synchronizedList(new ArrayList<>());

	    // Parallel processing of visited locations
	    userLocations.parallelStream().forEach(visitedLocation -> {
	        attractions.parallelStream().forEach(attraction -> {
	            if (!rewardedAttractions.contains(attraction.attractionName)) {
	                if (nearAttraction(visitedLocation, attraction)) {
	                    synchronized (rewardsToAdd) {
	                        rewardsToAdd.add(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
	                    }
	                    rewardedAttractions.add(attraction.attractionName);
	                }
	            }
	        });
	    });

	    // Add all collected rewards to the user's rewards
	    user.getUserRewards().addAll(rewardsToAdd);
	}
          */
	
	/*
	public void calculateRewards(User user) {
	    List<VisitedLocation> userLocations = new ArrayList<>(user.getVisitedLocations());  // Copy of the visited locations

	    // Fetch the attractions internally
	    List<Attraction> attractions = gpsUtil.getAttractions();

	    // Use a Set to store already rewarded attractions for faster lookup
	    Set<String> rewardedAttractions = user.getUserRewards().stream()
	        .map(reward -> reward.attraction.attractionName)
	        .collect(Collectors.toSet());

	    // Collect rewards to be added
	    List<UserReward> rewardsToAdd = new ArrayList<>();

	    // Loop through copied visited locations and attractions
	    for (VisitedLocation visitedLocation : userLocations) {
	        for (Attraction attraction : attractions) {
	            // Skip if the user has already been rewarded for this attraction
	            if (!rewardedAttractions.contains(attraction.attractionName)) {
	                if (nearAttraction(visitedLocation, attraction)) {
	                    UserReward reward = new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user));
	                    rewardsToAdd.add(reward);  // Add to the temporary list
	                    rewardedAttractions.add(attraction.attractionName);
	                }
	            }
	        }
	    }

	    // Add all collected rewards to the user's rewards
	    user.getUserRewards().addAll(rewardsToAdd);
	}
           */


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
