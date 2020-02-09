package weka.show;

import weka.algorithm.GeneralClassification;
import weka.algorithm.GeneralCluster;
import weka.classifiers.AbstractClassifier;
import weka.clusterers.AbstractClusterer;
import weka.clusterers.Clusterer;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.initData.GeneralData;

import java.util.List;

/**
 * @ClassName RunInformation
 * @Description RunInformation信息
 * @Author 林春永
 * @Date 2020/2/2
 * @Version 1.0
 **/
public class RunInformation {

    public static String classifierRunInformation() {
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

    public static String clustererRunInformation() {
       Clusterer clusterer = GeneralCluster.getCluster();
        Instances inst = GeneralData.getInstances();
        List<Integer> ignoreKeyList = GeneralCluster.getIgnoreKeyList();
        boolean classesToClustersBut = GeneralCluster.isClassesToClustersBut();
        int classCombo = GeneralCluster.getClassCombo();
        int[] ignoredAtts = GeneralCluster.getIgnoredAtts();
        int testMode = GeneralCluster.getTestMode();
        int percent = GeneralCluster.getPercent();
        Instances userTest = GeneralCluster.getUserTest();
        StringBuffer outBuff = new StringBuffer();
        // Output some header information
        outBuff.append("=== Run information ===\n\n");
        outBuff.append("Scheme:       " + clusterer.getClass().getName());
        if (clusterer instanceof OptionHandler) {
            String[] o = ((OptionHandler) clusterer).getOptions();
            outBuff.append(" " + Utils.joinOptions(o));
        }
        outBuff.append("\n");
        outBuff.append("Relation:     " + inst.relationName() + '\n');
        outBuff.append("Instances:    " + inst.numInstances() + '\n');
        outBuff.append("Attributes:   " + inst.numAttributes() + '\n');
        if (inst.numAttributes() < 100) {
            boolean[] selected = new boolean[inst.numAttributes()];
            for (int i = 0; i < inst.numAttributes(); i++) {
                selected[i] = true;
            }
            if (!(ignoreKeyList == null || ignoreKeyList.isEmpty())) {
                int[] indices = ignoreKeyList.stream().mapToInt(Integer::valueOf).toArray();
                for (int i = 0; i < indices.length; i++) {
                    selected[indices[i]] = false;
                }
            }
            if (classesToClustersBut) {
                selected[classCombo] = false;
            }
            for (int i = 0; i < inst.numAttributes(); i++) {
                if (selected[i]) {
                    outBuff.append("              " + inst.attribute(i).name()
                            + '\n');
                }
            }
            if (!(ignoreKeyList == null || ignoreKeyList.isEmpty())
                    || classesToClustersBut) {
                outBuff.append("Ignored:\n");
                for (int i = 0; i < inst.numAttributes(); i++) {
                    if (!selected[i]) {
                        outBuff.append("              " + inst.attribute(i).name()
                                + '\n');
                    }
                }
            }
        } else {
            outBuff.append("              [list of attributes omitted]\n");
        }

        if (!(ignoreKeyList == null || ignoreKeyList.isEmpty())) {
            ignoredAtts = ignoreKeyList.stream().mapToInt(Integer::valueOf).toArray();
        }

        if (classesToClustersBut) {
            // add class to ignored list
            if (ignoredAtts == null) {
                ignoredAtts = new int[1];
                ignoredAtts[0] = classCombo;
            } else {
                int[] newIgnoredAtts = new int[ignoredAtts.length + 1];
                System.arraycopy(ignoredAtts, 0, newIgnoredAtts, 0,
                        ignoredAtts.length);
                newIgnoredAtts[ignoredAtts.length] =
                        classCombo;
                ignoredAtts = newIgnoredAtts;
            }
        }

        outBuff.append("Test mode:    ");
        switch (testMode) {
            case 3: // Test on training
                outBuff.append("evaluate on training data\n");
                break;
            case 2: // Percent split
                outBuff.append("split " + percent + "% train, remainder test\n");
                break;
            case 4: // Test on user split
                outBuff.append("user supplied test set: "
                        + userTest.numInstances() + " instances\n");
                break;
            case 5: // Classes to clusters evaluation on training
                outBuff.append("Classes to clusters evaluation on training data");

                break;
        }
        outBuff.append("\n");
        return outBuff.toString();
    }

}
