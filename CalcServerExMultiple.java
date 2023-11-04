import java.io.*;
import java.net.*;
import java.util.*;

public class CalcServerExMultiple {
    public static class DivisideByZeroException extends Exception{
        DivisideByZeroException(){
            super("Dividing by zero");
        }
}
public static class InvalidExpressionException extends Exception {
    InvalidExpressionException(String message) {
        super(message);    
    }
}

    public static String calc(String exp) throws DivisideByZeroException, InvalidExpressionException{
        StringTokenizer st = new StringTokenizer(exp, " ");
        if (st.countTokens() != 3)
            throw new InvalidExpressionException("Invalid expression");
        String res = "";
        int op1 = Integer.parseInt(st.nextToken());
        String opcode = st.nextToken();
        int op2 = Integer.parseInt(st.nextToken());
        switch (opcode) {
            case "+":
                res = Integer.toString(op1 + op2);
                break;
            case "-":
                res = Integer.toString(op1 - op2);
                break;
            case "*":
                res = Integer.toString(op1 * op2);
                break;
            case "/":
                if(op2==0)
                    throw new DivisideByZeroException();
                res = Integer.toString(op1/op2);
                break;
            default:
                throw new InvalidExpressionException("Invalid opcode"+opcode);
        }
        return res;
    }

    public static void main(String[] args) {
        ServerSocket listener = null;

        try {
            listener = new ServerSocket(9999);
            System.out.println("연결을 기다리고 있습니다.....");

            while (true) {
                Socket socket = listener.accept();
                System.out.println("연결되었습니다.");

                Thread clientThread = new Thread(new ClientHandler(socket));
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
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                while (true) {
                    String inputMessage = in.readLine();
                    if (inputMessage.equalsIgnoreCase("bye")) {
                        System.out.println("클라이언트에서 연결을 종료하였음");
                        break;
                    }
                    System.out.println(inputMessage);
                    try{
                    String res = calc(inputMessage);
                    out.write(res + "\n");
                    } catch(DivisideByZeroException e){
                        out.write(e.getMessage());
                    }catch(InvalidExpressionException e2){
                        out.write(e2.getMessage());
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
