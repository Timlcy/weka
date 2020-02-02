package weka.initData;

import weka.core.Instances;
import weka.core.converters.ConverterUtils;

/**
 * @ClassName GeneralData
 * @Description 通用数据
 * @Author 林春永
 * @Date 2020/2/1
 * @Version 1.0
 **/
public class GeneralData {

    public static Instances instances;

    public static ConverterUtils.DataSource source;

    public static ConverterUtils.DataSource getSource() {
        return source;
    }

    public static void setSource(ConverterUtils.DataSource source) {
        GeneralData.source = source;
    }

    public static Instances getInstances() {
        return instances;
    }

    public static void setInstances(Instances instances) {
        GeneralData.instances = instances;
    }
}
