import java.io.*;
import java.net.*;
import java.util.*;

public class CalcClientEx {
    public static void main(String[] args) {
        BufferedReader in = null;
        BufferedWriter out = null;
        Socket socket = null;
        Scanner scanner = new Scanner(System.in);
        try {
            // default IP, port number, 따로 저장하여 사용
            String serverIP = "localhost";
            int serverPort = 9999;

            // Server_info.dat을 통해 server 정보를 가져오는 코드 try/catch를 사용해 파일 정보가 잘못되거나 이름이 잘못됬을 때
            // 예외 처리
            try {
                File configFile = new File("Server_info.dat");
                if (configFile.exists()) {
                    Scanner configScanner = new Scanner(configFile);
                    if (configScanner.hasNext()) {
                        serverIP = configScanner.next();
                    }
                    if (configScanner.hasNextInt()) {
                        serverPort = configScanner.nextInt();
                    }
                    configScanner.close();
                    System.out.println("Connection for Server_infor.dat");
                }
            } catch (IOException e) {
                System.out.println("Error : Can't read Server_info.dat(Status Code : 404)");
            }

            socket = new Socket(serverIP, serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                while (true) {
                    System.out.print("계산식(빈칸으로 띄어 입력,예:24 + 42, bye 입력시 종료)>>"); // 프롬프트
                    String outputMessage = scanner.nextLine(); // 키보드에서 수식 읽기
                    if (outputMessage.equalsIgnoreCase("bye")) {
                        out.write(outputMessage + "\n"); // "bye" 문자열 전송
                        out.flush();
                        break; // 사용자가 "bye"를 입력한 경우 서버로 전송 후 연결 종료
                    }
                    out.write(outputMessage + "\n"); // 키보드에서 읽은 수식 문자열 전송
                    out.flush();
                    String inputMessage = in.readLine(); // 서버로부터 계산 결과 수신
                    String splitMessage[] = inputMessage.split(",");

                    if (splitMessage[0].equals("200")) {//Error가 없을 때
                        System.out.println("Result : " + splitMessage[1] + "(Status Code : " + splitMessage[0] + ")");
                    } else {//Error가 있을 때
                        System.out.println("ERROR : " + splitMessage[1] + "(Status Code : " + splitMessage[0] + ")");
                    }
                }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                scanner.close();
                if (socket != null)
                    socket.close(); // 클라이언트 소켓 닫기
            } catch (IOException e) {
                System.out.println("Error : Error in chating with server.(Status Code : 500)");
            }
        }
    }
}
