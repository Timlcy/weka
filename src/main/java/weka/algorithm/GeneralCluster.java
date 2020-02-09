package weka.algorithm;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import weka.classifiers.AbstractClassifier;
import weka.clusterers.AbstractClusterer;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.clusterers.EM;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.gui.explorer.ClustererAssignmentsPlotInstances;
import weka.gui.explorer.ExplorerDefaults;
import weka.initData.GeneralData;

import java.util.List;
import java.util.Random;

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

    final static Logger LOGGER = LoggerFactory.getLogger(GeneralCluster.class);

    static ClustererAssignmentsPlotInstances plotInstances =
            ExplorerDefaults.getClustererAssignmentsPlotInstances();

    static Clusterer cluster = new EM();
    static List<Integer> ignoreKeyList;
    static boolean classesToClustersBut = false;
    static int classCombo;
    static int[] ignoredAtts;
    //2:按比例拆分 3:使用训练集 4:选择测试集 5:选择属性
    static int testMode = 3;
    static int percent = 66;
    static Instances userTest;
    static Instances trainInst;

    public static Clusterer getCluster() {
        return cluster;
    }

    public static void setCluster(Clusterer cluster) {
        GeneralCluster.cluster = cluster;
    }

    public static int getPercent() {
        return percent;
    }

    public static void setPercent(int percent) {
        GeneralCluster.percent = percent;
    }

    public static Instances getUserTest() {
        return userTest;
    }

    public static void setUserTest(Instances userTest) {
        GeneralCluster.userTest = userTest;
    }

    public static int getTestMode() {
        return testMode;
    }

    public static void setTestMode(int testMode) {
        GeneralCluster.testMode = testMode;
    }

    public static int[] getIgnoredAtts() {
        return ignoredAtts;
    }

    public static void setIgnoredAtts(int[] ignoredAtts) {
        GeneralCluster.ignoredAtts = ignoredAtts;
    }

    public static int getClassCombo() {
        return classCombo;
    }

    public static void setClassCombo(int classCombo) {
        GeneralCluster.classCombo = classCombo;
    }



    public static List<Integer> getIgnoreKeyList() {
        return ignoreKeyList;
    }

    public static void setIgnoreKeyList(List<Integer> ignoreKeyList) {
        GeneralCluster.ignoreKeyList = ignoreKeyList;
    }

    public static boolean isClassesToClustersBut() {
        return classesToClustersBut;
    }

    public static void setClassesToClustersBut(boolean classesToClustersBut) {
        GeneralCluster.classesToClustersBut = classesToClustersBut;
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

    public static long buildClusterer() throws Exception {
        Instances inst = GeneralData.getInstances();
        trainInst = new Instances(inst);
        if (classesToClustersBut) {
            trainInst.setClassIndex(classCombo);
            inst.setClassIndex(classCombo);
            if (inst.classAttribute().isNumeric()) {
                throw new Exception("Class must be nominal for class based "
                        + "evaluation!");
            }
        }
        if (!(ignoreKeyList == null || ignoreKeyList.isEmpty())) {
            trainInst = removeIgnoreCols(trainInst);
        }


        long trainTimeStart = 0, trainTimeElapsed = 0;
        Clusterer clusterer = GeneralCluster.getCluster();
        // Build the model and output it.
        LOGGER.info("Building model on training data...");
        // remove the class attribute (if set) and build the clusterer
        trainTimeStart = System.currentTimeMillis();
        try {
            clusterer.buildClusterer(removeClass(trainInst));
        } catch (Exception e) {
            e.printStackTrace();
        }
        trainTimeElapsed = System.currentTimeMillis() - trainTimeStart;
        return trainTimeElapsed;
    }

    public static String clusterResults() throws Exception {
        long trainTimeStart = 0, trainTimeElapsed = 0;
        StringBuilder outBuff = new StringBuilder();
        Instances inst = GeneralData.getInstances();
        ClusterEvaluation eval = new ClusterEvaluation();
        eval.setClusterer(cluster);
        switch (testMode) {
            case 3:
            case 5: // Test on training
                LOGGER.info("Clustering training data...");
                eval.evaluateClusterer(trainInst, "", false);

                plotInstances.setInstances(inst);
                plotInstances.setClusterEvaluation(eval);
                outBuff.append("=== Model and evaluation on training set ===\n\n");
                break;

            case 2: // Percent split
                LOGGER.info("Randomizing instances...");
                inst.randomize(new Random(1));
                trainInst.randomize(new Random(1));
                int trainSize = trainInst.numInstances() * percent / 100;
                int testSize = trainInst.numInstances() - trainSize;
                Instances train = new Instances(trainInst, 0, trainSize);
                Instances test = new Instances(trainInst, trainSize, testSize);
                Instances testVis = new Instances(inst, trainSize, testSize);
                LOGGER.info("Building model on training split...");
                trainTimeStart = System.currentTimeMillis();
                cluster.buildClusterer(train);
                trainTimeElapsed = System.currentTimeMillis() - trainTimeStart;
                LOGGER.info("Evaluating on test split...");
                eval.evaluateClusterer(test, "", false);
                plotInstances.setInstances(testVis);
                plotInstances.setClusterEvaluation(eval);
                outBuff.append("=== Model and evaluation on test split ===\n");
                outBuff.append(cluster.toString() + "\n");
                outBuff.append("\nTime taken to build model (percentage split) : "
                        + Utils.doubleToString(trainTimeElapsed / 1000.0, 2)
                        + " seconds\n\n");
                break;

            case 4: // Test on user split
                LOGGER.info("Evaluating on test data...");
                Instances userTestT = new Instances(userTest);
                if (!(ignoreKeyList == null || ignoreKeyList.isEmpty())) {
                    userTestT = removeIgnoreCols(userTestT);
                }
                eval.evaluateClusterer(userTestT, "", false);
                plotInstances.setInstances(userTest);
                plotInstances.setClusterEvaluation(eval);
                outBuff.append("=== Evaluation on test set ===\n");
                break;

            default:
                throw new Exception("Test mode not implemented");
        }
        outBuff.append(eval.clusterResultsToString());
        outBuff.append("\n");
        return outBuff.toString();
    }

    private static Instances removeClass(Instances inst) {
        Remove af = new Remove();
        Instances retI = null;

        try {
            if (inst.classIndex() < 0) {
                retI = inst;
            } else {
                af.setAttributeIndices("" + (inst.classIndex() + 1));
                af.setInvertSelection(false);
                af.setInputFormat(inst);
                retI = Filter.useFilter(inst, af);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retI;
    }

    private static Instances removeIgnoreCols(Instances inst) {

        // If the user is doing classes to clusters evaluation and
        // they have opted to ignore the class, then unselect the class in
        // the ignore list
        if (classesToClustersBut) {
            int classIndex = classCombo;
            if (ignoreKeyList.contains(classIndex)) {
                ignoreKeyList.remove(classIndex);
            }
        }
        int[] selected = ignoreKeyList.stream().mapToInt(Integer::valueOf).toArray();
        Remove af = new Remove();
        Instances retI = null;

        try {
            af.setAttributeIndicesArray(selected);
            af.setInvertSelection(false);
            af.setInputFormat(inst);
            retI = Filter.useFilter(inst, af);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retI;
    }


}
