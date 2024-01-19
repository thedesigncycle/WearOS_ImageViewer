
# Wear OS Image Viewer



This is a simple app to quickly preview images from your phone on your watch. This app was built to preview UI designs on wear os devices that do not have in-built image viewers.




## How to install

- Go to [releases](https://github.com/thedesigncycle/WearOS_ImageViewer/releases) and download the APK files

- Make sure you have Android platform-tools installed on your PC.



***Phone***

Install the `WearOSImageViewer-phone.apk` on the phone using either adb or any file manager.



***Watch***

1. Go to Settings → Developer Options → enable Wireless Debugging



2. On your PC, use the following adb commands to connect the watch & install



	Pair your watch

		`adb pair <ipaddress:port>`

	(Enter pairing code upon prompt)

  


	Then connect your watch

	`adb connect <ipadress:port>`



3. Install apk

   `adb install path/to/WearOSImageViewer-watch.apk`




**Usage**

Open the watch app & phone app.

Select any image on the phone to be instantly displayed on the watch.