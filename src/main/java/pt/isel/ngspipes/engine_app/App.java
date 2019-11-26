package pt.isel.ngspipes.engine_app;

import org.apache.commons.cli.ParseException;
import pt.isel.ngspipes.dsl_parser.domain.NGSPipesParser;
import pt.isel.ngspipes.dsl_parser.transversal.ParserException;
import pt.isel.ngspipes.engine_common.entities.Arguments;
import pt.isel.ngspipes.engine_common.entities.StateEnum;
import pt.isel.ngspipes.engine_common.entities.contexts.Pipeline;
import pt.isel.ngspipes.engine_common.executionReporter.ConsoleReporter;
import pt.isel.ngspipes.engine_common.interfaces.IExecutor;
import pt.isel.ngspipes.engine_core.exception.EngineException;
import pt.isel.ngspipes.engine_core.implementations.Engine;
import pt.isel.ngspipes.engine_core.interfaces.IEngine;
import pt.isel.ngspipes.engine_executor.entities.MesosInfo;
import pt.isel.ngspipes.engine_executor.implementations.LinuxExecutor;
import pt.isel.ngspipes.engine_executor.implementations.MesosExecutor;
import pt.isel.ngspipes.engine_executor.implementations.VagrantExecutor;
import pt.isel.ngspipes.pipeline_descriptor.IPipelineDescriptor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class App {

    public static void main(String[] args) {
        ArgumentsParser argsParser = new ArgumentsParser();
        try {
            ConsoleArguments arguments = argsParser.parse(args);
            if (arguments != null) {
                IExecutor executor = getExecutor(arguments);
                IEngine engine = new Engine(executor);
                System.out.println("Execution begun");
                Pipeline pipeline;
                if (arguments.pipes != null && !arguments.pipes.isEmpty()) {
                    IPipelineDescriptor pipelineDescriptor = getPipelineDescriptor(arguments);
                    pipeline = engine.execute(pipelineDescriptor, arguments.parameters, getPipelineArguments(arguments));
                } else {
                    String intermediateRepresentation = arguments.intermediateRepresentation;
                    String ir = getIR(intermediateRepresentation);
                    pipeline = engine.execute(ir, arguments);
                }
                while (true) {
                    if (pipeline.getState().getState().equals(StateEnum.SUCCESS))
                        break;
                    if (pipeline.getState().getState().equals(StateEnum.FAILED))
                        break;
                    try {
                        Thread.sleep(100000);
                        System.out.println(pipeline.getState().getState());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    System.out.println("APP Finished");
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        } catch (ParseException | IOException | ParserException | EngineException e) {
            System.out.println(e.getMessage());
        }

    }

    private static IExecutor getExecutor(ConsoleArguments arguments) {

        ConsoleReporter reporter = new ConsoleReporter();
        if (arguments.executorType.equalsIgnoreCase("mesos")) {
            return getMesosExecutor(arguments, reporter);
        } else if (arguments.executorType.equalsIgnoreCase("linux")) {
            return getLinuxExecutor(arguments, reporter);
        } else if (arguments.executorType.equalsIgnoreCase("vagrant")) {
            return getVagrantExecutor(arguments, reporter);
        }
        return null;
    }

    private static IExecutor getVagrantExecutor(ConsoleArguments arguments, ConsoleReporter reporter) {
        if (!arguments.workingDirectory.isEmpty())
            return new VagrantExecutor(reporter, arguments.workingDirectory);
        return new VagrantExecutor(reporter);
    }

    private static IExecutor getLinuxExecutor(ConsoleArguments arguments, ConsoleReporter reporter) {
        if (!arguments.workingDirectory.isEmpty())
            return new LinuxExecutor(reporter, arguments.workingDirectory);
        return new LinuxExecutor(reporter);
    }

    private static IExecutor getMesosExecutor(ConsoleArguments arguments, ConsoleReporter reporter) {
        MesosInfo mesosInfo = new MesosInfo(arguments.mesosSshHost, arguments.mesosSshUsername,
                                            arguments.mesosSshPass, arguments.mesosSshPort,
                                            arguments.mesosChronosEndpoint, arguments.mesosBaseDir);
//            MesosInfo mesosInfo = new MesosInfo("10.62.73.5", "calmen", "ngs##19",
//                    22, "http://10.62.73.5:4400", "/home/calmen/pipes");
        if (!arguments.workingDirectory.isEmpty())
            return new MesosExecutor(reporter, mesosInfo, arguments.workingDirectory);
        return new MesosExecutor(reporter, mesosInfo);
    }

    private static String getIR(String irPath) throws IOException {
        return readContent(irPath);
    }

    private static IPipelineDescriptor getPipelineDescriptor(ConsoleArguments arguments) throws IOException, ParserException {
        System.out.println("Arguments parsed");
        String pipesContent = readContent(arguments.pipes);
        NGSPipesParser parser = new NGSPipesParser();
        IPipelineDescriptor pipelineDescriptor = parser.getFromString(pipesContent);
        System.out.println("Pipeline loaded");
        return pipelineDescriptor;
    }

    private static Arguments getPipelineArguments(ConsoleArguments arguments) {
        return new Arguments(arguments.outPath, arguments.workingDirectory, arguments.cpus,
                             arguments.mem, arguments.disk, arguments.parallel);
    }

    private static String readContent(String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        String str;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            while ((str = br.readLine()) != null) {
                sb.append(str);
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
