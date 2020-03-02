<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>OpenControl - Skladové hospodářství efektivně</title>
<link rel="stylesheet" type="text/css" href="reset.css" />
<link rel="stylesheet" type="text/css" href="style.css" />
<meta name="description" content="Skladové hospodářství, zoží, příjemky, výdejky, 
      maloobchod, velkoobchod, inventury" />
      
<!-- lightbox support -->
<script type="text/javascript" src="lightbox/js/prototype.js"></script>
<script type="text/javascript" src="lightbox/js/scriptaculous.js?load=effects,builder"></script>
<script type="text/javascript" src="lightbox/js/lightbox.js"></script>
<link rel="stylesheet" href="lightbox/css/lightbox.css" type="text/css" media="screen" />

</head>
<body>
<div id="wrap">
  <div id="content">
    <div class="left float-l">
      <div id="header">
        <div id="logo">
          <h1>OpenControl</h1>
          <div>Skladové hospodářství efektivně</div>
        </div>
        <ul id="nav">
            <?require "menu.php"?>        
        </ul>
            <?require "footer.php"?>        
      </div>
    </div>
    <div class="right folat-r">
      <div id="right-content">
        <div id="top">
            
            <? # Main Content 
            
            $menu = $_REQUEST["menu"];
            
            if ($menu == "home") {
                require "home.php";
            } else 
            if ($menu == "about") {
                require "about.php";
            } else 
            if ($menu == "price") {
                require "price.php";
            } else 
            if ($menu == "download") {
                require "download.php";
            } else 
            if ($menu == "contact") {
                require "contact.php";
            } else 
            if ($menu == "mysql") {
                require "mysql.php";
            } else {
                require "home.php";
            }
            ?>
            
        </div>
      </div>
    </div>
  </div>
  <!-- /content -->
  <!-- /footer -->
</div>

<script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-45091844-1', 'opencontrol.cz');
  ga('send', 'pageview');

</script>

</body>
</html>
