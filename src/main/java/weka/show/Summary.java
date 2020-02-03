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


    public static String GeneratesSummary() {
        try {
            return GeneralClassification.runProcess();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


}
