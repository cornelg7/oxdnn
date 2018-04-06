//function which show/hide the element with the give id

show = function(elementId) {
	if(document.getElementById(elementId).style.display=='none') {
		document.getElementById(elementId).style.display='';
	} else {
		document.getElementById(elementId).style.display='none';
	}
}