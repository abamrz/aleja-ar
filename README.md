# AlejaAR - Indoor Guidance System using Augmented Reality 

## Overview 

### What's a AlejaAR?
AlejaAR is an Android application available on API levels 21 (Lollipop)+. It provides navigation service inside the office buildings using Augmented Reality in mobile camera.

Please consider and follow the guidelines below to get to know an application better.

## Requirements
Library and Frameworks                                                      |
----------------------------------------------------------------------------|
ARCore (https://developers.google.com/ar)                                   |
JGraphT (https://jgrapht.org/)                                              |
SQLite (https://www.sqlite.org/index.html)                                  |


## Installation
There are two options to install an app

#### Option 1: Install the release version on the smartphone
The app is developed for Android 7.0 (API Level 24, Nougat) or higher. To use the app you need a smartphone 
that is supported by ARCore (https://developers.google.com/ar/discover/supported-devices#android_play).

To install the app simply download the app/release/app-release.apk file. Since this is not a trusted source, you have to allow the installation for unknown apps and sources.
* On devices running Android 8.0 (API level 26) and higher, users must navigate to the Install unknown apps system settings screen to enable app installations from a particular source.
* On devices running Android 7.1.1 (API level 25) and lower, users must either enable the Unknown sources system setting or allow a single installation of an unknown app.

#### Option 2: Clone this repository and import into your **Android Studio** and follow the steps below

```bash
git@github.com:abamrz/aleja-ar.git
```

## Configuration
### Keystores:
Create `app/keystore.gradle` with the following data:
```gradle
ext.key_alias='...'
ext.key_password='...'
ext.store_password='...'
```
Then place both keystores under `app/keystores/` directory.


## Build variants
Use the Android Studio *Build Variants* button.


## Generating signed APK
From Android Studio:
1. ***Build*** menu
2. ***Generate Signed APK...***
3. Fill in the keystore information *(you only need to do this once manually and then let Android Studio remember it)*

## How to use the app? 
### Step 1: Start the app  

Launch AlejaAR and you will be landed on the following page with some options:

![WelcomeScreen Image](./welcome_screen.png)

### Step 2: Make a new plan

Create a new plan of the floor (to be updated)

### Step 3: Use an existing plan

To be added

**Note**: the steps 1-3 should be followed in strict order during the first launching the app, i.e. should you do not have any plan (graph) in the database, you first create a new plan (step 2).

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


