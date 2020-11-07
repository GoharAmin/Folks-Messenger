// MyAidl.aidl
package codegradients.com.chatapp;

// Declare any non-default types here with import statements

interface MyAidl{
 	void registerScreenShotObserver();
 	void unregisterScreenShotObserver();
 	void setScreenShotEnable(boolean enable);
 }
