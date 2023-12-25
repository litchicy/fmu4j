import no.ntnu.ihb.fmi4j.importer.fmi1.CoSimulationSlave;
import no.ntnu.ihb.fmi4j.importer.fmi1.Fmu;
import no.ntnu.ihb.fmi4j.modeldescription.CoSimulationModelDescription;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


/**
 * 弹力球仿真测试
 *
 * @author cy
 * @date 2023/12/05
 */
public class Demo {
    public static void main(String[] args) {
        cs_simulate();
        getXML();
    }

    /**
     * 仿真
     */
    private static void cs_simulate() {
        try {
            Fmu fmu = Fmu.from(new File("cs/bouncingBall.fmu")); //URLs are also supported
            CoSimulationSlave slave = fmu.asCoSimulationFmu().newInstance();
            slave.setupExperiment();
            //--relative-tolerance : CVode方法使用，指定tolerance
//            slave.setupExperiment(0,10,x); x表示tolerance
            slave.enterInitializationMode();
            slave.exitInitializationMode();
            CoSimulationModelDescription modelDescription = slave.getModelDescription();
            // 输入参数：初始值
            long h = modelDescription.getValueReference("h");
            long e = modelDescription.getValueReference("e");
            long[] write_vr = {h, e};
            double[] vr_value = {100, 0.07};
            slave.writeReal(write_vr, vr_value);
            // 输出参数
            double[] ref = new double[2];
            long[] vr = {
                    modelDescription.getVariableByName("h").getValueReference(),
                    modelDescription.getVariableByName("v").getValueReference()
//                    modelDescription.getVariableByName("der(h)").getValueReference(),
//                    modelDescription.getVariableByName("der(v)").getValueReference()
            };
            //slave.simpleSetup(); //进行简单的设置，准备模型以进行仿真。
            double t = 0;
            double stop = 10;
            double stepSize = 10.0 / 100;
            FileWriter fw = new FileWriter("output2.csv");
            // 写入 CSV 文件头部
//            fw.write("Time,h,der(h),der(v)\n");
            fw.write("Time,h,v\n");
            while(t <= stop) { // 循环仿真
//                System.out.printf("t=%f, h=%f, der(h)=%f, der(v)=%f.%n", t, ref[0], ref[1], ref[2]);
                if(!slave.doStep(t, stepSize)) {
                    break;
                }
                // 判断是否为正常状态
                if(!slave.readReal(vr, ref).isOK()) {
                    break;
                }
                // String outputString = String.format("t=%f, h=%f, der(h)=%f, der(v)=%f.%n", t, ref[0], ref[1], ref[2]);
//                String csvLine = String.format("%f,%f,%f,%f%n", t, ref[0], ref[1], ref[2]);
                String csvLine = String.format("%f,%f,%f%n", t, ref[0], ref[1]);
                // 将信息写入文件
                fw.write(csvLine);
                t += stepSize;
            }
            slave.terminate(); //or close, try with resources is also supported
            fmu.close();
            fw.close();
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析模型文件获取xml数据信息
     */
    private static void getXML() {
        try {
            Fmu fmu = Fmu.from(new File("cs/bouncingBall.fmu")); //URLs are also supported
            FileWriter fw = new FileWriter("output.xml");
            String modelDescriptionXml = fmu.getModelDescriptionXml();
            fw.write(modelDescriptionXml);
            fw.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
