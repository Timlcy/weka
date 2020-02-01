package weka.initData;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.core.converters.DatabaseLoader;
import weka.experiment.InstanceQuery;

import java.io.IOException;
import java.io.InputStream;
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
public class DataBase extends GeneralData {

    public static QueryInstances queryInstances;

    public static List<Map> dataBaseHistory;

    public static List<String> sqlHistory;

    @ApiOperation(value = "数据源接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名", required =
                    true, paramType = "query", dataType = "String", defaultValue =
                    "root"
            ),
            @ApiImplicitParam(name = "password", value = "密码", required = true, paramType =
                    "query", dataType = "String", defaultValue =
                    "123456"
            ),
            @ApiImplicitParam(name = "databaseURL", value = "数据库连接URL", required = true, paramType =
                    "query", dataType = "String", defaultValue =
                    "jdbc:mysql://localhost:3306/weka?useUnicode=true" +
                            "&characterEncoding=utf8&serverTimezone=UTC"
            )
    })
    @PostMapping("initData")
    public void initData(@RequestParam(value = "username", required = true) String username,
                         @RequestParam(value = "password", required = true) String password,
                         @RequestParam(value = "databaseURL", required = true) String databaseURL
    ) {
        queryInstances = new QueryInstances();
        queryInstances.setUsername(username);
        queryInstances.setPassword(password);
        queryInstances.setDatabaseURL(databaseURL);

        Map<String, Object> dataBaseMap = new HashMap<>();
        dataBaseMap.put("username", username);
        dataBaseMap.put("password", password);
        dataBaseMap.put("databaseURL", databaseURL);
        if (dataBaseHistory == null) {
            dataBaseHistory = new ArrayList<>();
        }
        if (!dataBaseHistory.contains(dataBaseMap)) {
            dataBaseHistory.add(dataBaseMap);
        }


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
        if (sqlHistory == null) {
            sqlHistory = new ArrayList<>();
        }
        if (!sqlHistory.contains(querySQL)) {
            sqlHistory.add(querySQL);
        }
        Instances instances = queryInstances.changeInstances();
        instances.setClassIndex(instances.numAttributes()-1);
        GeneralData.setInstances(instances);
        List<Map<String, Object>> queryList = queryInstances.getQueryList();
        Vector<String> columnNames = queryInstances.getColumnNames();
        Map<String, Object> map = new HashMap<>();
        map.put("columnNames", columnNames);
        map.put("queryValue", queryList);
        return map;
    }


    @ApiOperation(value = "查询数据库历史")
    @PostMapping("queryDataBaseHistory")
    public List queryDataBaseHistory(
    ) {
        return dataBaseHistory;
    }


    @ApiOperation(value = "查询SQL历史")
    @PostMapping("querySqlHistory")
    public List querySqlHistory(
    ) {
        return sqlHistory;
    }

}
