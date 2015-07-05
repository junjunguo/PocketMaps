Sports tracker: is an gps based activity tracker application.


main functions: (functional requirements)
1. monitoring and tracking fitness related activities such as distance:        
   1. walked 
   2. jog
   3. run
   4. ski
   5. snowboard
   6. bike
   7. driving
   8. boat
1. analyze:
   1. calorie consumption.
   2. speed graph
   3. maximum, minimum and average speed


1. can also used in making reverse journey in unfamiliar area without getting lost.
2. map is downloadable, so users can download map and save it on device and use it without internet.
3. save tracked route with format xml, gps ???
   1. can be used for geotag photos: 
      1. i.e: import trackedRoute.gps to Lightroom and auto tag photos without geo infor to geotagged photos (with geo infor). (there are still lots of DSLRs do not have gps function, this is useful for them)
1. sync with Google Fit (third release):
   1. users can synchronize their activities to Google Fit, so activities can be viewed on Google Fit website or any applications with Google Fit API.
1. share on facebook … ???
   1. share tracked route, calorie consumption and speed graph on social media. 
1. offline navigation - route tracker (second release)
   1. with internet google map can easily navigate, but the core of this app is that it can be used without internet. Offline navigation allows user navigate, find, search route without internet. 
   2. Offline route calculation.
1. Switch between Google Map and OSMap (future function)
   1. Google map has a lot of advantages, user should have the opportunity to switch between Google Map and OSMap.




________________


Quality requirements:


1. Reliability:
   1. the app need to run in the background for as long as user did not close it. The app need to be high reliable. 
1. Modifiability & Maintainability: 
   1. the app need to update all the time to satisfy the user’s needs. 
      1. Solution: use models, layers, components, MVC architecture etc to make the app loosely coupled, and easy to modify. 
      2. increase code readability
      3. documentation
   1. the APIs may change over time.
      1. If there is a better API found later, the system can easily change API without much modify of the code.
      2. The system can change between Google Map API and OSMap API without effect on the basic functionality. 
      3. Solution: user abstract factory pattern, use interface to define the communication. 
1. Reusability:
   1. some components can be reused 
   2. Solution: create loosely coupled components.
1. Usability:
   1. consistency
      1. Did I saw this before?
   1. affordance
      1. how do i use it?
   1. feedback
      1. what happened now?
   1. visibility
      1. can I see it?
   1. constrains
      1. why I can not do it?
   1. mapping 
      1. where am I, and where can I go?
   1. Aesthetic and minimal design
   2. Help and documentation
   3. ...


There is possibility to build an IOS version.
