<img src="https://github.com/justjohn/transitwidget/raw/master/artwork/transit.png" align="right" width="128" height="128" />
<a href="http://play.google.com/store/apps/details?id=com.transitwidget">
  <img alt="Android app on Google Play"
       src="http://developer.android.com/images/brand/en_generic_rgb_wo_45.png" />
</a>

## TransitWidget

This project is devoted to the development of transitwidget, an open source android
app that displays bus stop prediction information from NextBus. Deploy the transitwidget 
to your home screen, then configure the agency, route, stop that you are interested in. 
TransitWidget displays the time the next bus is predicted to be at that stop.

### Building TransitWidget

In order to build TransitWidget you need to setup the Android projects of both 
TransitWidget and it's dependencies for your machine. This involves running the provided
`init.sh` script or `android update project` on the project directory and all libraries
in `deps`.

Once you've updated the projects you can build by running

    ant clean debug

The resulting APK will be: `bin/transitwidget-debug.apk`

## Acknowledgments

TransitWidget uses the excellent [HoloEverywhere](https://github.com/ChristopheVersieux/HoloEverywhere) and [ActionBarSherlock](http://actionbarsherlock.com/) libraries.

## License

TBD