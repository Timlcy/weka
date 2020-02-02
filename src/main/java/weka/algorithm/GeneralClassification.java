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
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.output.prediction.AbstractOutput;
import weka.classifiers.rules.ZeroR;
import weka.core.BatchPredictor;
import weka.core.Instances;
import weka.gui.CostMatrixEditor;
import weka.gui.explorer.ClassifierErrorsPlotInstances;
import weka.gui.explorer.ExplorerDefaults;
import weka.initData.GeneralData;
import weka.show.Summary;

import java.util.List;

/**
 * @ClassName GeneralClassification
 * @Description 通用分类接口
 * @Author 林春永
 * @Date 2020/2/2
 * @Version 1.0
 **/
@RestController
@RequestMapping(value = "/classification")
@Api(value = "通用分类接口Controller")
public class GeneralClassification {

    //分类器模式选择
    static int testMode = 1;
    //分层交叉校验值
    static int crossValidationText = 10;
    //输出算法模型
    static boolean outputModel = true;
    //百分比
    static double percent = 66;
    //成本矩阵值
    static CostMatrixEditor costMatrixEditorValue = new CostMatrixEditor();
    //成本矩阵
    static CostMatrix costMatrix;
    //评估
    static Evaluation eval;

    //是否输出预测文本
    static boolean outputPredictionsText = false;
    //错误可视化视图
    static ClassifierErrorsPlotInstances plotInstances =
            ExplorerDefaults.getClassifierErrorsPlotInstances();
    static AbstractOutput classificationOutput;

    static List<String> selectedEvalMetrics = Evaluation
            .getAllEvaluationMetricNames();//用户选择评估指标列表

    //默认选择ZeroR分类算法
    static AbstractClassifier classifier = new ZeroR();


    public static boolean isOutputPredictionsText() {
        return outputPredictionsText;
    }

    public static void setOutputPredictionsText(boolean outputPredictionsText) {
        GeneralClassification.outputPredictionsText = outputPredictionsText;
    }

    public static double getPercent() {
        return percent;
    }

    public static void setPercent(double percent) {
        GeneralClassification.percent = percent;
    }

    public static int getCrossValidationText() {
        return crossValidationText;
    }

    public static void setCrossValidationText(int crossValidationText) {
        GeneralClassification.crossValidationText = crossValidationText;
    }

    public static int getTestMode() {
        return testMode;
    }

    public static void setTestMode(int testMode) {
        GeneralClassification.testMode = testMode;
    }

    public static AbstractClassifier getClassifier() {
        return classifier;
    }

    public static void setClassifier(AbstractClassifier classifier) {
        GeneralClassification.classifier = classifier;
    }


    /**
     * 根据位置进行参数控制 1,2 3,4 "-C", "first", "-N", "ID"
     *
     * @param options
     */
    @ApiOperation(value = "算法选择接口")
    @PostMapping("classificationSelection")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "options", value = "分类参数", required =
                    true, paramType = "query", dataType = "String", allowMultiple = true,
                    defaultValue =
                            "-C,0.25,-M,2"
            ),
            @ApiImplicitParam(name = "classifierName", value = "分类器", required =
                    true, paramType = "query", dataType = "String", defaultValue = "weka" +
                    ".classifiers.trees.J48"
            )
    })
    public void classificationSelection(
            @RequestParam(value = "options", required = true) String[] options,
            @RequestParam(value = "classifierName", required = true) String classifierName
    ) {
        try {
            classifier = (AbstractClassifier) Class.forName(classifierName).newInstance();
            classifier.setOptions(options);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static long runClassification() {
        long trainTimeStart = 0, trainTimeElapsed = 0;
        if (outputModel || (testMode == 3) || (testMode == 4)) {
            trainTimeStart = System.currentTimeMillis();
            try {
                classifier.buildClassifier(GeneralData.getInstances());
            } catch (Exception e) {
                e.printStackTrace();
            }
            trainTimeElapsed = System.currentTimeMillis() - trainTimeStart;
            return trainTimeElapsed;
        } else {
            return 0L;
        }
    }

    public static void runProcess() throws Exception {
        switch (testMode) {
            case 3: // Test on training
                testOrTrainingMode();
                break;
            case 1: // CV mode
//                crossValidationMode();
                break;

            case 2: // Percent split
//                percentSplitMode();
                break;

            case 4: // Test on user split
//                testOnUserSplitMode();
                break;
            default:
                throw new Exception("Test mode not implemented");
        }

    }


    private static void testOrTrainingMode() throws Exception {
        long testTimeStart = 0, testTimeElapsed = 0;
        Instances inst = GeneralData.getInstances();
        eval = new Evaluation(inst, costMatrix);

        eval.setMetricsToDisplay(selectedEvalMetrics);

        if (outputPredictionsText) {
            Summary.printPredictionsHeader(classificationOutput,
                    "training set");
        }

        testTimeStart = System.currentTimeMillis();
        if (classifier instanceof BatchPredictor
                && ((BatchPredictor) classifier)
                .implementsMoreEfficientBatchPrediction()) {
            Instances toPred = new Instances(inst);
            for (int i = 0; i < toPred.numInstances(); i++) {
                toPred.instance(i).setClassMissing();
            }
            double[][] predictions =
                    ((BatchPredictor) classifier)
                            .distributionsForInstances(toPred);
            plotInstances.process(inst, predictions, eval);
            if (outputPredictionsText) {
                for (int jj = 0; jj < inst.numInstances(); jj++) {
                    classificationOutput.printClassification(predictions[jj],
                            inst.instance(jj), jj);
                }
            }
        } else {
            for (int jj = 0; jj < inst.numInstances(); jj++) {
                plotInstances.process(inst.instance(jj), classifier, eval);

                if (outputPredictionsText) {
                    classificationOutput.printClassification(classifier,
                            inst.instance(jj), jj);
                }
                if ((jj % 100) == 0) {
//                    log.info("Evaluating on training data. Processed "
//                            + jj + " instances...");
                }
            }
        }
        testTimeElapsed = System.currentTimeMillis() - testTimeStart;
        if (outputPredictionsText) {
            classificationOutput.printFooter();
        }
    }


    public static void setCostMatrix() {
        costMatrixEditorValue.setValue(new CostMatrix(1));//设置成本矩阵值
        costMatrix = new CostMatrix((CostMatrix) costMatrixEditorValue.getValue());
    }


}
