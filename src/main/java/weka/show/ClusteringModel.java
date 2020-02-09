package weka.show;

import weka.algorithm.GeneralClassification;
import weka.algorithm.GeneralCluster;
import weka.clusterers.AbstractClusterer;
import weka.clusterers.Clusterer;
import weka.core.Utils;
import weka.initData.GeneralData;

/**
 * @ClassName ClusteringModel
 * @Description Clustering model聚类模式
 * @Author 林春永
 * @Date 2020/2/9
 * @Version 1.0
 **/
public class ClusteringModel {

    public static String
    GeneratesClusteringModel() {
       Clusterer clusterer = GeneralCluster.getCluster();
        StringBuilder outBuff = new StringBuilder();
        long trainTimeElapsed = 0;
        try {
            trainTimeElapsed = GeneralCluster.buildClusterer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        outBuff
                .append("\n=== Clustering model (full training set) ===\n\n");

        outBuff.append(clusterer.toString() + '\n');
        outBuff
                .append("\nTime taken to build model (full training data) : "
                        + Utils.doubleToString(trainTimeElapsed / 1000.0, 2)
                        + " seconds\n\n");
        return outBuff.toString();
    }

    public static String clusterResults() {
        String sb = "";
        try {
            sb = GeneralCluster.clusterResults();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb;

    }

}
