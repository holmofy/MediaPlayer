#飞狐播放器
学安卓4个多月了，写个app练练手。
---
因为是边学边写，所以代码有点乱。
第一遍组件通信基本上都是用广播实现的，代码特别乱，然后边学的过程中接触到了一些第三方库，EventBus，xUtils之类的，第三方库的使用确实改善了代码的结构。


其中主要一个难点就是电视直播，当时在网上找了很多直播源，自己也从“央视影音”抓包看了一下。反正特纠结。。。然后就是用了网上的一堆m3u8直播源，之前准备用安卓sdk中的VideoView播放，但并没有什么软用。安卓支持的视频格式确实少的可怜。然后就找到了Vitamio解码框架。不得不赞叹这解码框架设计的太好了，基本上和安卓SDK中的api一模一样，基本上只需要替换一下包名就行。


以下是软件的使用截图，UI确实有点丑，PS技术比较勉强。。。

歌词界面
---
![image](https://github.com/holmofy/MediaPlayer/blob/master/screenshot/MusicLyric.png)

唱片动画界面
---
![image](https://github.com/holmofy/MediaPlayer/blob/master/screenshot/MusicRecord.png)

主界面
---

![image](https://github.com/holmofy/MediaPlayer/blob/master/screenshot/NetMusic.png)
![image](https://github.com/holmofy/MediaPlayer/blob/master/screenshot/NetVideo.png)
![image](https://github.com/holmofy/MediaPlayer/blob/master/screenshot/SlidingMenu.png)
![image](https://github.com/holmofy/MediaPlayer/blob/master/screenshot/localMusic.png)

歌曲搜索界面
---

![image](https://github.com/holmofy/MediaPlayer/blob/master/screenshot/searchList.png)

视频播放界面
---

![image](https://github.com/holmofy/MediaPlayer/blob/master/screenshot/VideoPlayer.png)
