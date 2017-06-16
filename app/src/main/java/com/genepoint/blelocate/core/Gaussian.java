package com.genepoint.blelocate.core;

import java.util.List;

/**
 * 高斯分布概率筛选蓝牙
 *
 * @author jsj
 */
public class Gaussian {
    /**
     * 平均数
     *
     * @param allNumber
     * @return
     */
    public static double averageValue(List<Integer> allNumber) {
        double value = 0;

        for (int i = 0; i < allNumber.size(); i++) {
            value = value + allNumber.get(i);
        }
        value /= allNumber.size();
        return value;

    }

    /**
     * 标准方差
     *
     * @param allNumber
     * @return
     */
    public static double VarianceValue(List<Integer> allNumber) {
        double value = 0;
        double variance = averageValue(allNumber);
        for (int i = 0; i < allNumber.size(); i++) {
            double x = (allNumber.get(i) - variance) * (allNumber.get(i) - variance);
            value += x;

        }

        value /= allNumber.size() - 1;
        value = Math.sqrt(value);
        return value;

    }

    /**
     * 高斯分布概率
     *
     * @param rssi
     * @param average  平均值
     * @param variance 方差
     * @return
     */
    public static double GaussianValue(int rssi, double average, double variance) {
        double value = 1 / (variance * Math.sqrt(2 * Math.PI));
        value = value * Math.exp((-1 * (rssi - average) * (rssi - average)) / (2 * variance * variance));
        return value;
    }

    /**
     * 用高斯筛选，返回信号强度
     *
     * @param rssiList
     * @param yuzhi    阈值
     * @return
     */
    public static int GaussianFilter(List<Integer> rssiList, float yuzhi) {
        double sum = 0;
        int n = 0;
        if (rssiList.size() == 1) {
            return rssiList.get(0);
        } else {
            double average = averageValue(rssiList);
            double variance = VarianceValue(rssiList);
            for (int i = 0; i < rssiList.size(); i++) {
                if (GaussianValue(rssiList.get(i), average, variance) > yuzhi) {
                    sum += rssiList.get(i);
                    ++n;
                }
            }
            if (n != 0)
                return ((int) sum / n);
            else
                return (int) average;
        }
    }
}
