<?php

if (isset($_FILES['picture'])) {
	$fname = $_FILES['picture']['name'];
    if( move_uploaded_file($_FILES['picture']['tmp_name'], "upload/" . $fname)){
    	echo 'upload done';
    	unlink("upload/".$fname);
    }
    else echo 'upload failed';
    exit;
}
else echo 'no file found'
?>