package weka;

import org.apache.log4j.Logger;
import weka.classifiers.*;
import weka.classifiers.evaluation.output.prediction.AbstractOutput;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.J48;
import weka.core.*;
import weka.core.converters.ConverterUtils;
import weka.gui.CostMatrixEditor;
import weka.gui.GenericObjectEditor;
import weka.gui.explorer.ClassifierErrorsPlotInstances;
import weka.gui.explorer.ExplorerDefaults;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @ClassName WekaTest
 * @Description
 * @Author 林春永
 * @Date 2020/1/5
 * @Version 1.0
 **/
public class WekaTest {

    private static Logger log = Logger.getLogger(WekaTest.class.getClass());

    Instances inst = null;//数据集
    StringBuffer outBuff = new StringBuffer();
    String name = (new SimpleDateFormat("HH:mm:ss - ")).format(new Date());
    String cname = "";
    String cmd = "";
    Boolean crossValidation = true;//是否选择分层交叉校验
    int crossValidationText = 10;//分层交叉校验值
    int testMode = 1;// 1:分层交叉校验 2:按比例拆分 3:使用训练集 4.选择测试集
    double percent = 66;
    AbstractClassifier classifier = new ZeroR();//默认选择ZeroR算法
    ConverterUtils.DataSource source;
    long trainTimeStart = 0, trainTimeElapsed = 0;
    long testTimeStart = 0, testTimeElapsed = 0;
    boolean outputModel = true;
    String grph = null;//可以汇画的算法
    Evaluation eval = null;
    boolean costMatrixEditor = false;//是否选择成本矩阵
    protected CostMatrixEditor costMatrixEditorValue = new CostMatrixEditor();//成本矩阵值
    CostMatrix costMatrix = null;//成本矩阵
    ClassifierErrorsPlotInstances plotInstances = ExplorerDefaults.getClassifierErrorsPlotInstances();;//错误可视化视图
    boolean collectPredictionsForEvaluation = true;
    AbstractOutput classificationOutput = null;
    Instances userTestStructure = null;
    boolean m_PreserveOrderBut = true;
    boolean outputPredictionsText = false;
    boolean outputSummary = true;
    boolean outputEntropy = true;
    boolean outputPerClass = true;
    boolean outputConfusion = true;
    Classifier fullClassifier = null;
    boolean m_OutputSourceCode = true;
    String randomSeedText = "1";//随机种子进行交叉验证或％拆分
    protected JTextField m_SourceCodeClass = new JTextField("WekaClassifier", 10);
    protected List<String> m_selectedEvalMetrics = Evaluation
            .getAllEvaluationMetricNames();//用户选择评估指标列表
    protected GenericObjectEditor m_ClassificationOutputEditor =
            new GenericObjectEditor(true);
    Classifier template = null;
    boolean outputModelsForTrainingSplits = true;
    int classIndex = 1;

    public void wekaStart() throws Exception {

        setData();//初始化数据集

        algorithmSelection();//算法选择

        algorithProcessPrintln();//模型输出

    }

    public void algorithProcessPrintln() throws Exception {

        template = AbstractClassifier.makeCopy(classifier);

        runInformation();//输出Run information

        classifierModel();//输出Classifier model

        summary();//输出summary

        classDetailedAccuracy();//输出Detailed Accuracy By Class

        confusionMatrix();//输出Confusion Matrix
    }


    private void confusionMatrix() {
    }


    private void classDetailedAccuracy() {
    }

    private void summary() throws Exception {
        //成本矩阵@todo 成本矩阵修改
        if (costMatrixEditor) {
            costMatrixEditorValue.setValue(new CostMatrix(1));//设置成本矩阵值
            costMatrix =
                    new CostMatrix((CostMatrix) costMatrixEditorValue.getValue());
        }

        switch (testMode) {
            case 3: // Test on training
                testOrTrainingMode();
                break;
            case 1: // CV mode
                crossValidationMode();
                break;

            case 2: // Percent split
                percentSplitMode();
                break;

            case 4: // Test on user split
                testOnUserSplitMode();
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

        if (inst.attribute(classIndex).isNominal()) {

            if (outputPerClass) {
                outBuff.append(eval.toClassDetailsString() + "\n");
            }

            if (outputConfusion) {
                outBuff.append(eval.toMatrixString() + "\n");
            }
        }
        if ((fullClassifier instanceof Sourcable)
                && m_OutputSourceCode) {
            outBuff.append("=== Source code ===\n\n");
            outBuff.append(Evaluation.wekaStaticWrapper(
                    ((Sourcable) fullClassifier), m_SourceCodeClass.getText()));
        }
        System.out.println(outBuff);
    }

    private void testOnUserSplitMode() throws Exception {
        log.info("Evaluating on test data...");
        eval = new Evaluation(inst, costMatrix);
        // make adjustments if the classifier is an InputMappedClassifier
        eval =
                setupEval(eval, classifier, inst, costMatrix, plotInstances,
                        classificationOutput, false, collectPredictionsForEvaluation);
        plotInstances.setInstances(userTestStructure);
        eval.setMetricsToDisplay(m_selectedEvalMetrics);

        // plotInstances.setEvaluation(eval);
        plotInstances.setUp();

        if (outputPredictionsText) {
            printPredictionsHeader(outBuff, classificationOutput,
                    "test set");
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
            log.info("Performing batch prediction with batch size " + batchSize);
        }
        testTimeStart = System.currentTimeMillis();
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
                    log.info("Evaluating on test data. Processed "
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
                    log.info("Evaluating on test data. Processed "
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
    }

    private void percentSplitMode() throws Exception {
        int rnd;
        if (!m_PreserveOrderBut) {
            log.info("Randomizing instances...");
            try {
                rnd = Integer.parseInt(randomSeedText.trim());
            } catch (Exception ex) {
                log.info("Trouble parsing random seed value");
                rnd = 1;
            }
            inst.randomize(new Random(rnd));
        }
        int trainSize =
                (int) Math.round(inst.numInstances() * percent / 100);
        int testSize = inst.numInstances() - trainSize;
        Instances train = new Instances(inst, 0, trainSize);
        Instances test = new Instances(inst, trainSize, testSize);
        log.info("Building model on training split ("
                + trainSize + " instances)...");
        Classifier current = null;
        try {
            current = AbstractClassifier.makeCopy(template);
        } catch (Exception ex) {
            log.info("Problem copying classifier: "
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
        eval.setMetricsToDisplay(m_selectedEvalMetrics);

        // plotInstances.setEvaluation(eval);
        plotInstances.setUp();
        log.info("Evaluating on test split...");

        if (outputPredictionsText) {
            printPredictionsHeader(outBuff, classificationOutput,
                    "test split");
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
                    log.info("Evaluating on test split. Processed "
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
    }

    private void crossValidationMode() throws Exception {
        log.info("Randomizing instances...");
        int rnd = 1;
        try {
            rnd = Integer.parseInt(randomSeedText.trim());
            // System.err.println("Using random seed "+rnd);
        } catch (Exception ex) {
            log.info("Trouble parsing random seed value");
            rnd = 1;
        }
        Random random = new Random(rnd);
        inst.randomize(random);
        if (inst.attribute(classIndex).isNominal()) {
            log.info("Stratifying instances...");
            inst.stratify(crossValidationText);
        }
        eval = new Evaluation(inst, costMatrix);

        // make adjustments if the classifier is an InputMappedClassifier
        eval = setupEval(eval, classifier, inst, costMatrix, plotInstances,
                classificationOutput, false, collectPredictionsForEvaluation);
        eval.setMetricsToDisplay(m_selectedEvalMetrics);

        // plotInstances.setEvaluation(eval);
        plotInstances.setUp();

//        if (outputPredictionsText) {
//            printPredictionsHeader(outBuff, classificationOutput, "test data");
//        }

        // Make some splits and do a CV
        for (int fold = 0; fold < crossValidationText; fold++) {
            log.info("Creating splits for fold " + (fold + 1)
                    + "...");
            Instances train = inst.trainCV(crossValidationText, fold, random);

            // make adjustments if the classifier is an
            // InputMappedClassifier
            eval = setupEval(eval, classifier, train, costMatrix, plotInstances,
                    classificationOutput, true, collectPredictionsForEvaluation);
            eval.setMetricsToDisplay(m_selectedEvalMetrics);

            // eval.setPriors(train);
            log.info("Building model for fold " + (fold + 1)
                    + "...");
            Classifier current = null;
            try {
                current = AbstractClassifier.makeCopy(template);
//                if (current instanceof LogHandler) {
//                    ((LogHandler) current).setLog(m_Log);
//                }
            } catch (Exception ex) {
                log.info("Problem copying classifier: "
                        + ex.getMessage());
            }
            current.buildClassifier(train);
            if (outputModelsForTrainingSplits) {
                outBuff.append("\n=== Classifier model for fold "
                        + (fold + 1) + " ===\n\n");
                outBuff.append(current.toString() + "\n");
            }
            Instances test = inst.testCV(crossValidationText, fold);
            log.info("Evaluating model for fold " + (fold + 1)
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

    private void testOrTrainingMode() throws Exception {
        log.info("Evaluating on training data...");
        eval = new Evaluation(inst, costMatrix);

        // make adjustments if the classifier is an InputMappedClassifier
        eval =
                setupEval(eval, classifier, inst, costMatrix, plotInstances,
                        classificationOutput, false, collectPredictionsForEvaluation);
        eval.setMetricsToDisplay(m_selectedEvalMetrics);

        // plotInstances.setEvaluation(eval);
        plotInstances.setUp();

        if (outputPredictionsText) {
            printPredictionsHeader(outBuff, classificationOutput,
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
                    log.info("Evaluating on training data. Processed "
                            + jj + " instances...");
                }
            }
        }
        testTimeElapsed = System.currentTimeMillis() - testTimeStart;
        if (outputPredictionsText) {
            classificationOutput.printFooter();
        }
        if (outputPredictionsText
                && classificationOutput.generatesOutput()) {
            outBuff.append("\n");
        }
        outBuff.append("=== Evaluation on training set ===\n");
        return;
    }

    /**
     * Configures an evaluation object with respect to a classifier, cost matrix,
     * output and plotting.
     *
     * @param eval                 the Evaluation object to configure
     * @param classifier           the Classifier being used
     * @param inst                 the Instances involved
     * @param costMatrix           a cost matrix (if any)
     * @param plotInstances        a ClassifierErrorsPlotInstances for visualization of
     *                             errors (can be null)
     * @param classificationOutput an output object for printing predictions (can
     *                             be null)
     * @param onlySetPriors        true to only set priors
     * @param collectPredictions   whether to collect predictions for calculating ROC, etc.
     * @return the configured Evaluation object
     * @throws Exception if a problem occurs
     */
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

    private void classifierModel() throws Exception {
        if (outputModel || (testMode == 3) || (testMode == 4)) {
            log.info("Building model on training data...");

            trainTimeStart = System.currentTimeMillis();
            classifier.buildClassifier(inst);
            trainTimeElapsed = System.currentTimeMillis() - trainTimeStart;
        }
        outBuff.append("=== Classifier model (full training set) ===\n\n");
        outBuff.append(classifier.toString() + "\n");
        outBuff.append("\nTime taken to build model: "
                + Utils.doubleToString(trainTimeElapsed / 1000.0, 2)
                + " seconds\n\n");
        //生成图像@TODO 有无办法获得
        if (classifier instanceof Drawable) {
            grph = null;
            try {
                grph = ((Drawable) classifier).graph();
            } catch (Exception ex) {
            }
        }
    }

    /**
     * outputs the header for the predictions on the data.
     *
     * @param outBuff              the buffer to add the output to
     * @param classificationOutput for generating the classification output
     * @param title                the title to print
     */
    protected void printPredictionsHeader(StringBuffer outBuff,
                                          AbstractOutput classificationOutput, String title) {
        if (classificationOutput.generatesOutput()) {
            outBuff.append("=== Predictions on " + title + " ===\n\n");
        }
        classificationOutput.printHeader();
    }

    public void algorithmSelection() throws Exception {
        classifier = new J48();
        String[] options = {"-C", "0.25", "-M","2"};
        classifier.setOptions(options);
    }

    /**
     * 输出 === Run information ===信息
     *
     * @throws Exception
     */
    private void runInformation() throws Exception {

        outBuff.append("=== Run information ===\n\n");
        cname = classifier.getClass().getName();
        outBuff.append("Scheme: " + cname);
        outBuff.append("\n");
        outBuff.append("Relation:     " + inst.relationName() + '\n');
        outBuff.append("Instances:    " + inst.numInstances() + '\n');
        outBuff.append("Attributes:   " + inst.numAttributes() + '\n');
        if (inst.numAttributes() < 100) {
            for (int i = 0; i < inst.numAttributes(); i++) {
                outBuff.append("              " + inst.attribute(i).name()
                        + '\n');
            }
        } else {
            outBuff.append("              [list of attributes omitted]\n");
        }
        outBuff.append("Test mode:    ");
        switch (testMode) {
            case 3: // Test on training
                outBuff.append("evaluate on training data\n");
                break;
            case 1: // CV mode
                outBuff.append("" + crossValidationText + "-fold cross-validation\n");
                break;
            case 2: // Percent split
                outBuff.append("split " + percent + "% train, remainder test\n");
                break;
            case 4: // Test on user split
                if (source.isIncremental()) {
                    outBuff.append("user supplied test set: "
                            + " size unknown (reading incrementally)\n");
                } else {
                    outBuff.append("user supplied test set: "
                            + source.getDataSet().numInstances() + " instances\n");
                }
                break;
        }
    }


    public void setData() throws Exception {
        String path = System.getProperty("user.dir") + "\\src\\main\\resources\\data\\weather" +
                ".nominal.arff";
        inst = ConverterUtils.DataSource.read(path);
        inst.setClassIndex(inst.numAttributes() - 1);
    }

    public static void main(String[] args) throws Exception {

        WekaTest wekaTest = new WekaTest();
        wekaTest.wekaStart();


    }


}
