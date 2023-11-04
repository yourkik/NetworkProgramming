import java.io.*;
import java.net.*;
import java.util.*;

//server info 부분 미완성
public class CalcServerExMultiple {
    //예외 정의 : 유효하지 않은 계산식
    public static class InvalidExpressionException extends Exception {
        public InvalidExpressionException(String message) {
            super(message);
        }
    }

    //예외 정의 : 0으로 나누었을 때
    public static class DivisionByZeroException extends Exception {
        public DivisionByZeroException(String message) {
            super(message);
        }
    }

    public static String calc(String exp) throws InvalidExpressionException, DivisionByZeroException {
        StringTokenizer st = new StringTokenizer(exp, " ");
        if (st.countTokens() != 3) {
            throw new InvalidExpressionException("Invalid expression format : " + exp + ". Enter just format like (a + b)");
        }

        int op1 = Integer.parseInt(st.nextToken());
        String opcode = st.nextToken();
        int op2 = Integer.parseInt(st.nextToken());

        switch (opcode) {
            case "+":
                return Integer.toString(op1 + op2);
            case "-":
                return Integer.toString(op1 - op2);
            case "*":
                return Integer.toString(op1 * op2);
            case "/":
                if (op2 == 0) {
                    throw new DivisionByZeroException("Division by zero is not allowed.");
                }
                return Integer.toString(op1 / op2);
            default:
                throw new InvalidExpressionException("Invalid operator(" + opcode+")");
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

                Thread clientThread = new Thread(new ClientHandler(socket));//socket에 대해서 Thread를 객체를 생성
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

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            BufferedReader in = null;
            BufferedWriter out = null;

            try {
                //입출력 버퍼, 이를 통해 출력해야 Client에 출력
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
                        out.write("Error : " + e.getMessage() + "\n");//\n이 없으면 출력되지 않음
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
