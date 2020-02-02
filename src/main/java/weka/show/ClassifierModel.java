package weka.show;

import weka.algorithm.GeneralClassification;
import weka.core.Utils;
import weka.initData.GeneralData;

/**
 * @ClassName ClassifierModel
 * @Description ClassifierModel
 * @Author 林春永
 * @Date 2020/2/2
 * @Version 1.0
 **/
public class ClassifierModel {

    public static String GeneratesClassifierModel() {
        long trainTimeElapsed = GeneralClassification.runClassification();
        StringBuffer outBuff = new StringBuffer();
        outBuff.append("=== Classifier model (full training set) ===\n\n");
        //输出决策过程
        outBuff.append(GeneralClassification.getClassifier().toString() + "\n");
        outBuff.append("\nTime taken to build model: "
                + Utils.doubleToString(trainTimeElapsed / 1000.0, 2)
                + " seconds\n\n");
        return outBuff.toString();
    }

}
