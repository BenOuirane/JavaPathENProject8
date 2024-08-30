package com.openclassrooms.tourguide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gpsUtil.location.VisitedLocation;

import com.openclassrooms.tourguide.record.NearByAttraction;
import com.openclassrooms.tourguide.record.NearByAttractionList;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

import tripPricer.Provider;

@RestController
public class TourGuideController {

	@Autowired
	TourGuideService tourGuideService;
	
    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }
    
    
    @RequestMapping("/getLocation")
    public VisitedLocation getLocation(@RequestParam String userName) {
        // Obtenez l'utilisateur par le nom d'utilisateur
        User user = getUser(userName);

        // Créez une liste contenant cet utilisateur
        List<User> userList = List.of(user);

        // Appel à la méthode qui retourne une liste avec une seule localisation
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(userList).get(0);

        // Retournez la localisation obtenue (comme dans la méthode d'origine)
        return visitedLocation;
    }

    
    /*
    @RequestMapping("/getLocation") 
    public VisitedLocation getLocation(@RequestParam String userName) {
    	return tourGuideService.getUserLocation(getUser(userName));
    }
    */
    
    
    
    //  TODO: Change this method to no longer return a List of Attractions.
 	//  Instead: Get the closest five tourist attractions to the user - no matter how far away they are.
 	//  Return a new JSON object that contains:
    	// Name of Tourist attraction, 
        // Tourist attractions lat/long, 
        // The user's location lat/long, 
        // The distance in miles between the user's location and each of the attractions.
        // The reward points for visiting each Attraction.
        //    Note: Attraction reward points can be gathered from RewardsCentral
    
    
    @RequestMapping("/getNearbyAttractions")
    public NearByAttractionList getNearbyAttractions(@RequestParam String userName) {
        // Obtenez l'utilisateur par le nom d'utilisateur
        User user = getUser(userName);
        
        // Créez une liste d'utilisateurs, ici nous n'avons qu'un seul utilisateur
        List<User> userList = List.of(user);

        // Obtenez la liste des localisations visitées pour l'utilisateur
        List<VisitedLocation> visitedLocations = tourGuideService.getUserLocation(userList);

        // Utilisez une liste temporaire pour accumuler les attractions
        List<NearByAttraction> allAttractions = new ArrayList<>();
        
        // Traitez chaque localisation pour obtenir les attractions à proximité
        for (VisitedLocation visitedLocation : visitedLocations) {
            // Obtenez un NearByAttractionList pour la localisation visitée
            NearByAttractionList attractionList = tourGuideService.getNearByAttractions(visitedLocation);
            
            // Extrayez les attractions de NearByAttractionList
            NearByAttraction[] nearbyAttractionsArray = attractionList.nearByAttractionList();
            
            // Ajoutez toutes les attractions à la liste
            allAttractions.addAll(Arrays.asList(nearbyAttractionsArray));
        }

        // Créez une instance de NearByAttractionList avec les attractions collectées
        NearByAttractionList attractionList = new NearByAttractionList(
            user.getLastVisitedLocation().location, // Utilisation de la dernière localisation pour l'exemple
            allAttractions.toArray(new NearByAttraction[0])
        );

        return attractionList;
    }



    
    /*
    @RequestMapping("/getNearbyAttractions")
    public NearByAttractionList getNearbyAttractions(@RequestParam String userName) {
    	return tourGuideService.getNearByAttractions(tourGuideService.getUserLocation(getUser(userName)));
    }
          */
    /*
    @RequestMapping("/getNearbyAttractions") 
    public List<Attraction> getNearbyAttractions(@RequestParam String userName) {
    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
    	return tourGuideService.getNearByAttractions(visitedLocation);
    }
       */
    
    
    
    @RequestMapping("/getRewards") 
    public List<UserReward> getRewards(@RequestParam String userName) {
    	return tourGuideService.getUserRewards(getUser(userName));
    }
       
    @RequestMapping("/getTripDeals")
    public List<Provider> getTripDeals(@RequestParam String userName) {
    	return tourGuideService.getTripDeals(getUser(userName));
    }
    
    private User getUser(String userName) {
    	return tourGuideService.getUser(userName);
    }
   

}