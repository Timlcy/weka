package weka.initData;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import weka.core.Instances;

import java.util.*;

/**
 * @ClassName mysql
 * @Description 插入mysql
 * @Author 林春永
 * @Date 2020/1/24
 * @Version 1.0
 **/
@RestController
@RequestMapping(value = "/dataBase")
@Api(value = "数据源接口Controller")
public class DataBase {

    public static Instances instances;

    public static QueryInstances queryInstances;

    public static Instances getInstances() {
        return instances;
    }

    public static void setInstances(Instances instances) {
        DataBase.instances = instances;
    }

    @ApiOperation(value = "数据源接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名", required =
                    true, paramType = "query", dataType = "String"
            ),
            @ApiImplicitParam(name = "password", value = "密码", required = true, paramType =
                    "query", dataType = "String"
            ),
            @ApiImplicitParam(name = "databaseURL", value = "数据库连接URL", required = true, paramType =
                    "query", dataType = "String"
            )
    })
    @PostMapping("initData")
    public void initData(@RequestParam(value = "username", required = true, defaultValue =
            "root") String username,
                         @RequestParam(value = "password", required = true, defaultValue =
                                 "123456") String password,
                         @RequestParam(value = "databaseURL", required = true, defaultValue =
                                 "jdbc:mysql://localhost:3306/weka?useUnicode=true" +
                                         "&characterEncoding=utf8&serverTimezone=UTC") String databaseURL
    ) {
        queryInstances = new QueryInstances();
        queryInstances.setUsername(username);
        queryInstances.setPassword(password);
        queryInstances.setDatabaseURL(databaseURL);
    }

    @ApiOperation(value = "测试数据源连接接口")
    @PostMapping("testDataBase")
    public boolean testDataBase(
    ) {
        return queryInstances.connectionDataBase();
    }

    @ApiOperation(value = "查询转换")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "querySQL", value = "查询SQL", required =
                    true, paramType = "query", dataType = "String"
            )
    })
    @PostMapping("querySQL")
    public Map querySQL(@RequestParam(value = "querySQL", required = true) String querySQL
    ) {
        queryInstances.setQuery(querySQL);
        instances = queryInstances.changeInstances();
        instances.setClassIndex(instances.numAttributes() - 1);
        List<Map<String, Object>> queryList = queryInstances.getQueryList();
        Vector<String> columnNames = queryInstances.getColumnNames();
        Map<String, Object> map = new HashMap<>();
        map.put("columnNames", columnNames);
        map.put("queryValue", queryList);
        return map;
    }


}
