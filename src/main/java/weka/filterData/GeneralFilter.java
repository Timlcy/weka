package weka.filterData;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.filters.AllFilter;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AddClassification;
import weka.filters.unsupervised.attribute.*;
import weka.initData.DataBase;
import weka.initData.GeneralData;

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
     *
     * @param options
     */
    @ApiOperation(value = "过滤数据接口")
    @PostMapping("filterData")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "options", value = "过滤参数", required =
                    true, paramType = "query", dataType = "String", allowMultiple = true,
                    defaultValue =
                            "-C, first,-N, ID"
            ),
            @ApiImplicitParam(name = "filterName", value = "过滤器", required =
                    true, paramType = "query", dataType = "String", defaultValue = "weka.filters" +
                    ".unsupervised.attribute.AddID"
            )
    })
    public void filterData(@RequestParam(value = "options", required = true) String[] options,
                           @RequestParam(value = "filterName", required = true) String filterName
    ) {
        Instances inst = GeneralData.getInstances();
        Filter filter = getFilter(filterName);
        changeInstances(options, inst, filter);
    }

    public static Filter getFilter(String filterName) {
        try {
            return (Filter) Class.forName(filterName).newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return new AllFilter();
    }


    private void changeInstances(String[] options, Instances inst, Filter filter) {
        try {
            filter.setOptions(options);
            filter.setInputFormat(inst);
            Instances newInstances = Filter.useFilter(inst, filter);
            GeneralData.setInstances(newInstances);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
