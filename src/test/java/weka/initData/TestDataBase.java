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
public class TestDataBase {

     Instances instances = null;

    @Test
    public void initData() {
        TestQueryInstances testQueryInstances = new TestQueryInstances();
        testQueryInstances.setUsername("root");
        testQueryInstances.setPassword("123456");
        testQueryInstances.setDatabaseURL("jdbc:mysql://localhost:3306/weka?useUnicode=true" +
                "&characterEncoding=utf8&serverTimezone=UTC");
        testQueryInstances.setQuery("select * from weather");
        instances = testQueryInstances.changeInstances();
        instances.setClassIndex(instances.numAttributes() - 1);
        try {
            filterData.filterData(instances);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
