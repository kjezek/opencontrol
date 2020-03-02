<?php
  $email = $_REQUEST['email'] ;
  $name = $_REQUEST['name'] ;
  $message = $_REQUEST['msg'] ;

  mail( "opencontrol.info@gmail.com", "OpenControl Web From",
    $message, "From: $name, $email" );

?>

<h2>Váš email byl úspěšně odeslán.</h2>