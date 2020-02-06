package weka.algorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.core.*;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.gui.explorer.ClustererAssignmentsPlotInstances;
import weka.gui.explorer.ExplorerDefaults;
import weka.initData.GeneralData;

import javax.swing.*;
import java.util.Random;

/**
 * @ClassName Cluster
 * @Description 聚类接口
 * @Author 林春永
 * @Date 2020/2/6
 * @Version 1.0
 **/
public class TestCluster {

    static Logger LOGGER = LoggerFactory.getLogger(TestCluster.class);

    static JList ignoreKeyList = null;
    static Clusterer clusterer = null;
    static int[] ignoredAtts = null;
    static JRadioButton classesToClustersBut = null;
    static JComboBox classCombo = null;
    static int testMode = 0;
    static int percent = 66;
    static long trainTimeStart = 0, trainTimeElapsed = 0;
    static Instances userTest = null;

    static Instances inst = GeneralData.getInstances();
    static Instances trainInst = new Instances(inst);
    static ClustererAssignmentsPlotInstances plotInstances =
            ExplorerDefaults.getClustererAssignmentsPlotInstances();

    public static void main(String[] args) throws Exception {

        plotInstances.setClusterer((Clusterer) null);
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
            if (!ignoreKeyList.isSelectionEmpty()) {
                int[] indices = ignoreKeyList.getSelectedIndices();
                for (int i = 0; i < indices.length; i++) {
                    selected[indices[i]] = false;
                }
            }
            if (classesToClustersBut.isSelected()) {
                selected[classCombo.getSelectedIndex()] = false;
            }
            for (int i = 0; i < inst.numAttributes(); i++) {
                if (selected[i]) {
                    outBuff.append("              " + inst.attribute(i).name()
                            + '\n');
                }
            }
            if (!ignoreKeyList.isSelectionEmpty()
                    || classesToClustersBut.isSelected()) {
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

        if (!ignoreKeyList.isSelectionEmpty()) {
            ignoredAtts = ignoreKeyList.getSelectedIndices();
        }

        if (classesToClustersBut.isSelected()) {
            // add class to ignored list
            if (ignoredAtts == null) {
                ignoredAtts = new int[1];
                ignoredAtts[0] = classCombo.getSelectedIndex();
            } else {
                int[] newIgnoredAtts = new int[ignoredAtts.length + 1];
                System.arraycopy(ignoredAtts, 0, newIgnoredAtts, 0,
                        ignoredAtts.length);
                newIgnoredAtts[ignoredAtts.length] =
                        classCombo.getSelectedIndex();
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

        // Build the model and output it.
        LOGGER.info("Building model on training data...");

        // remove the class attribute (if set) and build the clusterer
        trainTimeStart = System.currentTimeMillis();
        clusterer.buildClusterer(removeClass(trainInst));
        trainTimeElapsed = System.currentTimeMillis() - trainTimeStart;

        // if (testMode == 2) {
        outBuff
                .append("\n=== Clustering model (full training set) ===\n\n");

        outBuff.append(clusterer.toString() + '\n');
        outBuff
                .append("\nTime taken to build model (full training data) : "
                        + Utils.doubleToString(trainTimeElapsed / 1000.0, 2)
                        + " seconds\n\n");
        // }
//        if (clusterer instanceof Drawable) {
//            try {
//                grph = ((Drawable) clusterer).graph();
//            } catch (Exception ex) {
//            }
//        }
//        // copy full model for output
//        SerializedObject so = new SerializedObject(clusterer);
//        fullClusterer = (Clusterer) so.getObject();

        ClusterEvaluation eval = new ClusterEvaluation();
        eval.setClusterer(clusterer);
        switch (testMode) {
            case 3:
            case 5: // Test on training
                LOGGER.info("Clustering training data...");
                eval.evaluateClusterer(trainInst, "", false);
                plotInstances.setInstances(inst);
                plotInstances.setClusterEvaluation(eval);
                outBuff
                        .append("=== Model and evaluation on training set ===\n\n");
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
                clusterer.buildClusterer(train);
                trainTimeElapsed = System.currentTimeMillis() - trainTimeStart;
                LOGGER.info("Evaluating on test split...");
                eval.evaluateClusterer(test, "", false);
                plotInstances.setInstances(testVis);
                plotInstances.setClusterEvaluation(eval);
                outBuff.append("=== Model and evaluation on test split ===\n");
                outBuff.append(clusterer.toString() + "\n");
                outBuff
                        .append("\nTime taken to build model (percentage split) : "
                                + Utils.doubleToString(trainTimeElapsed / 1000.0, 2)
                                + " seconds\n\n");
                break;

            case 4: // Test on user split
                LOGGER.info("Evaluating on test data...");
                Instances userTestT = new Instances(userTest);
                if (!ignoreKeyList.isSelectionEmpty()) {
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
        if (classesToClustersBut.isSelected()) {
            int classIndex = classCombo.getSelectedIndex();
            if (ignoreKeyList.isSelectedIndex(classIndex)) {
                ignoreKeyList.removeSelectionInterval(classIndex, classIndex);
            }
        }
        int[] selected = ignoreKeyList.getSelectedIndices();
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
