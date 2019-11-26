## NGSPipes Engine App
Console application to execute `pipelines` using **NGSPipes Engine**.
 `-baseDirectory <arg>` Mesos host base working dir.
 `-cpus <arg>`			               Assigned cores
 `-disk <arg>`               Assigned disk space
 `-executorType <arg>`       Executor to be used (mandatory e.x. mesos, vagrant or linux)
 `-ir <arg>`                 Pipeline intermediate representation
 `-mem <arg>`                Assigned max memory in megabytes
 `-out <arg>`                Output absolute pathname (mandatory)
 `-parallel <arg>`           Indicates either execution must be parallel or sequential (mandatory).
 `-parameters <arg>`         Pipeline parameters. (e.x. param_name=1,param1_name=true,param2_name=add)
`-pipes <arg>`              Pipeline path (mandatory)
 `-sshChronosURL <arg>`      Chronos scheduler endpoint URL
 `-sshHost <arg>`            Chronos SSH host
 `-sshPass <arg>`            Chronos SSH password
 `-sshPort <arg>`            Chronos SSH port
 `-sshUsername <arg>`        Chronos SSH username
 `-workingDirectory <arg>`   Working directory absolute pathname (mandatory)