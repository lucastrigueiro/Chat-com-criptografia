
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Servidor extends Thread {
    
    private static int porta = 60000;
    private static ServerSocket mordomo;
    byte[] keyGerada = null;
     
    private Socket conexao;
    private DataInputStream entrada;
    private DataOutputStream saida;
    private BigInteger n;
    private BigInteger d;
    private byte[] key = null;
    
    private static ArrayList<Servidor> listaConexoes = new ArrayList<Servidor>();
    
    public Servidor(Socket conn, BigInteger n, BigInteger d) {
        
        this.conexao = conn;
        this.n = n;
        this.d = d;
        
        try {       
            entrada = new DataInputStream(this.conexao.getInputStream());
            saida = new DataOutputStream(this.conexao.getOutputStream());
        } catch (IOException ex) {        
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);        
        }
    }
    
    @Override
    public void run() {
        
        String msg;
            
        try {
            while(true) {
            msg = entrada.readUTF();
                System.out.println(this.key);
            //Processo para decriptografar a mensagem
            byte[] msgEnc = AES.toHex(msg);
            byte[] msgByte = AES.decode(msgEnc,this.key);
            String mensagem = new String(msgByte).trim();
            
            //Enviando mensagem decriptografada pela chave simétrica AES e enviando a todos clientes.
            for(Servidor s : listaConexoes) {
                //Processo para criptografar msg 
                byte[] enc = AES.encode(AES.nullPadString(mensagem).getBytes(), s.key);
                String msgParaEnviar = AES.fromHex(enc);
                
                s.saida.writeUTF(msgParaEnviar);
            }
            }
            
        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }

        
    }
    
    public static void main(String[] args) { 
        
        try {    
            mordomo = new ServerSocket(porta);        
            
            while(true) {
            System.out.println("Aguardando cliente...");
            Socket conn = mordomo.accept();
            
            List<BigInteger> list = new ArrayList();
            
            //Gerando list contendo chaves 0=n 1=d 2=e
            list = RSA.geraChaves();
            
            System.out.println("Cliente aceito");
            
            //Instanciando thread do servidor que vai rodar para determinado cliente, com suas respectivas chaves
            Servidor s = new Servidor(conn,list.get(0),list.get(2));
                System.out.println("RSA");
                System.out.println("N: "+s.n);
                System.out.println("D: "+s.d);
                System.out.println("");
                
            //Passando para cliente chave pública RSA
            s.saida.writeUTF(list.get(1).toString());
            s.saida.writeUTF(list.get(0).toString());
            
            while (s.key == null) {
                //Recebendo chave AES criptgrafada com RSA e decriptografando a mesma para ser armazenada no servidor e usada em relação ao cliente
                String chaveAEScrip = s.entrada.readUTF();
                BigInteger ch = new BigInteger(chaveAEScrip);
                String chaveAESdec = RSA.decodificar(ch, s.d, s.n);
                System.out.println("AES");
                System.out.println("HEX CRIP: "+ch);
                System.out.println("HEX: "+chaveAESdec);
                System.out.println("");
                s.key = AES.toHex(chaveAESdec);

            }
            
            listaConexoes.add(s);
            s.start();
            }       
        } catch (Exception e) {            
        System.out.println("Erro: "+e.toString());        
        }
        
    }
}
