# Mobile application project step 4 report

## Testing strategy
- Unit test: Take a small part of the system (unit), run a function or change something and compare the results (expected vs actual), we tested functionality that is more likely to break
- UI tests: Simulate some common actions the user is likely to do (like creating a routine)
## Build process for APK
- In Android Studio
  - -> Build 
  - -> Generate Signed App
  - -> Choose "APK", press "Next"
  - -> Create or choose a keystore, press "Next"
  - -> Choose release, press "Create"
  - -> Locate the .apk file on the computer

## Known bugs or limitations
- Routines are hard coded, not on the cloud
- There is no point to creating the profile (because there is no cloud)
- User can't change their profile picture
