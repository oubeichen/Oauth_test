Reference

VK:

https://github.com/VKCOM/vk-android-sdk
http://vkcom.github.io/vk-android-sdk/
https://vk.com/dev/standalone

APIOK:

http://apiok.ru/wiki/display/api/Odnoklassniki+Mobile+API
http://apiok.ru/wiki/display/api/Android+application

You must change SERVER_URL in the MainActivity.java
default is:
"http://192.168.1.101:8899/Oauth_test_server/Getusername"

And the client usage is:

/Getusername?authtype=vk&
token=XXXXXXXXXXX&secret=XXXXXXXX

/Getusername?authtype=ok&
token=XXXXXXXXXXX
