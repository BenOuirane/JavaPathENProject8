package com.openclassrooms.tourguide.record;



import gpsUtil.location.Location;

public record NearByAttractionList(
		 Location userLocation,
	     NearByAttraction[] nearByAttractionList
		) {

    
}
