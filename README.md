# GLXY

A simplified gravity simulator for Android.

Built with [libgdx](http://libgdx.badlogicgames.com/).

# Building

## Eclipse
If you have [Eclipse with the ADT Plugin](https://developer.android.com/tools/sdk/eclipse-adt.html), you can
just [download this zip](https://github.com/KaeruCT/glxy-android/archive/master.zip), extract it,
import the projects inside into Eclipse and then export an APK.

## Ant
You can also build the application with Apache Ant.
To build, you will need to have the [Android SDK](https://developer.android.com/sdk/index.html)
and [Apache Ant](https://github.com/apache/ant) installed.

1. Clone the repo:

   `$ git clone https://github.com/KaeruCT/GLXY.git`

2. Go into the android project directory:

   `$ cd GLXY/glxy-android`
   
4. Run `android update project`:
   
   `$ android update project --path .`

3. Run Ant:

   `$ ant release`

4. Sign the resulting APK file:
   `$ jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore my-release-key.keystore bin/GLXY-release-unsigned.apk alias_name`

5. Install the APK to your device:

   You can do this manually, or with [adb](http://developer.android.com/tools/help/adb.html):

   `$ adb install bin/GLXY-release-unsigned.apk`

# Controls

## Touch gestures supported:
- Single tap: Create a new particle.
- Double tap: Put camera focus on a particle.
- Pinch: Zoom the view.
- Pan: If panning is enabled, it will pan the view.
       Otherwise, it will create a particle with momentum.

## Option bar:
- Small, Medium, Large buttons: Choose a particle size.
- Pan button: Toggle pan/slingshotting particles.

## Settings menu (cog icon):
- Pause: Toggle pause.
- Particle trails: Show / don't show.
- Collision mode: Bounce / merge particles.
- Reset zoom: Resets the zoom to default.
- Reset particles: Removes all the particles from the field.

# Screenshots
![1.png](http://kaeruct.github.io/galleries/glxy-android/1.png)
![2.png](http://kaeruct.github.io/galleries/glxy-android/1.png)
