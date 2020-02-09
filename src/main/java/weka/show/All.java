package weka.show;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName All
 * @Description 输出全部
 * @Author 林春永
 * @Date 2020/1/29
 * @Version 1.0
 **/
@RestController
@RequestMapping(value = "/all")
@Api(value = "输出全部数据接口Controller")
public class All {


    @ApiOperation(value = "输出分类全部数据接口")
    @PostMapping("showClassifierAll")
    public String showClassifierAll() {
        StringBuffer sb = new StringBuffer();
        String instancesSummary = InstancesSummary.GeneratesInstancesSummary();
        String runInformation = RunInformation.classifierRunInformation();
        String classifierModel = ClassifierModel.GeneratesClassifierModel();
        String summary = Summary.GeneratesSummary();
        sb.append(instancesSummary);
        sb.append(runInformation);
        sb.append(classifierModel);
        sb.append(summary);
        return sb.toString();
    }

    @ApiOperation(value = "输出聚类全部数据接口")
    @PostMapping("showClusterAll")
    public String showClusterAll() {
        StringBuffer sb = new StringBuffer();
        String clustererRunInformation = RunInformation.clustererRunInformation();
        String clusteringModel = ClusteringModel.GeneratesClusteringModel();
        String clusterResults = ClusteringModel.clusterResults();
        sb.append(clustererRunInformation);
        sb.append(clusteringModel);
        sb.append(clusterResults);
        return sb.toString();
    }
}
