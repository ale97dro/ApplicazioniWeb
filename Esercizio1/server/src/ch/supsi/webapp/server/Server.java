package ch.supsi.webapp.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class Server {

	private static ServerSocket serverSocket;
	private final static int PORT = 8080;
	private final static String CONTENT_LENGTH_HEADER = "Content-Length";
	private final static String LINEBREAK = "\r\n";

	public static void main(String[] args) throws Exception {
		serverSocket = new ServerSocket(PORT);
		System.out.println("Server avviato sulla porta : " + PORT);
		System.out.println("-------------------------------------");

		while (true) {
			Socket clientSocket = serverSocket.accept();
			clientSocket.setSoTimeout(200);
			handleRequest(clientSocket);
			clientSocket.close();
		}
	}

	public static void handleRequest(Socket socket)
	{
		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			OutputStream out = socket.getOutputStream();

			Request request = readRequest(in);
			System.out.println(request.allRequest);
			
			Content responseBody = handleResponseContent(request);
			produceResponse(out, responseBody);

			out.flush();
			out.close();
			in.close();
		}
		catch (Exception e) {
			// e.printStackTrace();
		}
	}

	private static Request readRequest(BufferedReader input) throws IOException
	{
		String firstline = input.readLine();
		if (firstline != null) {
			System.out.println("----------------- " + new Date() + " --------------");
			boolean isPost = firstline.startsWith("POST");
			return getRequest(input, firstline, isPost);
		}
		return null;
	}

	private static Request getRequest(BufferedReader input, String line, boolean isPost) throws NumberFormatException, IOException {
		StringBuilder rawRequest = new StringBuilder();
		rawRequest.append(line);
		String resource = line.substring(line.indexOf(' ')+1, line.lastIndexOf(' '));
		int contentLength = 0;
		while (!(line = input.readLine()).equals("")) {
			rawRequest.append('\n' + line);
			if (line.startsWith(CONTENT_LENGTH_HEADER))
				contentLength = Integer.parseInt(line.substring(CONTENT_LENGTH_HEADER.length()+2));
		}
		String body = "";
		if (isPost) {
			rawRequest.append("\n\n" + getBody(input, contentLength));
		}
		return new Request(rawRequest.toString(), resource, body, isPost);
	}

	private static String getBody(BufferedReader bf, int length) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++)
			sb.append((char) bf.read());
		return sb.toString();
	}

	/*
	 * Usare questo metodo per gestire la richiesta ricevuta e produrre un 
	 * contenuto (txt, html, ...) da dare come corpo nella risposta
	 * 
	 */
	private static Content handleResponseContent(Request request)
	{
		//return new Content("Il corpo della riposta va qui".getBytes()); //Esercizio base

		//return new Content("<!DOCTYPE html><html> <head><meta charset=\"UTF-8\"><title>Prova</title> </head> <body>Il mio primo documento HTML5 </body> </html>".getBytes()); //Esercizio 3

		
		//ESERCIZIO 4
		/*StringBuilder body =new StringBuilder();

		body.append("<!DOCTYPE html>");
		body.append("<html> <head><meta charset=\"UTF-8\"><title>Prova</title> </head>");
		body.append("<body>Il mio primo documento HTML5");

		body.append("<br/><p>Domanda</p>");
		body.append("<form method=\"POST\">");
		body.append("<input type=\"text\" name=\"domanda\" />");
		body.append("<input type=\"submit\" />");
		body.append("</form></body></html>");

		//return new Content(body.toString().getBytes());
		*/

		//ESERCIZIO 5
		//Analizzo la richiesta per trovare la pagina da mandare
		//Leggo la pagina da file
		
		String[] risorsa = request.resource.split("/");
		
		StringBuilder body= new StringBuilder();
		String path="D:\\alex2\\Desktop\\"+risorsa[risorsa.length-1]+".html";
		
		File f = new File(path);
		
		if(f.exists())
		{
			try 
			{
				BufferedReader reader = new BufferedReader(new FileReader(path));
				 System.out.println(path);
				 			 
				 String linea = null;
				 
				 while((linea=reader.readLine())!=null)
					 body.append(linea);
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			
			return new Content(body.toString().getBytes());
		
		}
		else
		{
			return null;
		}
	}

	/*
	 * Usare questo metodo per scrivere l'intera risposta HTTP (prima linea+headers+body)
	 * 
	 */
	private static void produceResponse(OutputStream output, Content responseContent) throws IOException 
	{
		// usare la variabile LINEBREAK per andare a capo
		String risposta;

		//RICAVO DATA
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEE, yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		//--------------------------------------------------

		if(responseContent!=null)
		{
			risposta = "HTTP/1.1 200 OK"+LINEBREAK; //prima riga
			risposta += "Date: "+ dtf.format(now)+LINEBREAK; //Esercizio 3
			risposta += "Content-Type: text/html; charset=UTF-8"+LINEBREAK;
			risposta += "Content-Length: "+responseContent.length+LINEBREAK+LINEBREAK;
			risposta += new String(responseContent.content);
		}
		else
		{
			risposta = "HTTP/1.1 404 Not Found"+LINEBREAK; //prima riga
			risposta += "Date: "+ dtf.format(now)+LINEBREAK; //Esercizio 3
			risposta += "Content-Type: text/html; charset=UTF-8"+LINEBREAK;
			risposta += "Content-Length: 0"+LINEBREAK+LINEBREAK;
		}

		output.write(risposta.getBytes()); //scrivo la risposta
		
		//output.write(new String(responseContent.content).getBytes()); //Esercizio base
	}

}