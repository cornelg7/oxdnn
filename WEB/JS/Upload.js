//this file contains the code to handle the upload

//-- HANDLE FILES -----------------------------------------

//handle picture by displaying thumbnail and adding properties
const handlePicture = function(files) {
	
	var previewDiv = document.getElementById("dropZone")
	
	//get and display thumbnails
	for (var i =0; i< files.length; i++) {
		var file = files[i]
	
		var image = document.createElement("img")
		image.file = file;
		image.classList.add("toBeUploaded") //this class will be used to select the file that needs uploading
		
		previewDiv.appendChild(image)
		
		var reader = new FileReader()
		reader.onload =( function( Img) { return function (event) {Img.src = event.target.result;}; }  ) (image)
		reader.readAsDataURL(file)
		
	}
	
}


//function to upload a single file
function FileUpload(image, file) {

	var xhr = new XMLHttpRequest(); //object to send the data
	var formData = new FormData();
	
	formData.append('picture', file);//add prefix to file
	
  /*  I commented out the code to show upload progress; let's first make the upload work 
  	var self = this;
  	
  	//this says that when the upload progress we update the throbber object
	xhr.upload.addEventListener("progress", function(event) {
    		if (event.lengthComputable) {
          		var percentage = Math.round((event.loaded * 100) / event.total);
          		document.getElementById("progressBar").value=percentage;
        	}
    	}, false);
  	
  	
  	//this says that when the upload is complete we set the throbber to 100% and remove it
	xhr.upload.addEventListener("load", function(e){
    		self.bar.value=100;
    		document.getElementById("progressBar").style.display='none'
    	}, false);
    */
    
    //if something goes wrong
    xhr.onreadystatechange = function() {
                if (xhr.readyState == 4 && xhr.status == 200) {
                    alert(xhr.responseText);
                }
    };
    
    //here we open the request to upload, the second argument is the .php file to call on the server
	xhr.open("POST", "file.php", true);
	xhr.send(formData);
	
	if( xhr.responseText != '') alert(xhr.responseText)
	
	image.classList = ["Uploaded"] //so we know this file was uploaded

}



// function which individually sends each picture
const sendFiles = function () {
	
	var images = document.querySelectorAll(".toBeUploaded") //get all pictures

	alert("images to be uploaded "+images.length);
	//upload each picture
	for (var i =0; i< images.length; i++) {
		new FileUpload(images[i], images[i].file)
	
	}

}


//---------------------------------------------------------

//-- SECURITY CHECK ---------------------------------------
const MaxFileSize = 10485760; //10 MB

//this function checks the file which are being uploaded
const checkFile = function (file) {

	var regex = /^(image\/)(gif|(x-)?png|p?jpeg)$/i;
				
	if( file.size >= MaxFileSize){
		alert('File size: '+ files.size + 'B; - File too big')
		return false
	}
	else if( file.type.search(regex) == -1) {
		alert('File type: '+file.type+' - File type not allowed, upload a picture')
 		return false
  	}		
        			
	
	return true
};


const check = function (files) {

	var newFiles = []
	
	for (var i =0; i< files.length; i++) {
		if(checkFile(files[i])) newFiles.push(files[i])
	}
	
	if(newFiles.length == 0){
		alert('No file is uploaded')
		return;
	}
	
	return newFiles

}

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
	
	files = check(files);
	
	if(files.length ==0) return;
	
	handlePicture(files)
}





//here we set our drag and drop zone
var dropSection;

dropSection = document.getElementById("dropZone") //retrive the html element
//set listeners
dropSection.addEventListener("dragenter",dragenter,false)
dropSection.addEventListener("dragover",dragover,false)
dropSection.addEventListener("drop",drop,false)
document.getElementById("RunButton").addEventListener("click", sendFiles, false)


//---------------------------------------------------------


//-- FORM UPLOAD SECTION -----------------------------------

const formUpload = function (event) {

	var files = document.getElementById('fileUploading').files //get files from form
	
	files = check(files);
	
	if(files.length ==0) return;
	
	handlePicture(files)

}


//make the element call this function
document.getElementById('fileUploading').addEventListener("change", formUpload, false)

//----------------------------------------------------------

