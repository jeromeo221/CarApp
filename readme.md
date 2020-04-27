# Car Application Mobile
Mobile application for Android to maintain fuel transactions of a certain vehicle in order to determine the efficiency and cost.

## Installation
Git clone the repository to a target directory.
The following files are needed in order to run the application which are not included in the repository:
* `gradle.properties` - Contains the grade general properties including the API URLs.
* `keystore.properties` - Contains keystore signature properties.
* `*keystore.jks` - Keystore file generated in Android Studio.

The `grade.properties` API URLs should contain the following fields:
* `TRIAL_SERVER_URL`
* `STAGE_SERVER_URL`
* `PROD_SERVER_URL`

The `keystore.properties` should contain the following fields:
* `storeFile`
* `keyAlias`
* `keyPassword`
* `storePassword`

## Features
The application can do the following:
* Add a vehicle
* Edit a vehicle
* Delete a vehicle
* Displays available vehicles
* Add a fuel transaction
* Edit a fuel transaction
* Delete a fuel transaction
* Displays available fuel transaction for a particular vehicle

## Future Plans
* Add feature to edit account and user details
* Modify and improve refresh token logic
