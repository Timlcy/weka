package weka.filterData;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.AddID;

/**
 * @ClassName filterData
 * @Description 过滤数据和展示数据基础信息
 * @Author 林春永
 * @Date 2020/1/27
 * @Version 1.0
 **/
public class filterData {


    public static void  filterData(Instances inst) throws Exception {
        Filter filter = new AddID();
        String[] options = new String[]{"-C","first","-N","ID"};
        filter.setOptions(options);
        filter.setInputFormat(inst);
        Instances newInstances = Filter.useFilter(inst, filter);
        System.out.println(newInstances.toSummaryString());
    }
}
