# AlejaAR - Indoor Guidance System using Augmented Reality 

## Overview 

### What's AlejaAR?
AlejaAR is an Android application, that provides navigation service inside a building using Augmented Reality.

Please consider and follow the guidelines below to get to know the application better.


## Installation
There are following options to install the app.

#### Option 1: Install the release version on the smartphone
The app is developed for Android 7.0 (API Level 24, Nougat) or higher. To use the app you need a smartphone 
that is supported by ARCore (https://developers.google.com/ar/discover/supported-devices#android_play).

To install the app simply download the app/release/app-release.apk file. Since this is not a trusted source, you have to allow the installation for unknown apps and sources.
* On devices running Android 8.0 (API level 26) and higher, users must navigate to the Install unknown apps system settings screen to enable app installations from a particular source.
* On devices running Android 7.1.1 (API level 25) and lower, users must either enable the Unknown sources system setting or allow a single installation of an unknown app.

#### Option 2: Clone this repository and build your own APK using **Android Studio**

```bash
git@github.com:abamrz/aleja-ar.git
```

##### Configuration to build a release APK
###### Keystores:
Create `app/keystore.gradle` with the following data:
```gradle
ext.key_alias='...'
ext.key_password='...'
ext.store_password='...'
```
Then place both keystores under `app/keystores/` directory.


###### Build variants
Use the Android Studio *Build Variants* button to select release build.


###### Generating signed APK
From Android Studio:
1. ***Build*** menu
2. ***Generate Signed APK...***
3. Fill in the keystore information *(you only need to do this once manually and then let Android Studio remember it)*

## How to use the app? 
### Step 1: Start the app  

Launch AlejaAR and you will be landed on the following page with some options:

![WelcomeScreen Image](./welcome_screen.png)

From here you can choose between navigation (Use an existing plan) and making an own new plan of a specific place/floor (Make a new plan). 
On navigation you can search for a desired destinations or simply look around and inspect the labeling of all information points saved in the map.
When making a new plan you can build up a graph and add information to points of interest.

The two options are described in detail in the following parts.

### Step 2: Make a new plan

* Click **Make Plan** button on the start screen
* This is the screen to make a new plan:
![MakePlanScreen Image](./makeplan_screen.png)
TODO Image

#### Functionality of the buttons
* Add to Branch: Adds a new node to the graph at the current position of the smartphone. This node is connected to the previous added node.
* New Branch: Adds a new node to the graph at the current position of the smartphone. This node is connected to the closest edge. The connection point to the edge is shown by a red ball.
* Close Circle: Adds an edge from the last added node to the point at the closest edge which is again identified by a red ball.
* Set Attributes: Opens a dialog to change the attributes to the closest node to the current smartphone position. The attributes are
    - the label: title of the information at this point (e.g. "office of christian")
    - the type: the kind of object the point refers to (e.g. kitchen). The type waypoint is used for points that are only used to navigate along, put should not provide any information.
    - the description: only available, when the type is "office". Can be used to add some more information to the use of the office or similar.
* Save Graph: A dialog is shown to save the graph with an arbitrary name that can be chosen by the user. By this name the graph can later be loaded to navigate on.
* Undo: This option deletes the last added node.

### Step 3: Use an existing plan

* After you are landed at **Use Existing Plan** page, you have to choose desired plan saved before
* Choose the plan (map) from the list
* Let the AlejaAR to navigate!

* TODO Image

**Note**: the steps 1-3 should be followed in strict order during the first launching the app, i.e. should you do not have any plan (graph) in the database, you first create a new plan (step 2).


## Used Libraries and Frameworks
Library and Frameworks                                                      |
----------------------------------------------------------------------------|
ARCore (https://developers.google.com/ar)                                   |
JGraphT (https://jgrapht.org/)                                              |
SQLite (https://www.sqlite.org/index.html)                                  |


## Developers
This project has been implemented during itestra Coding Camp 2020 by:
* [Antonia](https://github.com/antschum)
* [Lukas](https://github.com/thenxmetti)
* [Erik](https://github.com/TheStealthReporter)
* [Jakob](https://github.com/j-stoll)
* [Aba](https://github.com/abamrz) 


## Contributing

1. Fork it
2. Create your feature branch (git checkout -b my-new-feature)
3. Commit your changes (git commit -m 'Add some feature')
4. Push your branch (git push origin my-new-feature)
5. Create a new Pull Request


