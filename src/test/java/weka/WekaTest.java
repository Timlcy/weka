package weka;

import org.junit.jupiter.api.Test;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.core.converters.ConverterUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * @ClassName WekaTest
 * @Description
 * @Author 林春永
 * @Date 2020/1/5
 * @Version 1.0
 **/
public class WekaTest {

    Instances inst = null;//数据集

    StringBuffer outBuff = new StringBuffer();
    String name = (new SimpleDateFormat("HH:mm:ss - ")).format(new Date());
    String cname = "";
    String cmd = "";
    Boolean crossValidation = true;//是否选择分层交叉校验
    int crossValidationText = 10;//分层交叉校验值
    int testMode = 0;
    int numFolds = 10;//交叉校验次数
    double percent = 66;
    AbstractClassifier classifier=new ZeroR();//默认选择ZeroR算法

    @Test
    public void wekaStart() throws Exception {

        setData();//初始化数据集

        modeSelection();//模式选择

        algorithmSelection();//算法选择


    }

    private void algorithmSelection() throws Exception {
        classifier = new J48();
        String[] options = {"-M", "5", "-R"};
        classifier.setOptions(options);
        classifier.buildClassifier(inst);
    }

    private void modeSelection() throws Exception {


        Evaluation eval = null;
        eval = new Evaluation(inst);
        if (crossValidation) {
            eval.crossValidateModel(classifier, inst, 10, new Random(1));
        }
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
    }

    private void setData() throws Exception {
        inst = ConverterUtils.DataSource.read("C:\\Users\\timlcy\\Desktop\\学习\\data\\weather" +
                ".nominal.arff");
        inst.setClassIndex(inst.numAttributes() - 1);
    }

    public static void main(String[] args) throws Exception {


//        if (cname.startsWith("weka.classifiers.")) {
//            name = name + cname.substring("weka.classifiers.".length());
//        } else {
//            name = name + cname;
//        }
//        cmd = classifier.getClass().getName();
//        if (classifier instanceof OptionHandler) {
//            cmd += " " + Utils.joinOptions(((OptionHandler) classifier).getOptions());
//        }
//            switch (testMode) {
//                case 3: // Test on training
//                    outBuff.append("evaluate on training data\n");
//                    break;
//                case 1: // CV mode
//                    outBuff.append("" + numFolds + "-fold cross-validation\n");
//                    break;
//                case 2: // Percent split
//                    outBuff.append("split " + percent + "% train, remainder test\n");
//                    break;
//                case 4: // Test on user split
//                    if (source.isIncremental()) {
//                        outBuff.append("user supplied test set: "
//                                + " size unknown (reading incrementally)\n");
//                    } else {
//                        outBuff.append("user supplied test set: "
//                                + source.getDataSet().numInstances() + " instances\n");
//                    }
//                    break;
//            }


//        outBuff.append(eval.toClassDetailsString());
//        outBuff.append(eval.toSummaryString("\nResult", false));
//        System.out.println(outBuff);

    }


}
