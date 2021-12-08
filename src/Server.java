import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class Server {

    // All client names, so we can check for duplicates upon registration.
    private static Set<String> nicki = new HashSet<>();

    // The set of all the print writers for all the clients, used for broadcast.
    private static Set<PrintWriter> transmisja = new HashSet<>();

    public static void main(String[] args) throws Exception {
        System.out.println("Serwer został uruchomiony!");
        var pool = Executors.newFixedThreadPool(8);        // max no of people allowed to chat
        try (ServerSocket server = new ServerSocket(1920)) {
            while (true) {
                pool.execute(new Handler(server.accept()));
            }
        }
    }


    private static class Handler implements Runnable {
        private String nick;
        private Socket socket;
        private Scanner in;
        private PrintWriter out;
        private String names_to_send;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() { // check_login
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);

                // Keep requesting a name until we get a unique one.
                while (true) {
                    out.println("NAZWA");
                    nick = in.nextLine();
                    if (nick == null) {
                        return;
                    }
                    synchronized (nicki) {
                        if (!nick.isBlank() && !nicki.contains(nick)) {
                            nicki.add(nick);
                            break;
                        }
                    }
                }

                // Now that a successful name has been chosen, add the socket's print writer
                // to the set of all writers so this client can receive broadcast messages.
                // But BEFORE THAT, let everyone else know that the new person has joined!
                out.println("unikalna " + nick);
                for (PrintWriter wolaj : transmisja) {
                    System.out.println(nick + " dołączył do czatu! ");
                    wolaj.println("CZAT " + nick + " dołączył do czatu!");
                }
                out.println("CZAT " + "Lista komend: \n" +
                        "CZAT " + "/komendy - pokazuje spis komend \n" +
                        "CZAT " + "/opusc - pozwala rozłączyć się z czatem \n" +
                        "CZAT " + "/lista - pokazuje listę użytkowników \n" +
                        "CZAT " + "/pliki - pokazuje listę plików dostępnych do pobrania \n +" +
                        "CZAT " + "/pobierz - pozwala pobrać plik \n" +
                        "CZAT " + "/wyslij - pozwala wysłać plik");
                transmisja.add(out);

                // Accept messages from this client and broadcast them.
                while (true) {
                    String input = in.nextLine();

                    if (input.toLowerCase().startsWith("/opusc")) {
                        return;
                    } else if (input.toLowerCase().startsWith("/komendy")){
                        out.println("CZAT " + "Lista komend: \n" +
                                "CZAT " + "/komendy - pokazuje spis komend \n" +
                                "CZAT " + "/opusc - pozwala rozłączyć się z czatem \n" +
                                "CZAT " + "/lista - pokazuje listę użytkowników \n" +
                                "CZAT " + "/pliki - pokazuje listę plików dostępnych do pobrania \n +" +
                                "CZAT " + "/pobierz - pozwala pobrać plik \n" +
                                "CZAT " + "/wyslij - pozwala wysłać plik");
                    } else if (input.toLowerCase().startsWith("/lista")) {
                        out.println("CZAT " + "Lista połączonych użytkowników: " + nicki);
                    } else if(input.toLowerCase().startsWith("/pliki")) {
                        String[] lista_plikow;
                        String file_list_to_send = "";
                        File file = new File("lista_plikow.txt");

                        try (BufferedReader read = new BufferedReader(new FileReader(file))) {
                            String firstLine = read.readLine().trim();
                            lista_plikow = firstLine.split(";");

                            for (int i = 0; i < lista_plikow.length; i++) {
                                file_list_to_send += " " + lista_plikow[i] + " ; ";
                            }
                            System.out.println(file_list_to_send);
                            out.println("CZAT " + "Lista dostępnych plików: " + file_list_to_send);
                        }
                    } else if(input.toLowerCase().startsWith("/pobierz")) {
                        out.println("Lista komend: " +
                                "/opusc - pozwala rozłączyć się z czatem \n" +
                                "/lista - pokazuje listę użytkowników \n" +
                                "/pliki - pokazuje listę plików dostępnych do pobrania \n +" +
                                "/pobierz - pozwala pobrać plik \n" +
                                "/wyslij - pozwala wysłać plik");
                    } else if(input.toLowerCase().startsWith("/wyslij")) {

                    }  else {
                        for (PrintWriter wolaj : transmisja) {
                            wolaj.println("CZAT " + nick + ": " + input);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                if (out != null) {
                    out.println("CZAT " + "Opuszczanie czatu. ");
                    transmisja.remove(out);
                }
                if (nick != null) {
                    System.out.println(nick + " opuścił czat!");
                    nicki.remove(nick);
                    for (PrintWriter wolaj : transmisja) {
                        wolaj.println("CZAT " + nick + " opuścił czat!");
                    }
                }
                try { socket.close(); } catch (IOException e) {}
            }
        }
    }
}