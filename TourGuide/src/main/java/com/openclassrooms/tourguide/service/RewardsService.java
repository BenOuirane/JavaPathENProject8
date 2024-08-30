package com.openclassrooms.tourguide.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
	
	public RewardsService() {
		this.gpsUtil = new GpsUtil();
		this.rewardsCentral = new RewardCentral();
		// TODO Auto-generated constructor stub
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
	
	
	private final RewardCentralServiceAsync rewardCentralServiceAsync = new RewardCentralServiceAsync();
	private final GpsUtilServiceAsync gpsUtilServiceAsync = new GpsUtilServiceAsync();
	private final Object lock = new Object();
     

	
	
	/*
	public void calculateReward(User user) {
		List<Attraction> attractions = gpsUtilServiceAsync.getAttractionsAsync().join();

		List<VisitedLocation> userLocations = new CopyOnWriteArrayList<>(user.getVisitedLocations());
		userLocations.forEach((visitedLocation) -> {
			attractions.stream()
					.filter(a -> (nearAttraction(visitedLocation, a)) && user
							.getUserRewards()
							.stream()
							.noneMatch(r -> r
							.attraction.attractionName.equals(a.attractionName)))
					.forEach(a -> user
							.addUserReward(new UserReward(visitedLocation, a, rewardCentralServiceAsync
							.getAttractionRewardPointsAsync(a.attractionId,user.getUserId()).join())));
		});
	 
	}
	*/
	
	/*
	public void calculateRewards(User user) {
	    // Utilisation d'un thread pool pour limiter le nombre de threads créés
	    ExecutorService executor = Executors.newFixedThreadPool(Math.min(user.getVisitedLocations().size(), 10000));

	    // Récupérer les attractions de manière asynchrone
	    CompletableFuture<List<Attraction>> attractionsFuture = CompletableFuture.supplyAsync(() -> gpsUtil.getAttractions(), executor);

	    // Une fois les attractions récupérées, traiter les récompenses
	    List<CompletableFuture<Void>> futures = attractionsFuture.thenApply(attractions -> {
	        return user.getVisitedLocations().stream()
	            .map(visitedLocation -> CompletableFuture.runAsync(() -> {
	                attractions.stream()
	                    .filter(attraction -> user.getUserRewards().stream()
	                        .noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName)) &&
	                        nearAttraction(visitedLocation, attraction))
	                    .forEach(attraction -> {
	                        int rewardPoints = getRewardPoints(attraction, user);
	                        user.addUserReward(new UserReward(visitedLocation, attraction, rewardPoints));
	                    });
	            }, executor))
	            .collect(Collectors.toList());
	    }).join();

	    // Attendre que toutes les tâches soient terminées
	    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

	    // Fermer le pool de threads après utilisation
	    executor.shutdown();
	}
	  */
	  
	
            
	
	
	
	
	
	   /*
	public void calculateRewards(User user) {
	    // Utilisation d'un thread pool pour limiter le nombre de threads créés
	    ExecutorService executor = Executors.newFixedThreadPool(Math.min(user.getVisitedLocations().size(), 1000));

	    // Récupérer les attractions de manière asynchrone
	    CompletableFuture<List<Attraction>> attractionsFuture = gpsUtilServiceAsync.getAttractionsAsync();

	    // Une fois les attractions récupérées, traiter les récompenses
	    attractionsFuture.thenAccept(attractions -> {
	        List<VisitedLocation> userLocations = new CopyOnWriteArrayList<>(user.getVisitedLocations());

	        // Traitement parallèle des emplacements visités
	        List<CompletableFuture<Void>> futures = userLocations.stream()
	            .map(visitedLocation -> CompletableFuture.runAsync(() -> {
	                attractions.stream()
	                    .filter(a -> nearAttraction(visitedLocation, a) &&
	                        user.getUserRewards().stream()
	                            .noneMatch(r -> r.attraction.attractionName.equals(a.attractionName)))
	                    .forEach(a -> {
	                        // Récupération asynchrone des points de récompense
	                        int rewardPoints = rewardCentralServiceAsync
	                            .getAttractionRewardPointsAsync(a.attractionId, user.getUserId()).join();
	                        // Ajout de la récompense à l'utilisateur
	                        user.addUserReward(new UserReward(visitedLocation, a, rewardPoints));
	                    });
	            }, executor)) // Exécuter chaque tâche dans le pool de threads
	            .collect(Collectors.toList());

	        // Attendre que toutes les tâches de calcul des récompenses soient terminées
	        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
	    }).join(); // Attendre la fin de toutes les opérations

	    // Fermer le pool de threads après utilisation
	    executor.shutdown();
	}
           
	*/
	
	
	
	

	
	/*
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
          */
          
	
	
	
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
