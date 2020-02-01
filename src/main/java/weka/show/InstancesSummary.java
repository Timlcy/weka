package weka.show;

import weka.initData.DataBase;
import weka.initData.GeneralData;

/**
 * @ClassName InstancesSummary
 * @Description
 * @Author 林春永
 * @Date 2020/1/29
 * @Version 1.0
 **/
public class InstancesSummary {

    public static String GeneratesInstancesSummary() {
        return GeneralData.getInstances().toSummaryString();
    }
}
