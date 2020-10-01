# AlejaAR - Indoor Guidance System using Augmented Reality 

## Overview 

### What's a AlejaAR?
AlejaAR is an Android system application available on API levels 21 (Lollipop)+. It provides navigation service inside the office buildings using Augmented Reality in mobile camera.

Please consider and follow the guidelines below to get to know an application better.

## Requirements
Library and Frameworks                                                      |
----------------------------------------------------------------------------|
ARCore (https://developers.google.com/ar)                                   |
JGraphT (https://jgrapht.org/)                                              |
SQLite (https://www.sqlite.org/index.html)                                  |


## Installation
Clone this repository and import into your **Android Studio**

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
And place both keystores under `app/keystores/` directory:
- `playstore.keystore`
- `stage.keystore`


## Build variants
Use the Android Studio *Build Variants* button.


## Generating signed APK
From Android Studio:
1. ***Build*** menu
2. ***Generate Signed APK...***
3. Fill in the keystore information *(you only need to do this once manually and then let Android Studio remember it)*

## Developers
This project has been implemented by:
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