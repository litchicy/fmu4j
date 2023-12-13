

import no.ntnu.ihb.fmi4j.FmiStatus;
import no.ntnu.ihb.fmi4j.importer.fmi1.CoSimulationSlave;
import no.ntnu.ihb.fmi4j.importer.fmi1.Fmu;
import no.ntnu.ihb.fmi4j.importer.fmi1.ModelExchangeFmu;
import no.ntnu.ihb.fmi4j.modeldescription.CoSimulationModelDescription;
import no.ntnu.ihb.fmi4j.modeldescription.util.FmiModelDescriptionUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


/**
 * 弹力球仿真测试，数据输出成功
 *
 * @author cy
 * @date 2023/12/05
 */
public class Demo {
    public static void main(String[] args) {
        Fmu fmu = null; //URLs are also supported
        try {
            fmu = Fmu.from(new File("cs/bouncingBall.fmu"));
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }

        CoSimulationSlave slave = fmu.asCoSimulationFmu().newInstance();
        CoSimulationModelDescription modelDescription = slave.getModelDescription();
        // 输出参数
        double[] ref = new double[3];
        long[] vr = {modelDescription.getVariableByName("h").getValueReference(),
                modelDescription.getVariableByName("der(h)").getValueReference(),
                modelDescription.getVariableByName("der(v)").getValueReference()};
        // 输入参数：初始值
        long h = modelDescription.getValueReference("h");
        long e = modelDescription.getValueReference("e");
        long[] write_vr = {h, e};
        double[] vr_value = {100, 0.07};
        slave.writeReal(write_vr, vr_value);

//        slave.simpleSetup(); //进行简单的设置，准备模型以进行仿真。
        slave.setupExperiment();
        slave.enterInitializationMode();
        slave.exitInitializationMode();
        double t = 0;
        double stop = 100;
        double stepSize = 2.0/10;
        try(FileWriter fw = new FileWriter("output.csv")) {
            // 写入 CSV 文件头部
            fw.write("Time,h,der(h),der(v)\n");
            while(t <= stop) { // 循环仿真
                if (!slave.doStep(t, stepSize)) {
                    break;
                }
                // 判断是否为正常状态
                if(!slave.readReal(vr, ref).isOK()) {
                    break;
                }
//                System.out.printf("t=%f, h=%f, der(h)=%f, der(v)=%f.%n", t, ref[0], ref[1], ref[2]);
                String outputStringInTXT = String.format("t=%f, h=%f, der(h)=%f, der(v)=%f.%n", t, ref[0], ref[1], ref[2]);

                String csvLine = String.format("%f,%f,%f,%f%n", t, ref[0], ref[1], ref[2]);
                // 将信息写入文件
                fw.write(csvLine);

                t += stepSize;
            }
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
        slave.terminate(); //or close, try with resources is also supported
        fmu.close();

    }
}
