package util;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static util.GlobalConstants.*;

/**
 * Created by Dale on 21/07/16.
 */
public final class AndroidSdkUtils {
    private static final String ANDROID_DIRECTORY  = "android";
    private static final String SDK_DIR = "sdk";
    private static final String ANDROID_PLATFORM_TOOLS_DIRECTORY = "platform-tools";
    private static final String ANDROID_TOOLS_DIRECTORY = "tools";
    private static final String EMULATOR_TOOL = "emulator";
    private static final String ANDROID_TOOL = "android";
    private static final String ADB_TOOL = "adb";
    public static final String ANDROID_SDK_ENV_VARIABLE = "ANDROID_SDK_DIR";
    private static final Charset CMD_OUTPUT_ENCODING = Charset.forName("UTF-8");
    private static final String CREATE_AVD = "create avd";
    private static final String LIST_AVD = "list avd";


    public static boolean createAVD(String sdkLocation, String name, String target, String abi) throws IOException {

        if(sdkLocation == null && (sdkLocation = tryLocateAndroidSDK()) == null)
            Objects.requireNonNull(sdkLocation);


        String avdNameOption = " --name  ";
        String avdTargetOption = " --target ";
        String avdAbiOption = " --abi ";
        String globalOption = " -s ";
        String output = "no\n";

        StringBuilder commandBuilder = new StringBuilder();
        String command = commandBuilder.append(sdkLocation)
                .append(FILE_SEPARATOR)
                .append(ANDROID_TOOLS_DIRECTORY)
                .append(FILE_SEPARATOR)
                .append(ANDROID_TOOL)
                .append(globalOption)
                .append(CREATE_AVD)
                .append(avdNameOption).append(name)
                .append(avdTargetOption).append(target)
                .append(avdAbiOption).append(abi).toString();

        System.out.println(command.toString());

        return ProcessUtils.executeCommand(command, new StringWriter(), output, CMD_OUTPUT_ENCODING);
    }

    public static String avdList(String sdkLocation) throws IOException {
        StringBuilder commandBuilder = new StringBuilder();
        String command = commandBuilder.append(sdkLocation)
                .append(FILE_SEPARATOR)
                .append(ANDROID_TOOLS_DIRECTORY)
                .append(FILE_SEPARATOR)
                .append(ANDROID_TOOL)
                .append(SPACE)
                .append(LIST_AVD).toString();

        StringWriter sw = new StringWriter();
        ProcessUtils.executeCommand(command,sw, null, CMD_OUTPUT_ENCODING);
        return sw.getBuffer().toString();
    }

    public static boolean createAVD(String name, String target, String abi) throws Exception {
        String androidSdkLocation;
        if((androidSdkLocation = tryLocateAndroidSDK()) == null)
            throw new Exception("Cannot locate android sdk to create android virtual device");

        return createAVD(androidSdkLocation,name,target,abi);
    }

    public static boolean isValidSdkLocation(String path){
        Path emulatorTool = Paths.get(path + FILE_SEPARATOR +
                ANDROID_TOOLS_DIRECTORY + FILE_SEPARATOR + EMULATOR_TOOL);

        Path androidTool = Paths.get(path + FILE_SEPARATOR +
                ANDROID_TOOLS_DIRECTORY + FILE_SEPARATOR + ANDROID_TOOL);

        Path adbTool = Paths.get(path + FILE_SEPARATOR +
                ANDROID_PLATFORM_TOOLS_DIRECTORY + FILE_SEPARATOR + ADB_TOOL);

        return path != null && Files.exists(emulatorTool) && Files.exists(androidTool) && Files.exists(adbTool);
    }

    public static String tryLocateAndroidSDK() {
        Optional<String> androidSdkDir =  Stream.of(separatedSystemPath())
                .filter(path->(path.toLowerCase().contains(ANDROID_DIRECTORY) ||
                        path.toLowerCase().contains(SDK_DIR))
                        && (path.contains(ANDROID_PLATFORM_TOOLS_DIRECTORY)
                        || path.contains(ANDROID_TOOLS_DIRECTORY))).findFirst();

        String finalDir;

        if(androidSdkDir.isPresent())
            finalDir = androidSdkDir.get();
        else
            finalDir = System.getenv(ANDROID_SDK_ENV_VARIABLE);

        if(isValidSdkLocation(finalDir)){
            finalDir = finalDir.substring(0,finalDir.endsWith(FILE_SEPARATOR)?
                    finalDir.lastIndexOf(FILE_SEPARATOR):finalDir.length());
        }

        return finalDir;
    }

    private AndroidSdkUtils(){}
}
