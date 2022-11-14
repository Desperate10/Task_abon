![Logo](https://play-lh.googleusercontent.com/voqKaNzcpofh-LJe-uTxzXvEKTGCRbt3R5USQZ-T5XebA7E0AqVH8cm79G7g-130F1A=w240-h480-rw) 
<a href="https://play.google.com/store/apps/details?id=ua.POE.Task_abon"><img alt="Get it on Google Play" src="https://github.com/Desperate10/Task_abon/blob/master/release/Google_Play-Badge-Logo.wine.png"/></a>

# Task_abon
Application for PoltavaOblEnergo controllers.
Using the application, they can record indicators, mark the status of the counter, add and change user data.


## Tech Stack

Kotlin, Room, Coroutines, Flow, Hilt, Retrofit


## How to use

To install the application, click on the google play icon above or download the apk in the release folder. You also need to download a test bypass sheet. It is located in the same place, the file name is E0900_361992 (2).xml. And so, we launch the application and see a blank screen with a button offering to download a bypass sheet.
After clicking it, we find the bypass sheet downloaded earlier and select it. We see that we have a list item on the screen

![First_screen](https://lh3.googleusercontent.com/pw/AL9nZEWTjqHYH7xagXjMErtOPM36KC0HzE_TeJCTYwpkY2GRpvCi6vOILJ1IMJYdhTuRyjMueip_DsYMlqZDU8fxmVL-mQNmVliiyxb4hdDK5CqFzlVyUD2IOAoores32bPhwGSx_H8ccy_Sir6c2Yvo3FBx=w252-h532-no?authuser=0)
![sECONDSF](https://lh3.googleusercontent.com/pw/AL9nZEW_oOR7hvlrDxYXf5IfMqw4CNQTTZXCPElNbcApgnDUBQUV1nKACmBxUX6fDKTiW98TvqIlhSLEZQgjX5nz2vgwkmFcWsNJHkxwF4TXQ_u6-KdgbffamVXh5CAAL9nlXbJXNvoih7JTVXLXIDi5fhFu=w252-h532-no?authuser=0)

Click on this sheet and see a list of all consumers that are included in this bypass and brief information about each of them.
To avoid questions about the icons on the screen, there is a menu at the top right. You can see what it means.

![Task_Detail](https://lh3.googleusercontent.com/pw/AL9nZEV2SOdBjV0WXzeO88IuyQw_WEEtLyHSyrsCXpSE6ydEwy7p3rol4tymObdBoNQGxUnUeJwsOxcy3siRo0VH7kZjQLW6JsPrRKKBJ8tKxk4ycwap6hvl8Ln_pyoNVcLJG5EyqxFnJJTIf0l4BPnrsUTf=w252-h532-no?authuser=0)
![icon_info](https://lh3.googleusercontent.com/pw/AL9nZEVqVnlzVZbfpFGl1XKPGzOF8JTGTo22TjdPTbRreDA4UJAH6cY-IBSV-2HwgpNT5kwMTq4o7P3oVZZFfO9LqS0w3eOJiHTi31Q9XFIc7w34KS2sBsryLixw-wqqNSTnff3zh9QAkJIRCVNaA3pg4H4L=w255-h532-no?authuser=0)

On the screen above you can see the "Search" button. Here you can filter the list by the specified parameters by list. There are pre-prepared data from the original list, or you can enter your own search text

![Find_user](https://lh3.googleusercontent.com/pw/AL9nZEUdktlpjGJ_NDP__sX8PIv8trv6-S5LpnlU_zbO2Gc2J8vaoy8qHxzdP6r0tn09_Tw_w_40aqT7sDkhhU-F0D6HRfGLIinpw7xMFTltfxNi7UAudq_lT_WDlLjiScqEhi3A0jEGnZQhoEX4Xhoyt_5b=w255-h532-no?authuser=0)
![After_filter](https://lh3.googleusercontent.com/pw/AL9nZEVsd7Qy8a6iXd7O1Jve-0daa81O2jhm1xWqwdfn9xeUDRb_6TDJbQ6oOOKRTfcROqOOQL9ZEwiEh-H69b36rSr5_8JZdUOfpzzgUw864Wwxa0AJzwTYhiU9Qr4l-wgxC_DkTfiuUqucsXnmYv35Z38e=w255-h532-no?authuser=0)

We click on the desired consumer, we see all the information about him. By clicking on the icons, you can see the description

![User_Info screen](https://lh3.googleusercontent.com/pw/AL9nZEXQYAd-oFhbdQh28YUHnrlhLHCkVyvAgT1W3rV8aRjlPF0PDK104nfihZ7hipjn1Jyrk5QtLz2wX7R2D9OaMQZMuMD5iZ3PBuUT5_wn5PaC-CEkvsl_VOtkqXmF-EOFrTqWn7KjuLw07eVrbYwDPZJv=w255-h670-no?authuser=0)
![icon_info2](https://lh3.googleusercontent.com/pw/AL9nZEXgiKIgiNtkQcE9rp3iBI3PbkjCVR3agrf1MuU8FCoJIsxF27GBMIRwbcucJIOmcNoqk1-hZJwuLZeByRvCeaRTfH24k7ABkTgZ9un5AuGm26APF38jX4r4Eiw43F7XFA-48HnAfU1bphPRaoKkeiFy=w255-h532-no?authuser=0)

This is the main function screen with which the controller works. In the results tab, he enters all possible new information about the consumer (metrics, bypass status, data source, new mobile phone, etc.). It is possible to take a photo of the counter. 
To switch between consumers, it is not necessary to go to the previous menu, you can use the "Forward" and "Back" arrows. There is also a drop-down list for viewing financial, technical and any other information, for example, to verify the seal number.

![Save_data](https://lh3.googleusercontent.com/pw/AL9nZEX7SEXzsuD10U-oaIyplVNr5kZz0c-efORH_9keBfHchJT0RS32H1rJsxvdT61qxEPzm9ubd2vPyjhmBy1z4gdFt5y_0Qv_6vcsLH-CLgS9gxioThk9hBwjh9jN82nfdJhL6jsIDNjEnRYp_hlHwtiq=w255-h532-no?authuser=0)
![User_Info_fields](https://lh3.googleusercontent.com/pw/AL9nZEUzzzb6gcwNgev1eNc7qiUKEMQCkZ8uQITqQHkfUWo8-i_BttsRdm7t8M54Vw358GAoOIIV3Lp4j9-59LIT4G0VBccRYUKA4JA6XI4G0xQOltUitoGFNZVpWOpbj8oLCLe2FTkTsLGioB7jUsio_Tdy=w255-h532-no?authuser=0)
![Tech_Info](https://lh3.googleusercontent.com/pw/AL9nZEW8l_cGaznjsvOWHCffoOvI99sTc0xsoQyLz-iMHn5EfI7lWSK31dRvDxY9Q1P2Q6bxi99BOvRR_bc6dBja7SQvv_rrEJ00R4z3lpUZXxSpk7lvlN4lRy5wGDRkRXXakOK4fulFhtO9gWRG8x1IucHK=w255-h532-no?authuser=0)

After all the data for all consumers are filled in, you need to return to the start screen and after a long press on the element, click "Unload sheet". An xml-file with data is formed. Which, as a result, is manually loaded into the billing. Photos are uploaded to the server with retrofit. Controllers use Telegram to receive and upload files from the administrator.

![Upload_sheet](https://lh3.googleusercontent.com/pw/AL9nZEVOiJ8PvsJYq9_LO8XPIi-1CXCh5SKPwbRYvF6rnio7Rm0LwbyujDj8Yi7_nOt485XY34Ak7DtfwGwLUG479UrYsIljH-1r36jDCtbNTmsTgM24TUWFA6G6WrPrG-Abi99JPs4tRAtQsTDvxazvDXFj=w255-h532-no?authuser=0)


## FAQ

#### Is this the finished version of the application?

No, the application is being refactored (see the "refactoring" thread)

#### Why is the data not taken directly from a remote server, but do you have to upload the file to the device manually?

Company policy. There are things that could be done more conveniently, but there is how it is

## 🚀 About Me
Hi, my name is Yevhenii. I'm 28 years old developer from Poltava, Ukraine. Currently working in PoltavaOblEnergo. For the last couple of years, I've been mainly developing apps for Android. And I'm currently looking for a job in this field. If you need a good developer, with a desire to improve and learn - write to any communication channel. Don't forget to check out the resume at the link below. Thank you for your attention

![image](https://user-images.githubusercontent.com/25912592/201657532-5fd6c065-f700-476b-a5df-708fcbae86c5.png) [Desperate10](https://t.me/Desperate10)

![image](https://user-images.githubusercontent.com/25912592/201657233-c885914c-13c5-405b-aafb-5b61e4ae0f0c.png) gromov.zhenya@gmail.com

![image](https://user-images.githubusercontent.com/25912592/201657402-feb61deb-cfbf-447b-9e63-3931bd6349cf.png)
[LinkedIn](https://www.linkedin.com/in/%D0%B5%D0%B2%D0%B3%D0%B5%D0%BD%D0%B8%D0%B9-%D0%B3%D1%80%D0%BE%D0%BC%D0%BE%D0%B2-6ab8401b2/)

## My Resume

 - [Click to watch](https://awesomeopensource.com/project/elangosundar/awesome-README-templates)




