//this file will contain the code to handle the Google Maps API

//create map

var map;
var geocoder;
var placeService;
var marker = null;
var placeID= null;
var radiusSearch = '20';

//initialise map and geocoder
function initMap() {

        map = new google.maps.Map(document.getElementById('GoogleMaps'), {
          zoom: 17,
          mapTypeId: 'satellite'
        });
        
        map.addListener('click',placeMarker);
        geocoder = new google.maps.Geocoder();
        geocoder.geocode({ 'address': 'Department of Computer Science, University of Oxford'}, geocoderCallBack);
		placeService = new google.maps.places.PlacesService(map);
};

//initMap();
//what to do with the response of the geocoder
var geocoderCallBack = function(results, status) {

	if (status == 'OK') {
        map.setCenter(results[0].geometry.location); //center map
        if( marker != null) marker.setMap(null) //remove old marker
        marker = new google.maps.Marker({ //create new marker
            map: map,
            position: results[0].geometry.location
        });
        placeID = [results[0].place_id]; //get place id
        
      } else {
        alert('Geocode was not successful for the following reason: ' + status);
      }
}

//get address and send request
var showOnMap = function () {

	var locationString = document.getElementById('address').value;
	geocoder.geocode({ 'address': locationString}, geocoderCallBack);
	
}

//callback for place request
var handlePlace = function (place, status) {

	if (status == google.maps.places.PlacesServiceStatus.OK) {
		
		var previewDiv = document.getElementById("dropZone")

		if(place.photos == null)  {
			alert('No photos was found at the give location; try using an address')
			return;
		}
		for (var i =0; i < place.photos.length; i++) {
			var photo = place.photos[i].getUrl({'maxWidth': 1080, 'maxHeight': 1080}); //get photo url
			//create photo thumbnail
			var image = document.createElement("img")
			image.src= photo
			image.classList.add('urlToUpload')
			previewDiv.appendChild(image)
			image.addEventListener('click', new delElem(image))
			
		}
		
	} else {
		alert('Google cannot find the place requested: '+ status);
	}
}

function delElem(element) {
	return function(e) {
		element.parentNode.removeChild(element)
	}
}

//get photos from Google
var getPhotos = function () {

	if (placeID == null) return; //no place to query
	var limit = 5
	if (placeID.length < 5) limit = placeID.length
	for (var i =0; i< placeID.length; i++) {
		var request = {
		placeId : placeID[i]
		};
		placeService.getDetails(request, handlePlace);
	}
	

}

//callback for search nearby method
var searchCallback = function (results, status) {
  if (status == google.maps.places.PlacesServiceStatus.OK) {
  	if(results.length==0) {
  		alert('Sorry no place found near that position; try using an address')
  		return;
  	}
  	for (var i =0; i< results.length; i++) {
  		if (isLocality(results[i])) continue; 
  		placeID.push( results[i].place_id)
  	}
  	placeID = results[0].place_id
  }
}

function isLocality(place) {

	var list = place.types;
	
	for (var i =0; i<list.length; i++) {
		if("locality" == list[i]) return true;
	}

	return false;
}

//get placeID from geocode response
var findPlaceID = function(results, status) {

      if (status == 'OK') {
        map.setCenter(results[0].geometry.location);
        
        var request = {
        	location: results[0].geometry.location,
    		radius: radiusSearch
        }
        placeService.nearbySearch(request, searchCallback); //here we look for a place nearby
      } else {
        alert('Geocode was not successful for the following reason: ' + status);
      }
    }

//function which place a marker where the user click
var placeMarker = function (ev) {

	var pos = ev.latLng;
	//console.log(pos)
	marker.setPosition(pos);
	
	geocoder.geocode({ 'location': pos}, findPlaceID); //update placeID
}

//get precision decided by user
var getPrecision = function() {

	radiusSearch = document.getElementById('precision').value

}



