package org.n3r.diamond.sdk.utils;


import org.n3r.diamond.sdk.domain.DiamondConf;

import java.util.List;
import java.util.Random;


public class RandomDiamondUtils {

    private List<DiamondConf> allDiamondConfs;
    private int retryTimes;
    private int maxTimes;
    private int[] randomIndexSequence;
    private int currentIndex;

    public RandomDiamondUtils(List<DiamondConf> diamondConfs) {
        int len = diamondConfs.size();
        if (allDiamondConfs == null) {
            allDiamondConfs = diamondConfs;
        }
        //最大访问次数为diamondConfs.size()
        maxTimes = len;
        //设置重试次数为0
        retryTimes = 0;
        //当前下标设置为0
        currentIndex = 0;
        //初始化下标数组
        randomIndexSequence = new int[len];
        //赋值
        for (int i = 0; i < len; i++) {
            randomIndexSequence[i] = i;
        }
        // 1.长度为１直接返回
        if (len == 1)
            return;
        // 2.长度为２,50%的概率换一下
        Random random = new Random();
        if (len == 2 && random.nextInt(2) == 1) {
            int temp = randomIndexSequence[0];
            randomIndexSequence[0] = randomIndexSequence[1];
            randomIndexSequence[1] = temp;
            return;
        }
        // 3.随机产生一个0~n-2的下标,并将此下标的值与数组最后一个元素交换,进行2n次
        int times = 2 * len;
        for (int j = 0; j < times; j++) {
            int selectedIndex = random.nextInt(len - 1);
            //将随机产生下标的值与最后一个元素值交换
            int temp = randomIndexSequence[selectedIndex];
            randomIndexSequence[selectedIndex] = randomIndexSequence[len - 1];
            randomIndexSequence[len - 1] = temp;
        }
    }


    public int getRetryTimes() {
        return retryTimes;
    }

    public int getMaxTimes() {
        return maxTimes;
    }

    /**
     * 随机取得一个diamondServer配置对象
     *
     * @return DiamondConf diamondServer配置对象
     */
    public DiamondConf generatorOneDiamondConf() {
        DiamondConf diamondConf = null;
        //访问下标小于最后一个下标
        if (retryTimes < maxTimes) {
            //得到当前访问下标
            currentIndex = randomIndexSequence[retryTimes];
            diamondConf = allDiamondConfs.get(currentIndex);
        } else {
            randomIndexSequence = null;
        }
        retryTimes++;
        return diamondConf;
    }


    public int[] getRandomIndexSequence() {
        return randomIndexSequence;
    }

    public String getSequenceToString() {
        StringBuilder sb = new StringBuilder();
        for (int i : this.randomIndexSequence)
            sb.append(i + "");
        return sb.toString();
    }
}
