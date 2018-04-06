//this file contains the code to handle the upload

//-- HANDLE FILES -----------------------------------------

const handlePicture = function(files) {
	
	var previewDiv = document.getElementById("dropZone")
	
	//get and display thubnails
	for (var i =0; i< files.length; i++) {
		var file = files[i]
	
		var image = document.createElement("img")
		//image.classList.add("obj")
		image.file = file;
		previewDiv.appendChild(image)
		
		var reader = new FileReader()
		reader.onload =( function( Img) { return function (event) {Img.src = event.target.result;}; }  ) (image)
		reader.readAsDataURL(file)
		
	}
	
}



//---------------------------------------------------------

//-- SECURITY CHECK ---------------------------------------
const MaxFileSize = 10485760; //10 MB

//this function checks the file which are being uploaded
let check = function (files) {

	var regex = /^(image\/)(gif|(x-)?png|p?jpeg)$/i;

	if (files.length === 0) {
		alert('No file to upload')
		return false//end the function call
	}
				
	for (var i=0; i < files.length; i++) { //check each uploaded file
		if( files[i].size >= MaxFileSize){
			alert('File size: '+ files[i].size + 'B; - File too big')
			return false
		}
		else if( files[i].type.search(regex) == -1) {
			alert('File type: '+files[i].type+' - File type not allowed, upload a picture')
 			return false
  		}		
        			
	}
	
	//alert('Uploading done successfully')
	return true
};




//now call the check function whenever something is submitted through the form
document.forms[0].addEventListener('submit', function (event) { if(!check(document.getElementById('fileUploading').files)) {event.preventDefault} else {handlePicture(document.getElementById('fileUploading').files)}}, false );

//--------------------------------------------------------------





//-- DRAG AND DROP SECTION ---------------------------

//functions
//prevent browser from doing anything when dragging
const dragenter = function(event) {
	event.stopPropagation()
	event.preventDefault()
}

const dragover = function (event) {
	event.stopPropagation()
	event.preventDefault()
}


//now do something when dropping
const drop = function (event) {
	event.stopPropagation()
	event.preventDefault()
	

	
	var transferData = event.dataTransfer
	var files = transferData.files;
	
	if( !check(files)) return //security check failed
	
	handlePicture(files)
}





//here we set our drag and drop zone
var dropSection;

dropSection = document.getElementById("dropZone") //retrive the html element
//set listeners
dropSection.addEventListener("dragenter",dragenter,false)
dropSection.addEventListener("dragover",dragover,false)
dropSection.addEventListener("drop",drop,false)


//---------------------------------------------------------


