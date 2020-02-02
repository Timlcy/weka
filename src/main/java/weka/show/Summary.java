package weka.show;

import weka.algorithm.GeneralClassification;
import weka.classifiers.Evaluation;
import weka.classifiers.Sourcable;
import weka.classifiers.evaluation.output.prediction.AbstractOutput;
import weka.core.Utils;

/**
 * @ClassName Summary
 * @Description summary信息
 * @Author 林春永
 * @Date 2020/2/2
 * @Version 1.0
 **/
public class Summary {

    static StringBuffer outBuff = new StringBuffer();

    public static String GeneratesSummary() {
        return null;
    }

    public static void printPredictionsHeader(
            AbstractOutput classificationOutput, String title) {
        if (classificationOutput.generatesOutput()) {
            outBuff.append("=== Predictions on " + title + " ===\n\n");
        }
        classificationOutput.printHeader();

        if (GeneralClassification.isOutputPredictionsText()
                && classificationOutput.generatesOutput()) {
            outBuff.append("\n");
        }
        outBuff.append("=== Evaluation on training set ===\n");
    }

}
