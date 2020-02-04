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
import org.springframework.web.multipart.MultipartFile;
import weka.classifiers.*;
import weka.classifiers.evaluation.output.prediction.AbstractOutput;
import weka.classifiers.rules.ZeroR;
import weka.core.*;
import weka.core.converters.ConverterUtils;
import weka.gui.CostMatrixEditor;
import weka.gui.explorer.ClassifierErrorsPlotInstances;
import weka.gui.explorer.ExplorerDefaults;
import weka.initData.GeneralData;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

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

    static final Logger LOGGER = LoggerFactory.getLogger(GeneralClassification.class);

    // 1:分层交叉校验 2:按比例拆分 3:使用训练集 4.选择测试集
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
    static boolean outputEntropy = true;
    static boolean outputSummary = true;
    static boolean outputPerClass = true;
    static boolean outputConfusion = true;
    static boolean outputSourceCode = true;
    static Classifier fullClassifier;
    static JTextField sourceCodeClass = new JTextField("WekaClassifier", 10);
    static int classIndex = 1;
    static boolean collectPredictionsForEvaluation = true;
    static boolean outputModelsForTrainingSplits = false;
    //随机种子进行交叉验证或％拆分
    static String randomSeedText = "1";

    static Instances userTestStructure;

    //是否输出预测文本
    static boolean outputPredictionsText = false;
    //错误可视化视图
    static ClassifierErrorsPlotInstances plotInstances =
            ExplorerDefaults.getClassifierErrorsPlotInstances();
    static AbstractOutput classificationOutput;

    static List<String> selectedEvalMetrics = Evaluation
            .getAllEvaluationMetricNames();//用户选择评估指标列表
    static Classifier template;

    static boolean preserveOrderBut = true;
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

    @ApiOperation(value = "设置使用训练集")
    @PostMapping("setUseTrainingSet")
    public void setUseTrainingSet() {
        setTestMode(3);
    }

    @ApiOperation(value = "设置使用测试集")
    @PostMapping(value = "setUseTestSet", headers = "content-type=multipart/form-data", consumes =
            "multipart/*")
    public void setUseTestSet(@RequestParam("file") MultipartFile file) {
        if (!file.isEmpty()) {
            try {
                InputStream is = file.getInputStream();
                Instances read = ConverterUtils.DataSource.read(is);
                read.setClassIndex(read.numAttributes() - 1);
                ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(read);
                GeneralData.setSource(dataSource);
                setTestMode(4);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    @ApiOperation(value = "设置分层交叉校验值")
    @PostMapping("setCrossValidationValue")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "crossValidationValue", value = "分层交叉校验值", required =
                    true, paramType = "query", dataType = "int",
                    defaultValue = "10"
            )
    })
    public void setCrossValidationValue(@RequestParam(value = "crossValidationValue", required =
            false, defaultValue = "10") int crossValidationValue) {
        setCrossValidationText(crossValidationValue);
        setTestMode(1);
    }

    @ApiOperation(value = "设置训练集占比值")
    @PostMapping("setTrainSetRatio")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "trainSetRatio", value = "训练集占比值", required =
                    true, paramType = "query", dataType = "double",
                    defaultValue = "66"
            )
    })
    public void setTrainSetRatio(@RequestParam(value = "crossValidationValue", required =
            false, defaultValue = "10") double crossValidationValue) {
        setPercent(crossValidationValue);
        setTestMode(2);
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

    public static String runProcess() throws Exception {
        StringBuffer outBuff = new StringBuffer();
        template = AbstractClassifier.makeCopy(classifier);
        long testTimeElapsed = 0L;
        switch (testMode) {
            case 3: // Test on training
                testTimeElapsed = testOrTrainingMode(outBuff);
                break;
            case 1: // CV mode
                crossValidationMode(outBuff);
                break;

            case 2: // Percent split
                testTimeElapsed = percentSplitMode(outBuff);
                break;

            case 4: // Test on user split
                testTimeElapsed = testOnUserSplitMode(outBuff);
                break;
            default:
                throw new Exception("Test mode not implemented");
        }

        if (testMode != 1) {
            String mode = "";
            if (testMode == 2) {
                mode = "test split";
            } else if (testMode == 3) {
                mode = "training data";
            } else if (testMode == 4) {
                mode = "supplied test set";
            }
            outBuff.append("\nTime taken to test model on " + mode + ": "
                    + Utils.doubleToString(testTimeElapsed / 1000.0, 2)
                    + " seconds\n\n");
        }

        if (outputSummary) {
            outBuff.append(eval.toSummaryString(outputEntropy) + "\n");
        }

        if (GeneralData.getInstances().attribute(classIndex).isNominal()) {

            if (outputPerClass) {
                outBuff.append(eval.toClassDetailsString() + "\n");//Detailed Accuracy By Class
            }

            if (outputConfusion) {
                outBuff.append(eval.toMatrixString() + "\n");//Confusion Matrix
            }
        }
        if ((fullClassifier instanceof Sourcable)
                && outputSourceCode) {
            outBuff.append("=== Source code ===\n\n");
            outBuff.append(Evaluation.wekaStaticWrapper(
                    ((Sourcable) fullClassifier), sourceCodeClass.getText()));
        }

        return outBuff.toString();


    }

    private static long testOnUserSplitMode(StringBuffer outBuff) throws Exception {
        Instances inst = GeneralData.getInstances();
        long testTimeStart = 0, testTimeElapsed = 0;
        LOGGER.info("Evaluating on test data...");
        eval = new Evaluation(inst, costMatrix);
        // make adjustments if the classifier is an InputMappedClassifier
        eval = setupEval(eval, classifier, inst, costMatrix, plotInstances,
                classificationOutput, false, collectPredictionsForEvaluation);
        plotInstances.setInstances(userTestStructure);
        eval.setMetricsToDisplay(selectedEvalMetrics);

        plotInstances.setEvaluation(eval);
        plotInstances.setUp();

        if (outputPredictionsText) {
            printPredictionsHeader(classificationOutput,
                    "test set", outBuff);
        }

        Instance instance;
        int jj = 0;
        Instances batchInst = null;
        int batchSize = 100;
        if (classifier instanceof BatchPredictor
                && ((BatchPredictor) classifier)
                .implementsMoreEfficientBatchPrediction()) {
            batchInst = new Instances(userTestStructure, 0);
            String batchSizeS =
                    ((BatchPredictor) classifier).getBatchSize();
            if (batchSizeS != null && batchSizeS.length() > 0) {
                try {
                    batchSizeS =
                            Environment.getSystemWide().substitute(batchSizeS);
                } catch (Exception ex) {
                }

                try {
                    batchSize = Integer.parseInt(batchSizeS);
                } catch (NumberFormatException ex) {
                    // just go with the default
                }
            }
            LOGGER.info("Performing batch prediction with batch size " + batchSize);
        }
        testTimeStart = System.currentTimeMillis();
        ConverterUtils.DataSource source = GeneralData.getSource();
        while (source.hasMoreElements(userTestStructure)) {
            instance = source.nextElement(userTestStructure);

            if (classifier instanceof BatchPredictor
                    && ((BatchPredictor) classifier)
                    .implementsMoreEfficientBatchPrediction()) {
                batchInst.add(instance);
                if (batchInst.numInstances() == batchSize) {
                    Instances toPred = new Instances(batchInst);
                    for (int i = 0; i < toPred.numInstances(); i++) {
                        toPred.instance(i).setClassMissing();
                    }
                    double[][] predictions =
                            ((BatchPredictor) classifier)
                                    .distributionsForInstances(toPred);
                    plotInstances.process(batchInst, predictions, eval);

                    if (outputPredictionsText) {
                        for (int kk = 0; kk < batchInst.numInstances(); kk++) {
                            classificationOutput.printClassification(
                                    predictions[kk], batchInst.instance(kk), kk);
                        }
                    }
                    jj += batchInst.numInstances();
                    LOGGER.info("Evaluating on test data. Processed "
                            + jj + " instances...");
                    batchInst.delete();
                }
            } else {
                plotInstances.process(instance, classifier, eval);
                if (outputPredictionsText) {
                    classificationOutput.printClassification(classifier,
                            instance, jj);
                }
                if ((++jj % 100) == 0) {
                    LOGGER.info("Evaluating on test data. Processed "
                            + jj + " instances...");
                }
            }
        }

        if (classifier instanceof BatchPredictor
                && ((BatchPredictor) classifier)
                .implementsMoreEfficientBatchPrediction()
                && batchInst.numInstances() > 0) {
            // finish the last batch

            Instances toPred = new Instances(batchInst);
            for (int i = 0; i < toPred.numInstances(); i++) {
                toPred.instance(i).setClassMissing();
            }

            double[][] predictions =
                    ((BatchPredictor) classifier)
                            .distributionsForInstances(toPred);
            plotInstances.process(batchInst, predictions, eval);

            if (outputPredictionsText) {
                for (int kk = 0; kk < batchInst.numInstances(); kk++) {
                    classificationOutput.printClassification(predictions[kk],
                            batchInst.instance(kk), kk);
                }
            }
        }
        testTimeElapsed = System.currentTimeMillis() - testTimeStart;

        if (outputPredictionsText) {
            classificationOutput.printFooter();
        }
        if (outputPredictionsText) {
            outBuff.append("\n");
        }
        outBuff.append("=== Evaluation on test set ===\n");

        return testTimeElapsed;
    }


    private static long percentSplitMode(StringBuffer outBuff) throws Exception {
        int rnd;
        long testTimeStart = 0, testTimeElapsed = 0;
        Instances inst = GeneralData.getInstances();
        if (!preserveOrderBut) {
            LOGGER.info("Randomizing instances...");
            try {
                rnd = Integer.parseInt(randomSeedText.trim());
            } catch (Exception ex) {
                LOGGER.info("Trouble parsing random seed value");
                rnd = 1;
            }
            inst.randomize(new Random(rnd));
        }
        int trainSize =
                (int) Math.round(inst.numInstances() * percent / 100);
        int testSize = inst.numInstances() - trainSize;
        Instances train = new Instances(inst, 0, trainSize);
        Instances test = new Instances(inst, trainSize, testSize);
        LOGGER.info("Building model on training split ("
                + trainSize + " instances)...");
        Classifier current = null;
        try {
            current = AbstractClassifier.makeCopy(template);
        } catch (Exception ex) {
            LOGGER.info("Problem copying classifier: "
                    + ex.getMessage());
        }
        current.buildClassifier(train);
        if (outputModelsForTrainingSplits) {
            outBuff.append("\n=== Classifier model for training split ("
                    + trainSize + " instances) ===\n\n");
            outBuff.append(current.toString() + "\n");
        }
        eval = new Evaluation(train, costMatrix);

        // make adjustments if the classifier is an InputMappedClassifier
        eval =
                setupEval(eval, classifier, train, costMatrix, plotInstances,
                        classificationOutput, false, collectPredictionsForEvaluation);
        eval.setMetricsToDisplay(selectedEvalMetrics);

        // plotInstances.setEvaluation(eval);
        plotInstances.setUp();
        LOGGER.info("Evaluating on test split...");

        if (outputPredictionsText) {
            printPredictionsHeader(classificationOutput,
                    "test split", outBuff);
        }

        testTimeStart = System.currentTimeMillis();
        if (classifier instanceof BatchPredictor
                && ((BatchPredictor) classifier)
                .implementsMoreEfficientBatchPrediction()) {
            Instances toPred = new Instances(test);
            for (int i = 0; i < toPred.numInstances(); i++) {
                toPred.instance(i).setClassMissing();
            }

            double[][] predictions =
                    ((BatchPredictor) current).distributionsForInstances(toPred);
            plotInstances.process(test, predictions, eval);
            if (outputPredictionsText) {
                for (int jj = 0; jj < test.numInstances(); jj++) {
                    classificationOutput.printClassification(predictions[jj],
                            test.instance(jj), jj);
                }
            }
        } else {
            for (int jj = 0; jj < test.numInstances(); jj++) {
                plotInstances.process(test.instance(jj), current, eval);
                if (outputPredictionsText) {
                    classificationOutput.printClassification(current,
                            test.instance(jj), jj);
                }
                if ((jj % 100) == 0) {
                    LOGGER.info("Evaluating on test split. Processed "
                            + jj + " instances...");
                }
            }
        }
        testTimeElapsed = System.currentTimeMillis() - testTimeStart;
        if (outputPredictionsText) {
            classificationOutput.printFooter();
        }
        if (outputPredictionsText) {
            outBuff.append("\n");
        }
        outBuff.append("=== Evaluation on test split ===\n");
        return testTimeElapsed;
    }


    private static long testOrTrainingMode(StringBuffer outBuff) throws Exception {
        LOGGER.info("Evaluating on training data...");
        long testTimeStart = 0, testTimeElapsed = 0;
        Instances inst = GeneralData.getInstances();
        eval = new Evaluation(inst, costMatrix);
        //设置输出指标
        eval.setMetricsToDisplay(selectedEvalMetrics);

        if (outputPredictionsText) {
            printPredictionsHeader(classificationOutput,
                    "training set", outBuff);
        }

        testTimeStart = System.currentTimeMillis();
        if (classifier instanceof BatchPredictor && ((BatchPredictor) classifier)
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
                    LOGGER.info("Evaluating on training data. Processed "
                            + jj + " instances...");
                }
            }
        }
        testTimeElapsed = System.currentTimeMillis() - testTimeStart;
        if (outputPredictionsText) {
            classificationOutput.printFooter();
        }
        if (GeneralClassification.isOutputPredictionsText()
                && classificationOutput.generatesOutput()) {
            outBuff.append("\n");
        }
        outBuff.append("=== Evaluation on training set ===\n");

        return testTimeElapsed;
    }

    private static void crossValidationMode(StringBuffer outBuff) throws Exception {

        LOGGER.info("Randomizing instances...");
        int rnd = 1;
        try {
            rnd = Integer.parseInt(randomSeedText.trim());
            // System.err.println("Using random seed "+rnd);
        } catch (Exception ex) {
            LOGGER.info("Trouble parsing random seed value");
            rnd = 1;
        }
        Random random = new Random(rnd);
        Instances inst = GeneralData.getInstances();
        inst.randomize(random);
        if (inst.attribute(classIndex).isNominal()) {
            LOGGER.info("Stratifying instances...");
            inst.stratify(crossValidationText);
        }
        eval = new Evaluation(inst, costMatrix);

        // make adjustments if the classifier is an InputMappedClassifier
        eval = setupEval(eval, classifier, inst, costMatrix, plotInstances,
                classificationOutput, false, collectPredictionsForEvaluation);
        eval.setMetricsToDisplay(selectedEvalMetrics);

        // plotInstances.setEvaluation(eval);
        plotInstances.setUp();

//        if (outputPredictionsText) {
//            printPredictionsHeader(outBuff, classificationOutput, "test data");
//        }

        // Make some splits and do a CV
        for (int fold = 0; fold < crossValidationText; fold++) {
            LOGGER.info("Creating splits for fold " + (fold + 1)
                    + "...");
            Instances train = inst.trainCV(crossValidationText, fold, random);

            // make adjustments if the classifier is an
            // InputMappedClassifier
            eval = setupEval(eval, classifier, train, costMatrix, plotInstances,
                    classificationOutput, true, collectPredictionsForEvaluation);
            eval.setMetricsToDisplay(selectedEvalMetrics);

            // eval.setPriors(train);
            LOGGER.info("Building model for fold " + (fold + 1)
                    + "...");
            Classifier current = null;
            try {
                current = AbstractClassifier.makeCopy(template);
//                if (current instanceof LogHandler) {
//                    ((LogHandler) current).setLog(m_Log);
//                }
            } catch (Exception ex) {
                LOGGER.info("Problem copying classifier: "
                        + ex.getMessage());
            }
            current.buildClassifier(train);
            if (outputModelsForTrainingSplits) {
                outBuff.append("\n=== Classifier model for fold "
                        + (fold + 1) + " ===\n\n");
                outBuff.append(current.toString() + "\n");
            }
            Instances test = inst.testCV(crossValidationText, fold);
            LOGGER.info("Evaluating model for fold " + (fold + 1)
                    + "...");

            if (classifier instanceof BatchPredictor
                    && ((BatchPredictor) classifier)
                    .implementsMoreEfficientBatchPrediction()) {
                Instances toPred = new Instances(test);
                for (int i = 0; i < toPred.numInstances(); i++) {
                    toPred.instance(i).setClassMissing();
                }
                double[][] predictions =
                        ((BatchPredictor) current)
                                .distributionsForInstances(toPred);
                plotInstances.process(test, predictions, eval);
                if (outputPredictionsText) {
                    for (int jj = 0; jj < test.numInstances(); jj++) {
                        classificationOutput.printClassification(predictions[jj],
                                test.instance(jj), jj);
                    }
                }
            } else {
                for (int jj = 0; jj < test.numInstances(); jj++) {
                    plotInstances.process(test.instance(jj), current, eval);
                    if (outputPredictionsText) {
                        classificationOutput.printClassification(current,
                                test.instance(jj), jj);
                    }
                }
            }
        }
        if (outputPredictionsText) {
            classificationOutput.printFooter();
        }
        if (outputPredictionsText) {
            outBuff.append("\n");
        }
        if (inst.attribute(classIndex).isNominal()) {
            outBuff.append("=== Stratified cross-validation ===\n");
        } else {
            outBuff.append("=== Cross-validation ===\n");
        }
    }

    public static Evaluation setupEval(Evaluation eval, Classifier classifier,
                                       Instances inst, CostMatrix costMatrix,
                                       ClassifierErrorsPlotInstances plotInstances,
                                       AbstractOutput classificationOutput, boolean
                                               onlySetPriors, boolean collectPredictions)
            throws Exception {

        if (classifier instanceof weka.classifiers.misc.InputMappedClassifier) {
            Instances mappedClassifierHeader =
                    ((weka.classifiers.misc.InputMappedClassifier) classifier)
                            .getModelHeader(new Instances(inst, 0));

            if (classificationOutput != null) {
                classificationOutput.setHeader(mappedClassifierHeader);
            }

            if (!onlySetPriors) {
                if (costMatrix != null) {
                    eval =
                            new Evaluation(new Instances(mappedClassifierHeader, 0), costMatrix);
                } else {
                    eval = new Evaluation(new Instances(mappedClassifierHeader, 0));
                }
            }

            if (!eval.getHeader().equalHeaders(inst)) {
                Instances mappedClassifierDataset =
                        ((weka.classifiers.misc.InputMappedClassifier) classifier)
                                .getModelHeader(new Instances(mappedClassifierHeader, 0));
                for (int zz = 0; zz < inst.numInstances(); zz++) {
                    Instance mapped =
                            ((weka.classifiers.misc.InputMappedClassifier) classifier)
                                    .constructMappedInstance(inst.instance(zz));
                    mappedClassifierDataset.add(mapped);
                }
                eval.setPriors(mappedClassifierDataset);
                setPlotInstances(eval, classifier, mappedClassifierDataset, plotInstances,
                        onlySetPriors);
            } else {
                eval.setPriors(inst);
                setPlotInstances(eval, classifier, inst, plotInstances, onlySetPriors);
            }
        } else {
            //设置先验概率
            eval.setPriors(inst);
            setPlotInstances(eval, classifier, inst, plotInstances, onlySetPriors);
        }

        eval.setDiscardPredictions(!collectPredictions);

        return eval;
    }

    private static void setPlotInstances(Evaluation eval, Classifier classifier, Instances inst,
                                         ClassifierErrorsPlotInstances plotInstances, boolean
                                                 onlySetPriors) {
        if (!onlySetPriors) {
            if (plotInstances != null) {
                plotInstances.setInstances(inst);
                plotInstances.setClassifier(classifier);
                plotInstances.setClassIndex(inst.classIndex());
                plotInstances.setEvaluation(eval);
            }
        }
    }

    public static void printPredictionsHeader(
            AbstractOutput classificationOutput, String title, StringBuffer outBuff) {
        if (classificationOutput.generatesOutput()) {
            outBuff.append("=== Predictions on " + title + " ===\n\n");
        }
        classificationOutput.printHeader();
    }

    public static void setCostMatrix() {
        //设置成本矩阵值
        costMatrixEditorValue.setValue(new CostMatrix(1));
        costMatrix = new CostMatrix((CostMatrix) costMatrixEditorValue.getValue());
    }


}
