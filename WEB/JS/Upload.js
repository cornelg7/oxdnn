//this file contains the code to handle the upload

//-- HANDLE FILES -----------------------------------------

//handle picture by displaying thumbnail and adding properties
const handlePicture = function(files) {
	
	var previewDiv = document.getElementById("dropZone")
	
	//get and display thumbnails
	for (var i =0; i< files.length; i++) {
		var file = files[i]
	
		//create <img> to place the thumbnail
		var image = document.createElement("img")
		image.file = file;
		image.classList.add("toBeUploaded") //this class will be used to select the file that needs uploading
		
		previewDiv.appendChild(image)
		
		var reader = new FileReader()
		reader.onload =( function( Img) { return function (event) {Img.src = event.target.result;}; }  ) (image)
		reader.readAsDataURL(file)
		
	}
	
}

var count = 0; //global variable which counts the uploaded files

//function to upload a single file
function FileUpload(image, file, total) {

	var xhr = new XMLHttpRequest(); //object to send the data
	var formData = new FormData();
	
	formData.append('picture', file);//add prefix to file
	
	//create HTML elements to show progress
	var progressDiv = document.getElementById("progressDiv"); //get div where progress is to be showed
	var spanFile = document.createElement("span"); //create <span> element
	spanFile.style.display = 'block';
	this.bar = document.createElement("progress") //create <progress> element
	var os = window.navigator.platform; //the style of <progress> changes if on windows or mac
	if(os.search("Mac") != -1) this.bar.style = 'display: inline; position: relative; top: 0.25rem;'
	else this.bar.style = 'display: inline; position: relative; top: 0.1rem;'
	this.bar.value = '2';
	this.bar.max = '100';
	spanFile.appendChild(this.bar);
	this.p = document.createElement("p"); //create <p> element
	this.p.style = 'display: inline; margin-left: 1rem;'
	this.p.innerHTML = " uploading "+file.name;
	spanFile.appendChild(this.p);
	progressDiv.appendChild(spanFile);
	this.spanFile = spanFile;
	
  	var self = this;
  	
  	//when the upload progress
	xhr.upload.addEventListener("progress", function(event) {
    		if (event.lengthComputable) {
          		var percentage = Math.round((event.loaded * 100) / event.total);
          		self.bar.value = percentage.toString();
        	}
    	}, false);
  	
  	
  	//when the upload is complete
	xhr.upload.addEventListener("load", function(e){
			count++;
			if(count == total) { //when all images are uploade
				document.forms[0].reset(); //this should reset the form (because the file are uploaded)
				count = 0;
				//document.getElementById("progressDiv").innerHTML = '';
			}
    		self.bar.value='100';
    		var text = self.p.innerHTML
    		text = text.replace("uploading", "uploaded")
    		self.p.innerHTML = text;
    		//self.spanFile.style.display='none'
    	}, false);

    
    //if something goes wrong
    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status != 200) { //4 means DONE; 200 means SUCCESS
            alert("something went wrong: "+xhr.responseText+" server status: "+xhr.status);
        }
        if (xhr.readyState == 4 && xhr.status == 200) {
			//write the response into a <p></p> in the outputDiv
        	var paragraph = document.createElement("p")
			paragraph.innerHTML = xhr.response;
			document.getElementById('outputDiv').appendChild(paragraph)
			
			image.classList = ["Uploaded"] //so we know this file was uploaded
        }
    };
    
    //here we open the request to upload, the second argument is the .php file to call on the server
	xhr.open("POST", "/upload", true);
	xhr.send(formData);

}



// function which individually sends each picture


const sendFiles = function () {
	 
	var images = document.querySelectorAll(".toBeUploaded") //get all pictures

	var imagesNumber = images.length;
	//upload each picture
	for (var i =0; i< imagesNumber; i++) {
		new FileUpload(images[i], images[i].file, imagesNumber)
	
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

