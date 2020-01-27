package weka.initData;

import org.junit.Test;
import weka.core.Instances;
import weka.filterData.filterData;

/**
 * @ClassName mysql
 * @Description 插入mysql
 * @Author 林春永
 * @Date 2020/1/24
 * @Version 1.0
 **/
public class mysql {


    @Test
    public void setInstances() {
        QueryInstances queryInstances = new QueryInstances();
        queryInstances.setUsername("root");
        queryInstances.setPassword("123456");
        queryInstances.setDatabaseURL("jdbc:mysql://localhost:3306/weka?useUnicode=true" +
                "&characterEncoding=utf8&serverTimezone=UTC");
        queryInstances.setQuery("select * from weather");
        Instances instances = queryInstances.changeInstances();
        instances.setClassIndex(instances.numAttributes() - 1);
        try {
            filterData.filterData(instances);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
