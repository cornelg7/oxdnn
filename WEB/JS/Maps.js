//this file will contain the code to handle the Google Maps API

//create map

var map;
var geocoder;
var marker;

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
        marker = new google.maps.Marker({
            map: map,
            position: results[0].geometry.location
        });
      } else {
        alert('Geocode was not successful for the following reason: ' + status);
      }
}

//get address and send request
var showOnMap = function () {

	var locationString = document.getElementById('address').value;
	geocoder.geocode({ 'address': locationString}, geocoderCallBack);
	
}
