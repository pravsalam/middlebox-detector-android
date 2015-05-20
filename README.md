# middlebox-detector-android

Source code can be imported from github to eclipse workspace.  Since some part of the code is written in “C”, it needs ndk integration into Eclipse for successful compilation of the native code. Information about integrating android ndk is available on internet.  Current application is tested with Android api 21. 

compiling the source code produces app which can be run on any android device. Along with Android application, source code also produces an executable “tcptest” from the native code. Android application requires this executable to perform the traceroute test to identify approximate location of middleboxes. 

To run the “tcptests” android phone should be rooted. details about the tcptests are explained in the design section.  Current implementation requires that “tcptests”  should be kept in “/data/tmp/” folder.  This can be achieved as below.
Connect an android phone to the computer
Go to the abdtools folder inside android sdk and execute following command. [ Assuming your eclipse workspace is in Documents folder.
/adb push ~/Documents/workspace/middleboxes/obj/local/armeabi/tcptests /sdcard/middlebox/ Since adb push does not allow to copy a file directly to /data/ folder, it must be copied to a temporary folder and then copied to /data/tmp
./adb shell and run sudo
cp /sdcard/middlebox/tcptests /data/tmp
Once the executable is copied into desired location, application can be run on android phone. Since tests are performed as a batch, it takes sometime for the results to appear on the screen. Results are posted on a listview of Android. Result on the top is the latest result

