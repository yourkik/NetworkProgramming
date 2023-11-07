//Server_info.dat을 client에서 생성해야하는지 Server에서 생성해야하는지 의문
//Protocol이 상태 메세지를 의미? -> 이에 따른 상태 코드를 출력하도록 변경할지 선택 필요 -> 변경 완료
import java.io.*;
import java.net.*;
import java.util.*;

public class CalcServerExMultiple {
    int statusCode = 0;

    // 예외는 message를 통해 구현(하나의 예외로 여러 예외를 처리하기 위함 -> DivisionByZero의 경우 변경 가능하나 통일함)
    // 11.07 message 형식 변경 : message는 StatusCode,meaning의 형태로 변경됨 Success의 경우 StatusCode에 200 그 외는 각자에 맞는 Status_Code가 들어감
    // 예외 정의 : 유효하지 않은 계산식
    public static class InvalidExpressionException extends Exception {
        public InvalidExpressionException(String message) {
            super(message);
        }
    }

    // 예외 정의 : 0으로 나누었을 때
    public static class DivisionByZeroException extends Exception {
        public DivisionByZeroException(String message) {
            super(message);
        }
    }

    // 명령 및 응답 프로토콜 정의
    public static final String COMMAND_ADD = "ADD";
    public static final String COMMAND_SUBTRACT = "SUB";
    public static final String COMMAND_MULTIPLY = "MUL";
    public static final String COMMAND_DIVIDE = "DIV";

    // Status code 정의
    // 상태 코드 정의
    public static final String STATUS_SUCCESS = "200"; // 성공
    public static final String STATUS_BAD_FORMAT = "400"; // message 형식이 다름
    public static final String STATUS_DIVISION_BY_ZERO = "401"; // 0으로 나눔
    public static final String STATUS_INVALID_EXPRESSION = "402"; // 유효하지 않은 수학 기호
    public static final String STATUS_UNKNOWN_ERROR = "500"; // 알 수 없는 오류, 현재 사용하지는 않으나 의미상 만듦

    // Input : exp(계산식이 저장된 string)
    // Output : res(계산 진행 후의 값을 저장하는 String)
    // 계산을 진행하는 함수로 들어온 exp(계산식)을 Token을 통해 공백을 기준으로 분할 후 숫자로 변환해 계산 이후 출력
    // InvalidException과 DivisionByZeroException을 통해 예외 처리
    public static String calc(String exp) throws InvalidExpressionException, DivisionByZeroException {
        StringTokenizer st = new StringTokenizer(exp, " ");
        if (st.countTokens() != 3) {
            // 계산식 형태에 예외 발생
            throw new InvalidExpressionException(
                    STATUS_BAD_FORMAT + ",Invalid expression format. Enter just format like (a + b)");
        }

        String res = "";
        String responseType;
        int op1 = Integer.parseInt(st.nextToken());
        String opcode = st.nextToken();
        int op2 = Integer.parseInt(st.nextToken());

        switch (opcode) {
            case "+":
                res = Integer.toString(op1 + op2);
                responseType = COMMAND_ADD;
                break;
            case "-":
                res = Integer.toString(op1 - op2);
                responseType = COMMAND_SUBTRACT;
                break;
            case "*":
                res = Integer.toString(op1 * op2);
                responseType = COMMAND_MULTIPLY;
                break;
            case "/":
                if (op2 == 0) {
                    // 0으로 나누는 예외
                    throw new DivisionByZeroException(STATUS_DIVISION_BY_ZERO + ",Division by zero is not allowed.");
                }
                res = Integer.toString(op1 / op2);
                responseType = COMMAND_DIVIDE;
                break;
            // default에서 throw가 발생하면 아래 return을 무시 -> error 발생 -> res에 error를 넣고 이를 검출해
            // throw 발생
            default:
                res = "error";
                responseType = STATUS_INVALID_EXPRESSION;
        }

        if (res == "error") {
            // 저장된 기호가 아닐 때 발생하는 예외
            throw new InvalidExpressionException(responseType + ",Invalid operator(" + opcode + ")");
        } else {
            return STATUS_SUCCESS + "," + responseType + " : " + res;
        }
    }

    public static void main(String[] args) {
        ServerSocket listener = null;
        try {
            listener = new ServerSocket(9999);
            System.out.println("연결을 기다리고 있습니다.....");

            while (true) {
                Socket socket = listener.accept();
                System.out.println("연결되었습니다.");

                Thread clientThread = new Thread(new ClientHandler(socket));// socket에 대해서 Thread를 객체를 생성
                clientThread.start();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (listener != null)
                    listener.close();
            } catch (IOException e) {
                System.out.println("서버 소켓 닫기 중 오류가 발생했습니다.");
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;

        // ClientHander에 대한 생성자
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        // Runnable에 있는 run 함수 override
        @Override
        public void run() {
            BufferedReader in = null;
            BufferedWriter out = null;

            try {
                // 입출력 버퍼, 이를 통해 출력해야 Client에 출력
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                while (true) {
                    String inputMessage = in.readLine();
                    if (inputMessage.equalsIgnoreCase("bye")) {
                        System.out.println("클라이언트에서 연결을 종료하였음");
                        break;
                    }
                    System.out.println(inputMessage);
                    try {
                        String res = calc(inputMessage);
                        out.write(res + "\n");
                    } catch (InvalidExpressionException | DivisionByZeroException e) {
                        out.write(e.getMessage() + "\n");// \n이 없으면 출력되지 않음
                    }
                    out.flush();
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            } finally {
                try {
                    if (socket != null)
                        socket.close();
                } catch (IOException e) {
                    System.out.println("클라이언트와 통신 중 오류가 발생했습니다.");
                }
            }
        }
    }
}
