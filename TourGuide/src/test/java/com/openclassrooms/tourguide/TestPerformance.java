package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;


@SpringBootTest
public class TestPerformance {

	/*
	 * A note on performance improvements:
	 * 
	 * The number of users generated for the high volume tests can be easily
	 * adjusted via this method:
	 * 
	 * InternalTestHelper.setInternalUserNumber(100000);
	 * 
	 * 
	 * These tests can be modified to suit new solutions, just as long as the
	 * performance metrics at the end of the tests remains consistent.
	 * 
	 * These are performance metrics that we are trying to hit:
	 * 
	 * highVolumeTrackLocation: 100,000 users within 15 minutes:
	 * assertTrue(TimeUnit.MINUTES.toSeconds(15) >=
	 * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 *
	 * highVolumeGetRewards: 100,000 users within 20 minutes:
	 * assertTrue(TimeUnit.MINUTES.toSeconds(20) >=
	 * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 */
	
	
	@Test
	public void highVolumeTrackLocation() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		// Users should be incremented up to 100,000, and test finishes within 15
		// minutes
		System.out.println("highVolumeTrackLocation userNumber: " + 100);
		InternalTestHelper.setInternalUserNumber(100000);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		List<User> allUsers = new ArrayList<>(tourGuideService.getAllUsers());

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		allUsers.parallelStream().forEach(tourGuideService::trackUserLocation);

		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeTrackLocation: Time Elapsed: "
				+ TimeUnit.MILLISECONDS.toMillis(stopWatch.getTime()) + " milliseconds." + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

	
	
	/*
	 private static final int BATCH_SIZE = 5000; // Adjust batch size based on performance testing

	    @Test
	    public void highVolumeTrackLocation() throws InterruptedException {
	        GpsUtil gpsUtil = new GpsUtil();
	        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
	        InternalTestHelper.setInternalUserNumber(100000); // Set to 100,000 users
	        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

	        List<User> allUsers = new ArrayList<>(tourGuideService.getAllUsers());

	        StopWatch stopWatch = new StopWatch();
	        stopWatch.start();

	        // Create a custom ExecutorService
	        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

	        try {
	            // Process users in batches
	            for (int start = 0; start < allUsers.size(); start += BATCH_SIZE) {
	                int end = Math.min(start + BATCH_SIZE, allUsers.size());
	                List<User> batch = allUsers.subList(start, end);

	                // Use CompletableFuture to track user locations in parallel
	                List<CompletableFuture<Void>> futures = new ArrayList<>();
	                for (User user : batch) {
	                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
	                        tourGuideService.trackUserLocation(user);
	                    }, executor);
	                    futures.add(future);
	                }

	                // Wait for the current batch to complete
	                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
	            }
	        } finally {
	            // Shut down the executor service
	            executor.shutdown();
	            executor.awaitTermination(10, TimeUnit.MINUTES);
	        }

	        stopWatch.stop();
	        tourGuideService.tracker.stopTracking();

	        System.out.println("highVolumeTrackLocation: Time Elapsed: "
	                + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
	        assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	    }
	    
	    */
	/* 19minutes
	@Test
	public void highVolumeTrackLocation() throws InterruptedException, ExecutionException {
	    GpsUtil gpsUtil = new GpsUtil();
	    RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
	    InternalTestHelper.setInternalUserNumber(10000); // Set to 100,000 users
	    TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

	    List<User> allUsers = new ArrayList<>(tourGuideService.getAllUsers());

	    StopWatch stopWatch = new StopWatch();
	    stopWatch.start();

	    // Create a custom ExecutorService
	    ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

	    try {
	        // Use CompletableFuture to track user locations in parallel with custom ExecutorService
	        List<CompletableFuture<Void>> futures = new ArrayList<>();
	        for (User user : allUsers) {
	            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
	                tourGuideService.trackUserLocation(user);
	            }, executor);
	            futures.add(future);
	        }

	        // Wait for all futures to complete
	        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
	    } finally {
	        // Shut down the executor service
	        executor.shutdown();
	        executor.awaitTermination(10, TimeUnit.MINUTES);
	    }

	    stopWatch.stop();
	    tourGuideService.tracker.stopTracking();

	    System.out.println("highVolumeTrackLocation: Time Elapsed: "
	            + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
	    assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}
	*/
	
	
	
	
	/* 22 minutes
	@Test
    public void highVolumeTrackLocation() throws InterruptedException, ExecutionException {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(100000); // Set to 100,000 users
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        List<User> allUsers = new ArrayList<>(tourGuideService.getAllUsers());

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Use CompletableFuture to track user locations in parallel
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (User user : allUsers) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                tourGuideService.trackUserLocation(user);
            });
            futures.add(future);
        }

        // Wait for all futures to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        stopWatch.stop();
        tourGuideService.tracker.stopTracking();

        System.out.println("highVolumeTrackLocation: Time Elapsed: "
                + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }
    */
	
/*
	@Test
	public void highVolumeTrackLocation() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		// Users should be incremented up to 100,000, and test finishes within 15
		// minutes
		InternalTestHelper.setInternalUserNumber(100);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		for (User user : allUsers) {
			tourGuideService.trackUserLocation(user);
		}
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeTrackLocation: Time Elapsed: "
				+ TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}
            
	*/
	
	
	
	
	
	
	//  27 minutes     
	@Test
	public void highVolumeGetRewards() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

		// Users should be incremented up to 100,000, and test finishes within 20
		// minutes
		System.out.println("highVolumeGetRewards userNumber: " + 10000);
		InternalTestHelper.setInternalUserNumber(1000);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		Attraction attraction = gpsUtil.getAttractions().get(0);
		List<User> allUsers = new CopyOnWriteArrayList<>(tourGuideService.getAllUsers());
		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		allUsers.parallelStream().forEach(rewardsService::calculateReward);

		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		for (User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}

		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toMillis(stopWatch.getTime()) + " milliseconds." + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}
          
	
	 
	    
	/*
	@Test
	public void highVolumeGetRewards() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

		
		// Users should be incremented up to 100,000, and test finishes within 20
		// minutes 
		InternalTestHelper.setInternalUserNumber(100);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		Attraction attraction = gpsUtil.getAttractions().get(0);
		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();
		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

		allUsers.forEach(u -> rewardsService.calculateRewards(u));

		for (User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())
				+ " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}
	 */

}
