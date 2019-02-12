# voice-velocity-demo

上传该示例的原因是：sonic原示例只有解析wav文件然后直接播放的示例，但是有些情况我们希望得到一个pcm或wav文件。<br/>
本示例提供原pcm(*.raw)及wav音频文件读取并输出为pcm(*.raw)及wav音频文件的示例给大家作为参考，同时也提供了对wav文件的处理方法。

### 目录结构
* Sonic.java
* Main.java
* WavFormatHead.java

1. Sonic.java
该文件是Java版的音频变速、变调库，只有一个文件。<br/>
最初需要用到音频变速不变调的库时，找到的几乎都是C/C++版本的音频库，最后才找到这个Java库<br/>
该文件源repositories：[sonic](https://github.com/waywardgeek/sonic)

2. Main.java
程序入口，原Sonic示例只有解析wav文件然后直接播放的示例<br/>
本示例中增加了原pcm(*.raw)及wav音频文件读取并输出为pcm(*.raw)及wav音频文件的示例

3. WavFormatHead.java
wav文件头处理类，在生成wav文件时需要加上对应属性的wav头，里面对每个字段有详细的解释。