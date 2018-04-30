//this file will contain the code to handle the Google Maps API

//create map

var map;
var geocoder;
var marker = null;
var placeID= null;

//initialise map and geocoder
function initMap() {

        map = new google.maps.Map(document.getElementById('GoogleMaps'), {
          zoom: 17,
          mapTypeId: 'satellite'
        });
        geocoder = new google.maps.Geocoder();
        geocoder.geocode({ 'address': 'Department of Computer Science, University of Oxford'}, geocoderCallBack);

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
        placeID = results[0].place_id; //get place id
        
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
		var photosArray = [];
		for (var i =0; i < place.photos.length; i++) {
			var photo = place.photos[i].getUrl({'maxWidth': 1080, 'maxHeight': 1080}); //get photo url
			//create photo thumbnail
			var image = document.createElement("img")
			image.src= photo
			previewDiv.appendChild(image)
			console.log(photo)
			//photosArray = photosArray.push()
			
		}
		
		
	} else {
		alert('Google cannot find the place requested: '+ status);
	}
}

//get photos from Google
var getPhotos = function () {

	if (placeID == null) return; //no place to query
	
	var request = {
		placeId : placeID
	};

	placeService = new google.maps.places.PlacesService(map);
	placeService.getDetails(request, handlePlace);

}