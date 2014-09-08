RxMapsAndroid
=============

This project is a generic maps client for Android built on RxJava.

The purpose of this project is not be a competitor for other map clients and map SDKs, but to illustrate usage of RxJava in a real life scenario.

Technology stack
---------

Currently there are not so many frameworks in use:

* RxJava
* OkHttpClient


Tile server
--------------------

You can see the tile server end point in Configuration.java. The format is currently a little simplified and assumes the order of <zoom>, <x>, <y>. However, as this the project is not in the form of a library yet anyway, you can create your own MapNetworkAdapter if the format does not work with your preferred tile server.

The default tile server is set to [MapQuest-OSM tiles](http://developer.mapquest.com/web/products/open/map)

    // This is default configuration, replace with a desired one
    public static final String MAP_TILE_URL = "http://otile1.mqcdn.com/tiles/1.0.0/map/%d/%d/%d.jpg";
