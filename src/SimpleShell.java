import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class SimpleShell {
    public static void main(String[] args) throws java.io.IOException, InterruptedException {
        String commandLine;
        BufferedReader console = new BufferedReader(new InputStreamReader((System.in)));
        File nowDir = new File(System.getProperty("user.dir"));
        ArrayList<String> history = new ArrayList<>();
        String hiscmd = "";
        boolean hischeck = false;

        //we break out witn <control><c>
        while (true) {
            try {
                //read what the user entered
                if(hischeck){
                    System.out.print("jsh>");
                    System.out.println(hiscmd);
                    commandLine = hiscmd;
                    hischeck = false;
                }else{
                    System.out.print("jsh>");
                    commandLine = console.readLine();
                }

                //inputArr 만들기
                String[] input = commandLine.split(" ");
                ArrayList<String> inputArr = new ArrayList<>(Arrays.asList(input));

                //histroy 저장 (중복 처리)
                if (history.size() == 0) {
                    history.add(commandLine);
                } else if (!commandLine.equals(history.get(history.size() - 1))) {
                    history.add(commandLine);
                }

                //if the user entered a return, just loop again
                if (commandLine.equals(""))
                    continue;

                //shell 탈출하기
                if (commandLine.equals("exit") || commandLine.equals("quit")) {
                    System.out.println("Goodbye");
                    System.exit(0);
                }

                //현재 dir에 있는 파일&폴더 출력
                if (inputArr.get(0).equals("ls") || inputArr.get(0).equals("cat") || inputArr.get(0).equals("ps"))  {
                    ProcessBuilder pb = new ProcessBuilder(inputArr);
                    pb.directory(nowDir);
                    Process process = pb.start();
                    BufferedReader outReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                    String line;
                    while ((line = outReader.readLine()) != null) {
                        System.out.println(line);
                    }
                    while ((line = errorReader.readLine()) != null) {
                        System.out.println(line);
                    }
                    int exitCode = process.waitFor();
                    assert exitCode == 0;
                    continue;
                }

                //pwd
                if (inputArr.get(0).equals("pwd")) {
                    //Processbuilder 생성 및 디렉토리 설정
                    ProcessBuilder pb = new ProcessBuilder(inputArr);
                    pb.directory(nowDir);
                    System.out.println(pb.directory().toString());
                }

                //change directory
                if (inputArr.get(0).equals("cd")) {
                    //Processbuilder 생성 및 디렉토리 설정
                    ProcessBuilder pb = new ProcessBuilder(inputArr);
                    pb.directory(nowDir);
                    //cd / ;루트
                    if (inputArr.get(1).equals("/")) {
                        pb.directory(new File("/"));
                        nowDir = new File("/");
                        // System.out.println(pb.directory().toString());
                        continue;
                    }//cd 절대경로
                    else if (inputArr.get(1).startsWith("/")) {
                        String Path = inputArr.get(1);
                        pb.directory(new File(Path));
                        nowDir = new File(Path);
                        //System.out.println(pb.directory().toString());
                    }// cd 상대경로 -> ./Project1
                    else if (inputArr.get(1).startsWith("./")) {
                        //현재 경로
                        if (inputArr.get(1).equals("./")) continue;
                            //현재 폴더의 상대경로
                        else {
                            String Path = pb.directory().toString() + "/" + inputArr.get(1).replace("./", "");
                            pb.directory(new File(Path));
                            nowDir = new File(Path);
                        }
                    }
                    //cd .. ;상위 폴더로 이동
                    else if (inputArr.get(1).equals("..") || inputArr.get(1).equals("../")) {
                        File parentPath = pb.directory().getParentFile();
                        pb.directory(parentPath);
                        nowDir = parentPath;
                        // System.out.println(pb.directory().toString());
                        continue;
                    }
                    //cd ../ITM
                    else if (inputArr.get(1).startsWith("../")) {
                        while (true) {
                            File parentPath = pb.directory().getParentFile();
                            File Path = new File(parentPath.toString() + inputArr.get(1).replace("../", ""));
                            pb.directory(Path);
                            nowDir = Path;
                            // System.out.println(pb.directory().toString());
                            continue;

                        }
                    }
                    //cd ~
                    else if (inputArr.get(1).equals("~")){
                        File Path = new File(System.getProperty("user.home"));
                        pb.directory(Path);
                        nowDir = Path;
                        continue;
                    }
                    //cd ~/dir
                    else if (inputArr.get(1).startsWith("~")){
                        File Path = new File(System.getProperty("user.home")+"/"+inputArr.get(1).replace("~/",""));
                        pb.directory(Path);
                        nowDir = Path;
                    }
                    //cd 상대경로 -> Project1
                    else {
                        String absolutePath = pb.directory().toString() + "\\" + inputArr.get(1);
                        pb.directory(new File(absolutePath));
                        nowDir = new File(absolutePath);
                        //System.out.println(pb.directory().toString());
                        continue;
                    }
                }

                //history
                if (inputArr.get(0).equals("history")) {
                    //history 전체 출력
                    if (inputArr.size() == 1) {
                        for (int i = 0; i < history.size(); i++) {
                            System.out.println(i + 1 + " " + history.get(i));
                        }
                    } else {
                        //history number
                        if (inputArr.get(1).matches("[+-]?\\d*(\\.\\d+)?")) {
                            int idx = Integer.parseInt(inputArr.get(1));
                            System.out.println(idx + " " + history.get(idx));
                        }
                        //history !number
                        if (inputArr.get(1).matches("![+-]?\\d*(\\.\\d+)?")) {
                            hischeck = true;
                            hiscmd = history.get(Integer.parseInt(inputArr.get(1).replace("!", ""))- 1);
                        }
                        //history !!
                        if (inputArr.get(1).equals("!!")) {
                            hischeck = true;
                            hiscmd = history.get(history.size() - 2);
                        }
                    }
                }
            }catch (Exception e){
                System.out.println(e.toString());
                System.out.println("Please check the command!");
            }
        }
    }
}