package pt.isel.ngspipes.engine_app;

import org.apache.commons.cli.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ArgumentsParser {

    private static final String APP_NAME = "NGSPipes Engine App";

    public static final String PIPES_PATH = "pipes";
    public static final String IR_PATH = "ir";
    public static final String OUT_PATH = "out";
    public static final String WORKING_DIRECTORY = "workingDirectory";
    public static final String PARALLEL = "parallel";
    public static final String CPUS = "cpus";
    public static final String MEM = "mem";
    public static final String DISK = "disk";
    public static final String PARAMETERS = "parameters";
    public static final String EXECUTOR_TYPE = "executorType";
    public static final String EXECUTOR_SSH_PORT = "sshPort";
    public static final String EXECUTOR_SSH_HOST = "sshHost";
    public static final String EXECUTOR_SSH_USERNAME = "sshUsername";
    public static final String EXECUTOR_SSH_PASS = "sshPass";
    public static final String EXECUTOR_CHRONOS_ENDPOINT = "sshChronosURL";
    public static final String EXECUTOR_BASE_DIR = "baseDirectory";

    public static final int DEFAULT_CPUS = 0;
    public static final int DEFAULT_MEM = 0;
    public static final int DEFAULT_DISK = 0;



    private final CommandLineParser parser = new DefaultParser();
    private final Options options = new Options();


    public ArgumentsParser(){
        options.addOption(PIPES_PATH, true, "Pipeline path (mandatory)");
        options.addOption(IR_PATH, true, "Pipeline intermediate representation");
        options.addOption(OUT_PATH, true, "Output absolute pathname (mandatory)");
        options.addOption(WORKING_DIRECTORY, true, "Working directory absolute pathname (mandatory)");
        options.addOption(CPUS, true, "Assigned cores");
        options.addOption(DISK, true, "Assigned disk space");
        options.addOption(MEM, true, "Assigned max memory in megabytes");
        options.addOption(PARAMETERS, true, "Pipeline parameters. (e.x. param_name=1,param1_name=true,param2_name=add)");
        options.addOption(PARALLEL, true, "Indicates either execution must be parallel or sequential (mandatory).");
        options.addOption(EXECUTOR_TYPE, true, "Executor to be used (mandatory e.x. mesos, vagrant or linux)");
        options.addOption(EXECUTOR_BASE_DIR, true, "Mesos host base working dir.");
        options.addOption(EXECUTOR_CHRONOS_ENDPOINT, true, "Chronos scheduler endpoint URL");
        options.addOption(EXECUTOR_SSH_HOST, true, "Chronos SSH host");
        options.addOption(EXECUTOR_SSH_PASS, true, "Chronos SSH password");
        options.addOption(EXECUTOR_SSH_USERNAME, true, "Chronos SSH username");
        options.addOption(EXECUTOR_SSH_PORT, true, "Chronos SSH port");
    }


    public ConsoleArguments parse(String[] args) throws ParseException {
        CommandLine cmdLine = parser.parse( options, args );

        // check mandatory arguments
        if (!validateMandatoryArguments(cmdLine) || !validateArguments(cmdLine)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( APP_NAME, options );
            return null;
        }

        return createArguments(cmdLine);
    }

    private boolean validateMandatoryArguments(CommandLine cmdLine){
        boolean hasPipes = cmdLine.hasOption(PIPES_PATH);
        boolean hasIr = cmdLine.hasOption(IR_PATH);
        if(     (hasPipes ^ !hasIr) ||
                !cmdLine.hasOption(PARALLEL) ||
                !cmdLine.hasOption(OUT_PATH)){

            System.err.println("Missing mandatory arguments.");
            return false;
        }
        String executorType = cmdLine.getOptionValue(EXECUTOR_TYPE);
        if(     executorType.equalsIgnoreCase("mesos") &&
                (!cmdLine.hasOption(EXECUTOR_SSH_PORT) ||
                !cmdLine.hasOption(EXECUTOR_SSH_USERNAME) ||
                !cmdLine.hasOption(EXECUTOR_SSH_PASS) ||
                !cmdLine.hasOption(EXECUTOR_SSH_HOST) ||
                !cmdLine.hasOption(EXECUTOR_CHRONOS_ENDPOINT) ||
                !cmdLine.hasOption(EXECUTOR_BASE_DIR))){

            System.err.println("Missing mandatory arguments.");
            return false;
        }


        return  true;
    }

    private boolean validateArguments(CommandLine cmdLine){
        boolean validatePipes = validatePipesPath(cmdLine.getOptionValue(PIPES_PATH, ""));
        boolean validateIRPath = validateIRPath(cmdLine.getOptionValue(IR_PATH, ""));
        boolean validateOutPath = validateOutPath(cmdLine.getOptionValue(OUT_PATH, ""));
        boolean validateWorkingDirectory = validateOutPath(cmdLine.getOptionValue(WORKING_DIRECTORY, ""));
        boolean validateParallel = validateParallel(cmdLine.getOptionValue(PARALLEL, ""));
        boolean validateParameters = validateParameters(cmdLine.getOptionValue(PARAMETERS, ""));
        boolean validateCpus = validateCpus(cmdLine.getOptionValue(CPUS, ""));
        boolean validateDisk = validateDisk(cmdLine.getOptionValue(DISK, ""));
        boolean validateMem = validateMem(cmdLine.getOptionValue(MEM, ""));
        boolean validateExecType = validateExecutorType(cmdLine.getOptionValue(EXECUTOR_TYPE, ""));
        boolean validateExecSshHost = validateHost(cmdLine.getOptionValue(EXECUTOR_SSH_HOST, ""));
        boolean validateExecSshPort = validatePort(cmdLine.getOptionValue(EXECUTOR_SSH_PORT, ""));
        boolean validateExecSshUsername = validateString(cmdLine.getOptionValue(EXECUTOR_SSH_USERNAME, ""));
        boolean validateExecSshPass = validateString(cmdLine.getOptionValue(EXECUTOR_SSH_PASS, ""));
        boolean validateExecBaseDir = validatePath(cmdLine.getOptionValue(EXECUTOR_BASE_DIR, ""));
        boolean validateExecChronosURL = validateURL(cmdLine.getOptionValue(EXECUTOR_CHRONOS_ENDPOINT, ""));

        return  validatePipes && validateIRPath && validateOutPath &&
                validateParallel && validateParameters && validateCpus &&
                validateDisk && validateMem && validateWorkingDirectory &&
                validateExecBaseDir && validateExecChronosURL &&
                validateExecSshHost && validateExecSshPass &&
                validateExecSshPort && validateExecSshUsername &&
                validateExecType;
    }

    private boolean validateURL(String url) {
        boolean stringValue = validateString(url);
        return stringValue && url.contains("http");
    }

    private boolean validatePath(String path) {
        boolean stringValid = validateString(path);
        return stringValid && (path.contains("\\") || path.contains("/"));
    }

    private boolean validatePort(String port) {
        return validateInteger(port, "Port value must be a positive!", "Invalid port value! It must be an int.");
    }

    private boolean validateHost(String host) {
        return validateString(host);
    }

    private boolean validateString(String stringValue) {
        return stringValue != null && !stringValue.isEmpty();
    }

    private boolean validateExecutorType(String executorType) {
        boolean stringValidated = validateString(executorType);

        if (stringValidated &&
                (executorType.equalsIgnoreCase("mesos") ||
                executorType.equalsIgnoreCase("linux") ||
                executorType.equalsIgnoreCase("vagrant")))
            return true;
        return false;
    }

    private boolean validatePipesPath(String path) {
        boolean valid = true;

        boolean existFile = new File(path).exists();
        if(!path.isEmpty() && !existFile){
            System.err.println("Nonexistent pipeline path!");
            valid = false;
        }

        return valid;
    }

    private boolean validateIRPath(String path) {
        boolean valid = true;

        boolean existsFile = new File(path).exists();
        if(!path.isEmpty() && !existsFile){
            System.err.println("Nonexistent ir path!");
            valid = false;
        }

        return valid;
    }

    private boolean validateOutPath(String path) {
        boolean valid = true;

        if(path == null || path.isEmpty()) {
            System.err.println("Invalid output path");
            valid = false;
        }

        boolean existFile = new File(path).exists();
        if(!existFile){
            System.err.println("Nonexistent output path");
            valid = false;
        }

        return valid;
    }

    private boolean validateParallel(String parallel) {
        return parallel.equalsIgnoreCase("true")
                || parallel.equalsIgnoreCase("false");
    }

    private boolean validateParameters(String parameters) {
        if (parameters != null && !parameters.isEmpty())
            return parameters.contains("=");

        return true;
    }

    private boolean validateCpus(String cpus) {
        return validateInteger(cpus, "Cpus value must be a positive!", "Invalid cpus value! It must be an int.");
    }

    private boolean validateInteger(String cpus, String s, String s2) {
        boolean valid = true;

        if (cpus != null && !cpus.isEmpty()) {
            try {
                int number = Integer.parseInt(cpus);

                if (number <= 0) {
                    System.err.println(s);
                    valid = false;
                }

            } catch (NumberFormatException ex) {
                System.err.println(s2);
                valid = false;
            }
        }

        return valid;
    }


    private boolean validateDisk(String disk) {
        return validateInteger(disk, "Mem value must be a positive!", "Invalid mem value! It must be an int.");
    }

    private boolean validateMem(String mem) {
        return validateInteger(mem, "Mem value must be a positive!", "Invalid mem value! It must be an int.");
    }

    private ConsoleArguments createArguments(CommandLine cmdLine) throws ParseException {
        ConsoleArguments consoleArgs;
        if (cmdLine.hasOption(IR_PATH)) {
            consoleArgs = new ConsoleArguments(
                    cmdLine.getOptionValue(OUT_PATH),
                    cmdLine.getOptionValue(WORKING_DIRECTORY),
                    getInt(cmdLine.getOptionValue(CPUS, DEFAULT_CPUS + "")),
                    getInt(cmdLine.getOptionValue(MEM, DEFAULT_MEM + "")),
                    getInt(cmdLine.getOptionValue(DISK, DEFAULT_DISK + "")),
                    getParallel(cmdLine.getOptionValue(PARALLEL, "false")),
                    getParameters(cmdLine.getOptionValue(PARAMETERS, "")),
                    cmdLine.getOptionValue(IR_PATH),
                    cmdLine.getOptionValue(EXECUTOR_TYPE));
        } else {
            consoleArgs = new ConsoleArguments(
                    cmdLine.getOptionValue(PIPES_PATH),
                    cmdLine.getOptionValue(OUT_PATH),
                    cmdLine.getOptionValue(WORKING_DIRECTORY),
                    getInt(cmdLine.getOptionValue(CPUS, DEFAULT_CPUS + "")),
                    getInt(cmdLine.getOptionValue(MEM, DEFAULT_MEM + "")),
                    getInt(cmdLine.getOptionValue(DISK, DEFAULT_DISK + "")),
                    getParallel(cmdLine.getOptionValue(PARALLEL, "true")),
                    getParameters(cmdLine.getOptionValue(PARAMETERS, "")),
                    cmdLine.getOptionValue(EXECUTOR_TYPE));
        }

        if (consoleArgs.executorType.equalsIgnoreCase("mesos")) {
            consoleArgs.mesosBaseDir = cmdLine.getOptionValue(EXECUTOR_BASE_DIR);
            consoleArgs.mesosChronosEndpoint = cmdLine.getOptionValue(EXECUTOR_CHRONOS_ENDPOINT);
            consoleArgs.mesosSshPass = cmdLine.getOptionValue(EXECUTOR_SSH_PASS);
            consoleArgs.mesosSshHost = cmdLine.getOptionValue(EXECUTOR_SSH_HOST);
            consoleArgs.mesosSshPort = Integer.parseInt(cmdLine.getOptionValue(EXECUTOR_SSH_PORT));
            consoleArgs.mesosSshUsername = cmdLine.getOptionValue(EXECUTOR_SSH_USERNAME);
        }

        return consoleArgs;
    }

    private Map<String, Object> getParameters(String value) throws ParseException {
        Map<String, Object> parameters = new HashMap<>();

        if (value == null || value.isEmpty())
            return parameters;

        String[] parametersStr = value.split(",");

        for (String parameter : parametersStr){
            String[] parameterValue = parameter.split("=");

            if (parameterValue.length < 2)
                throw new ParseException("Error parsing parameters");

            parameters.put(parameterValue[0], parameterValue[1]);
        }

        return parameters;
    }

    private int getInt(String value) {
        return Integer.parseInt(value);
    }

    private boolean getParallel(String value) {
        return value.equalsIgnoreCase("true");
    }
}