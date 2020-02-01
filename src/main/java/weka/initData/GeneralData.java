package weka.initData;

import weka.core.Instances;

/**
 * @ClassName GeneralData
 * @Description 通用数据
 * @Author 林春永
 * @Date 2020/2/1
 * @Version 1.0
 **/
public class GeneralData {

    public static Instances instances;

    public static Instances getInstances() {
        return instances;
    }

    public static void setInstances(Instances instances) {
        GeneralData.instances = instances;
    }
}
