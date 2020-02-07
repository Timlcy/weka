package weka.algorithm;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import weka.classifiers.AbstractClassifier;
import weka.clusterers.AbstractClusterer;
import weka.clusterers.Clusterer;
import weka.gui.explorer.ClustererAssignmentsPlotInstances;
import weka.gui.explorer.ExplorerDefaults;

/**
 * @ClassName GeneralCluster
 * @Description 通用聚类接口
 * @Author 林春永
 * @Date 2020/2/6
 * @Version 1.0
 **/
@RestController
@RequestMapping(value = "/cluster")
@Api(value = "通用聚类接口Controller")
public class GeneralCluster {
    static ClustererAssignmentsPlotInstances plotInstances =
            ExplorerDefaults.getClustererAssignmentsPlotInstances();

    static AbstractClusterer cluster;

    public static AbstractClusterer getCluster() {
        return cluster;
    }

    public static void setCluster(AbstractClusterer cluster) {
        GeneralCluster.cluster = cluster;
    }

    /**
     * 根据位置进行参数控制 1,2 3,4 "-C", "first", "-N", "ID"
     *
     * @param options
     */
    @ApiOperation(value = "聚类算法选择接口")
    @PostMapping("clusterSelection")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "options", value = "分类参数", required =
                    true, paramType = "query", dataType = "String", allowMultiple = true,
                    defaultValue =
                            "-N,2,-S,1"
            ),
            @ApiImplicitParam(name = "clusterName", value = "分类器", required =
                    true, paramType = "query", dataType = "String", defaultValue = "weka" +
                    ".clusterers.FarthestFirst"
            )
    })
    public void clusterSelection(
            @RequestParam(value = "options", required = true) String[] options,
            @RequestParam(value = "clusterName", required = true) String clusterName
    ) {
        try {
            cluster = (AbstractClusterer) AbstractClusterer.forName(clusterName, options);
            plotInstances.setClusterer((Clusterer) cluster);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
