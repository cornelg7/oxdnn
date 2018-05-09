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
		image.setAttribute("data-toggle", "tooltip");
        image.setAttribute("title","Click picture to remove it")
		previewDiv.appendChild(image)
		
		image.addEventListener('click', new delElem(image))
		
		var reader = new FileReader()
		reader.onload =( function( Img) { return function (event) {Img.src = event.target.result;}; }  ) (image)
		reader.readAsDataURL(file)
		
	}
	
	$('[data-toggle="tooltip"]').tooltip()
}

var count = 0; //global variable which counts the uploaded files

function delElem(element) {
	return function(e) {
		element.parentNode.removeChild(element)
		var tooltips = document.querySelectorAll(".tooltip")
		for (var i =0; i<tooltips.length; i++) {
			tooltips[i].parentNode.removeChild(tooltips[i])
		}
	}
}


function createXHR(image,name,total) {
	
	var xhr = new XMLHttpRequest(); //object to send the data
	xhr.responseType = "blob" //force the response to be interpreted as a Blob (binary)

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
	this.p.innerHTML = " uploading "+name;
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
				var dropZone = document.getElementById("dropZone");
				dropZone.innerHTML = ""
			}
    		self.bar.value='100';
    		var text = self.p.innerHTML
    		text = text.replace("uploading", "uploaded")
    		text = text.concat( " ... analysing picture")
    		self.p.innerHTML = text;
    		//self.spanFile.style.display='none'
    	}, false);
    	
    //handle response
    xhr.onreadystatechange = function() {
    	
        if (xhr.readyState == 4 && xhr.status != 200) { //4 means DONE; 200 means SUCCESS
        	var string = "unknown issue";
        	try {
        		string = xhr.responseText
        	} catch (Exception e) {
        		
        	}
            alert("something went wrong: "+string+" server status: "+xhr.status);
        }
        if (xhr.readyState == 4 && xhr.status == 200) {
			//write the response into a <p></p> in the outputDiv
			
			var reader = new FileReader();
			var output = document.createElement("img") //need to scale the image to some max-width and max-height
			output.style = 'max-width: 100%; max-height: 1080px; margin-right: 1rem; margin-bottom: 1rem; background-color: grey'
			reader.onload =( function(Img) { return function (event) {Img.src = event.target.result;}; }  ) (output)
			reader.readAsDataURL(xhr.response)
        	var text = self.p.innerHTML
            text = text.replace("... analysing picture"," done - find result below")
	        text = text.replace("uploaded", "")
            self.p.innerHTML = text
			document.getElementById('outputDiv').appendChild(output)
			document.getElementById('outputDiv').style.display='' //ensure its visible
			
			image.classList = ["Uploaded"] //so we know this file was uploaded
        }
    };

	return xhr;
}


//function to upload a single file
function FileUpload(image, file, total) {

	var xhr = new createXHR(image,file.name,total)
	var formData = new FormData();
	
	formData.append('picture', file);//add prefix to file
    
    //here we open the request to upload, the second argument is the .php file to call on the server
	xhr.open("POST", "/upload-pic-pic", true);
	xhr.send(formData);

}

function UrlUpload(image, url, total) {

	var xhr = new createXHR(image,'Google image' ,total)
    
    xhr.open("POST", "/upload-url-pic", true);
    
    //remove irrelevant part of the link
    var pos = url.search("/p/")
    var shortUrl = url.slice(pos+3)
    console.log(shortUrl)
    xhr.send(shortUrl);
}


// function which individually sends each picture
const sendFiles = function () {

	document.getElementById('outputDiv').innerHTML = "<h3> Result : </h3>" //get rid of old images
	document.getElementById("progressDiv").innerHTML = ''; //remove progress bar
	 
	var images = document.querySelectorAll(".toBeUploaded") //get all pictures

	var imagesNumber = images.length;
	//upload each picture
	for (var i =0; i< imagesNumber; i++) {
		new FileUpload(images[i], images[i].file, imagesNumber)
	
	}
	
	var urls = document.querySelectorAll(".urlToUpload")
	
	var urlsNumber = urls.length
	
	for(var i =0; i< urlsNumber; i++) {
		new UrlUpload(urls[i],urls[i].src,urlsNumber)
	}
}


//---------------------------------------------------------

//-- SECURITY CHECK ---------------------------------------
const MaxFileSize = 10485760; //10 MB

//this function checks the file which are being uploaded
const checkFile = function (file) {

	var regex = /^(image\/)((x-)?png|p?jpeg)$/i;
				
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

