Oculusbot
=========
![alt tag](https://raw.githubusercontent.com/jrmeschkat/oculusbot/master/Oculusbot.jpg)

The idea for this project was to translate the movements of an [Oculus Rift](https://developer.oculus.com/) headset to a real life robot. The robot would move two cameras (one for each eye) and send the images back to the Rift. This would allow the user to look around at the robots location and even have a 3D perspective of the room. The robot is controlled by a [Raspberry Pi 3](https://www.raspberrypi.org/) which is connected to the robot via the GPIO and the two USB-cameras. The Oculus Rift is a DK2 model.

The software consists of three Maven-eclipse projects and is written for Java 1.8:
- OculusbotShared: contains classes which are used by server and client
- OculusbotServer: the server application which runs on the Pi
- OculusbotClient: the client which runs on the computer with the connected Rift

Additional libraries were used to add to the functionality of the software:
- [lwjgl](https://www.lwjgl.org/): OpenGL- and OculusSDK-java-wrappers
- [OpenCV](http://opencv.org/): to capture the video from the webcams
- [Pi4J](http://pi4j.com/): to control the GPIO

