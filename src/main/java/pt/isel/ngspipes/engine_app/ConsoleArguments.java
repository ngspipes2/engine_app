package pt.isel.ngspipes.engine_app;

import pt.isel.ngspipes.engine_common.entities.Arguments;

import java.util.Map;

public class ConsoleArguments extends Arguments {

    public String pipes;
    public String intermediateRepresentation;
    public String executorType;
    public int mesosSshPort;
    public String mesosSshHost;
    public String mesosSshUsername;
    public String mesosSshPass;
    public String mesosChronosEndpoint;
    public String mesosBaseDir;
    public Map<String, Object> parameters;

    public ConsoleArguments() {
    }

    public ConsoleArguments(String pipes, String outPath, String workingDirectory, int cpus, int mem, int disk,
                            boolean parallel, Map<String, Object> parameters, String executorType) {
        super(outPath, workingDirectory, cpus, mem, disk, parallel);
        this.parameters = parameters;
        this.pipes = pipes;
        this.executorType = executorType;
    }

    public ConsoleArguments(String outPath, String workingDirectory, int cpus, int mem, int disk, boolean parallel,
                            Map<String, Object> parameters, String ir, String executorType) {
        super(outPath, workingDirectory, cpus, mem, disk, parallel);
        this.parameters = parameters;
        this.executorType = executorType;
        this.intermediateRepresentation = ir;
    }
}