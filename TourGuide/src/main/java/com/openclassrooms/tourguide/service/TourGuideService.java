package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.record.NearByAttraction;
import com.openclassrooms.tourguide.record.NearByAttractionList;
import com.openclassrooms.tourguide.tracker.Tracker;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;

import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsUtil gpsUtil;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	boolean testMode = true;

	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;
		
		Locale.setDefault(Locale.US);
 
		if (testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}

	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}
	
	
	public List<VisitedLocation> getUserLocation(List<User> users) {
	    // Liste pour stocker les résultats
	    List<VisitedLocation> visitedLocations = new ArrayList<>();

	    // Parcourir tous les utilisateurs
	    for (User user : users) {
	        // Si l'utilisateur a déjà des localisations visitées, utiliser la dernière localisation
	        if (user.getVisitedLocations().size() > 0) {
	            visitedLocations.add(user.getLastVisitedLocation());
	        } else {
	            // Si l'utilisateur n'a pas de localisation, traitez-le avec trackAllUsersLocations
	            List<User> singleUserList = List.of(user);
	            visitedLocations.addAll(trackAllUsersLocations(singleUserList));
	        }
	    }

	    return visitedLocations;
	}

/*
	public VisitedLocation getUserLocation(User user) {
	    // Si l'utilisateur a déjà des localisations visitées, retourner la dernière
	    if (user.getVisitedLocations().size() > 0) {
	        return user.getLastVisitedLocation();
	    } else {
	        // Sinon, traitez l'utilisateur dans une liste avec la méthode optimisée
	        List<User> singleUserList = List.of(user);
	        List<VisitedLocation> visitedLocations = trackAllUsersLocations(singleUserList);
	        
	        // Puisque nous savons qu'il n'y a qu'un seul utilisateur dans la liste, retourner le premier résultat
	        return visitedLocations.get(0);
	    }
	}
	*/
	
	/*
	public VisitedLocation getUserLocation(User user) {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ? user.getLastVisitedLocation()
				: trackUserLocation(user);
		return visitedLocation;
	}
        */
	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}

	public List<User> getAllUsers() {
		return internalUserMap.values().stream().collect(Collectors.toList());
	}

	public void addUser(User user) {
		if (!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}

	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),
				user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
				user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}

	
	public List<VisitedLocation> trackAllUsersLocations(List<User> users) {
	    // Utilisation d'un thread pool pour limiter le nombre de threads créés
	    ExecutorService executor = Executors.newFixedThreadPool(Math.min(users.size(), 1000)); // Ajustez en fonction des capacités de votre serveur

	    // Traitement parallèle des utilisateurs
	    List<CompletableFuture<VisitedLocation>> futures = users.stream()
	            .map(user -> CompletableFuture.supplyAsync(() -> {
	                // Récupérer la localisation de l'utilisateur de manière asynchrone
	                VisitedLocation visitedLocation = gpsUtilServiceAsync.getUserLocationAsync(user.getUserId()).join();
	                
	                // Ajouter la localisation et lancer le calcul des récompenses de manière asynchrone
	                user.addToVisitedLocations(visitedLocation);
	                CompletableFuture.runAsync(() -> rewardsService.calculateRewards(user), executor);
	                
	                // Retourner immédiatement la localisation
	                return visitedLocation;
	            }, executor))
	            .collect(Collectors.toList());

	    // Attendre que toutes les opérations soient terminées et collecter les résultats
	    List<VisitedLocation> results = futures.stream()
	            .map(CompletableFuture::join)
	            .collect(Collectors.toList());

	    executor.shutdown(); // Fermer le pool de threads
	    return results;
	}
	
	/*
	public VisitedLocation trackUserLocation(User user) {
	    // Lancer l'obtention de la localisation de l'utilisateur de manière asynchrone
	    CompletableFuture<VisitedLocation> locationFuture = gpsUtilServiceAsync.getUserLocationAsync(user.getUserId());
	    
	    // Dès que la localisation est obtenue, commencez à calculer les récompenses de manière asynchrone
	    CompletableFuture<Void> rewardsFuture = locationFuture.thenAcceptAsync(visitedLocation -> {
	        user.addToVisitedLocations(visitedLocation);
	        rewardsService.calculateRewards(user);
	    });

	    // Attendre l'obtention de la localisation, mais ne pas attendre le calcul des récompenses
	    VisitedLocation visitedLocation = locationFuture.join();

	    // Retourner la localisation immédiatement
	    return visitedLocation;
	}
	*/
	   
	
	public VisitedLocation trackUserLocation(User user) {
		VisitedLocation visitedLocation = gpsUtilServiceAsync.getUserLocationAsync(user.getUserId()).join();
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		return visitedLocation;
	}
		
	// we nned to optimize this method : 
	
	/*
	public VisitedLocation trackUserLocation(User user) {
		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		return visitedLocation;
	}
	*/
	
	private final RewardCentralServiceAsync rewardCentralServiceAsync = new RewardCentralServiceAsync();
	private final GpsUtilServiceAsync gpsUtilServiceAsync = new GpsUtilServiceAsync();
	
	public NearByAttractionList getNearByAttractions(VisitedLocation visitedLocation) {
		NearByAttraction[] nearByAttractions = gpsUtilServiceAsync.getAttractionsAsync().join().stream()
				.map(attraction -> new NearByAttraction(
						attraction.attractionName,
						attraction.attractionId,
						attraction.latitude,
						attraction.longitude,
						rewardsService.getDistance(attraction, visitedLocation.location),
						0
				))
				.sorted(Comparator.comparingDouble(NearByAttraction::distance))
				.limit(5)
				.map(nearByAttraction -> nearByAttraction
						   .withReward(rewardCentralServiceAsync
								   .getAttractionRewardPointsAsync(nearByAttraction.attractionId(), visitedLocation.userId).join()))
				.toArray(NearByAttraction[]::new);
		return new NearByAttractionList(visitedLocation.location, nearByAttractions);
          }
        
/*
	
	public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
		List<Attraction> nearbyAttractions = new ArrayList<>();
		for (Attraction attraction : gpsUtil.getAttractions()) {
			if (rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
				nearbyAttractions.add(attraction);
			}
		}

		return nearbyAttractions;
	}
	*/

	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				tracker.stopTracking();
			}
		});
	}

	/**********************************************************************************
	 * 
	 * Methods Below: For Internal Testing
	 * 
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes
	// internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();

	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);

			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}

	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i -> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
					new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}

	private double generateRandomLongitude() {
		double leftLimit = -180;
		double rightLimit = 180;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
		double rightLimit = 85.05112878;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
		return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}



}
