import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

class doRecieve2 extends Thread
{   Socket socket;
    Scanner in;
    PrintWriter out;
    Scanner inp;
    boolean loggedIN ;

    public doRecieve2(Socket socket,Scanner in , PrintWriter out , Scanner inp ){
        this.socket = socket ;
        this.in = in ;
        this.out = out ;
        this.inp = inp ;
        this.loggedIN = false ;
    }

    public void run()
    {
        while(in.hasNextLine()){
            var line = in.nextLine();
            if (line.startsWith("NAZWA")) {
                System.out.print("Podaj swój nick: ");
                out.println(inp.nextLine());
            } else if (line.startsWith("unikalna")) {
                System.out.println("Dołączyłeś do czatu jako: " + line.substring(9) );
                if(!loggedIN){
                    doSent ds = new doSent(socket, in, out, inp);
                    ds.start();
                }
            } else if (line.startsWith("CZAT")) {
                System.out.println(line.substring(5) + "\n");
            }
        }
    }
}

class doSent2 extends Thread
{   Socket socket;
    Scanner in;
    PrintWriter out;
    Scanner inp;

    public doSent2(Socket socket,Scanner in , PrintWriter out , Scanner inp ){
        this.socket = socket ;
        this.in = in ;
        this.out = out ;
        this.inp = inp ;
    }

    public void run()
    {
        while(true){
            out.println(inp.nextLine());
        }
    }
}

public class Klient2 {


    public static void main(String[] args) throws IOException {

        Socket socket = new Socket("localhost",1920);
        Scanner in = new Scanner(socket.getInputStream());
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        Scanner inp = new Scanner(System.in);

        doRecieve dr = new doRecieve(socket, in, out, inp);
        dr.start();


    }

}