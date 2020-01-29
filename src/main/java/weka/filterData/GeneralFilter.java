package weka.filterData;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.AddID;
import weka.initData.DataBase;

/**
 * @ClassName filter
 * @Description 过滤数据
 * @Author 林春永
 * @Date 2020/1/29
 * @Version 1.0
 **/
@RestController
@RequestMapping(value = "/filter")
@Api(value = "过滤数据接口Controller")
public class GeneralFilter {

    /**
     * 根据位置进行参数控制 1,2 3,4 "-C", "first", "-N", "ID"
     * @param options
     */
    @ApiOperation(value = "过滤数据接口接口")
    @PostMapping("filterData")
    public void filterData(@RequestParam(value = "username", required = true, defaultValue =
            "\"-C\", \"first\", \"-N\", \"ID\"") String[] options)  {
        Instances inst = DataBase.instances;
       Filter filter = new AddID();
        try {
            filter.setOptions(options);
            filter.setInputFormat(inst);
            Instances newInstances = Filter.useFilter(inst, filter);
            DataBase.setInstances(newInstances);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
