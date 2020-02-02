package weka.show;

import weka.algorithm.GeneralClassification;
import weka.classifiers.AbstractClassifier;
import weka.core.Instances;
import weka.core.Utils;
import weka.initData.GeneralData;

/**
 * @ClassName RunInformation
 * @Description RunInformation信息
 * @Author 林春永
 * @Date 2020/2/2
 * @Version 1.0
 **/
public class RunInformation {

    public static String GeneratesRunInformation() {
        AbstractClassifier classifier = GeneralClassification.getClassifier();
        Instances inst = GeneralData.getInstances();
        StringBuffer outBuff = new StringBuffer();
        outBuff.append("=== Run information ===\n\n");
        String cname = classifier.getClass().getName();
        outBuff.append("Scheme: " + cname + " " + Utils.joinOptions(classifier.getOptions()));
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
        switch (GeneralClassification.getTestMode()) {
            case 3: // Test on training
                outBuff.append("evaluate on training data\n");
                break;
            case 1: // CV mode
                outBuff.append("" + GeneralClassification.getCrossValidationText() + "-fold " +
                        "cross-validation\n");
                break;
            case 2: // Percent split
                outBuff.append("split " + GeneralClassification.getPercent() + "% train, " +
                        "remainder test\n");
                break;
            case 4: // Test on user split
                if (GeneralData.getSource().isIncremental()) {
                    outBuff.append("user supplied test set: "
                            + " size unknown (reading incrementally)\n");
                } else {
                    try {
                        outBuff.append("user supplied test set: "
                                + GeneralData.getSource().getDataSet().numInstances() + " " +
                                "instances\n");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
        return outBuff.toString();
    }
}
