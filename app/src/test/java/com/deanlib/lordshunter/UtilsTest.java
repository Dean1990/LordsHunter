package com.deanlib.lordshunter;

import org.junit.Test;

import static org.junit.Assert.*;

public class UtilsTest {


    @Test
    public void parseText() {
        String s = "ZNQ 獵魔二組 微信群上的聊天记录如下，请查收。\n" +
                "\n" +
                "—————  2018-09-03  —————\n" +
                "\n" +
                "\n" +
                "AAA---楠刺青TATTOO  12:47\n" +
                "\n" +
                "[图片: 196bcfcc8048af78f7cbb9a758d91a48.jpg(请在附件中查看)]\n" +
                "\n" +
                "一梦三四年。  16:27\n" +
                "\n" +
                "[图片: 9f52ff67d7d0a9dcbcd50ee6a88a5a09.jpg(请在附件中查看)]\n" +
                "\n" +
                "一梦三四年。  16:27\n" +
                "\n" +
                "[图片: 94ad7ea71b0751a993f69a50738a7316.jpg(请在附件中查看)]\n" +
                "\n" +
                "一梦三四年。  16:27\n" +
                "\n" +
                "[图片: 36cafa0d6ee13c08f7cd33d8d2895146.jpg(请在附件中查看)]\n" +
                "\n" +
                "都忘了  18:41\n" +
                "\n" +
                "[图片: 782d99d36a252775aebac8f38cf5bb68.jpg(请在附件中查看)]\n" +
                "\n" +
                "都忘了  18:41\n" +
                "\n" +
                "[图片: 6b4f69b46ec6d5d723da3dc4be500767.jpg(请在附件中查看)]\n" +
                "\n" +
                "Dean  18:44\n" +
                "\n" +
                "不到千万吗？\n" +
                "\n" +
                "G  (一刀）  18:44\n" +
                "\n" +
                "你俩是不是一个人\n" +
                "\n" +
                "都忘了  18:44\n" +
                "\n" +
                "我猎魔发展不好，打不了太多\n" +
                "\n" +
                "Dean  18:44\n" +
                "\n" +
                "嗯\n" +
                "\n" +
                "—————  2018-09-04  —————\n" +
                "\n" +
                "\n" +
                "Dean  00:44\n" +
                "\n" +
                "9月3日，总共6人上传猎魔记录，1只二级，14只一级\n" +
                "\n" +
                "Dean  00:45\n" +
                "\n" +
                "再接再厉\n" +
                "\n" +
                "Dean  00:45\n" +
                "\n" +
                "[图片: d4e6aa20f1c0e8d0a7d71daf59fc0446.jpg(请在附件中查看)]\n" +
                "\n" +
                "Dean  00:45\n" +
                "\n" +
                "[图片: 3c172f3e4efa4258ad3eb88ebc237aab.jpg(请在附件中查看)]\n" +
                "\n" +
                "Dean  00:45\n" +
                "\n" +
                "[图片: d0aabfafc02a7641d71eaca1864d1edc.jpg(请在附件中查看)]\n" +
                "\n" +
                "Dean  13:17\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n";

        Utils.parseText(s);
    }
}