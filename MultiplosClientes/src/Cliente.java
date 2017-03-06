
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Cliente extends Thread {
        
    private static int porta = 60000;
    private static Socket conexao;
    private static DataInputStream entrada;
    private static DataOutputStream saida;
    private BigInteger n;
    private BigInteger e;
    private byte[] mykey;
    
    public Cliente(BigInteger e, BigInteger n,byte[] m) {
        this.n = n;
        this.e = e;
        this.mykey = m;
    }
    
    @Override
    public void run() {
        
        while(true) {
        try {
            String msg = entrada.readUTF();
            //Processo para decriptografar a mensagem
            byte[] msgEnc = AES.toHex(msg);
            byte[] msgByte = AES.decode(msgEnc,this.mykey);
            String mensagem = new String(msgByte).trim();
            
            System.out.println(mensagem);
            
        } catch (Exception ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
        
    }
    
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException {
        
        try {
            conexao = new Socket("localhost", porta);
            saida = new DataOutputStream(conexao.getOutputStream());
            entrada = new DataInputStream(conexao.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String msg = "";
            
            BigInteger e = null;
            BigInteger n = null;
            
            //Recebendo chave pública RSA do servidor
            while (e == null) {
                e = new BigInteger(entrada.readUTF());
            }
            while (n == null) {
                n = new BigInteger(entrada.readUTF());
            }
            System.out.println("CHAVE RSA");
            System.out.println("N: "+n);
            System.out.println("E: "+e);
            System.out.println("");
            
            
            //Gerando chaves públicas
            byte[] AESkeyGerada = AES.key(); 
            System.out.println("CHAVE AES");
            //Criptografando chave pública AES com chave RSA recebida do servidor
            BigInteger msg_c = RSA.codificar(AES.fromHex(AESkeyGerada), e, n);
            System.out.println("HEX: "+AES.fromHex(AESkeyGerada));
            System.out.println("HEX CRIP: "+msg_c);
            //Envia chave simétrica AES criptografada com chave RSA
            saida.writeUTF(msg_c.toString());
            
            System.out.println("Digite seu apelido: ");
            String apelido = br.readLine();
            
            Cliente c = new Cliente(e, n, AESkeyGerada);
            c.start();
            
            while(!msg.equals("sair")) {
                
                System.out.println("Sua msg: ");
                msg = br.readLine();
                String msg_formulada = (apelido+ " diz: " +msg);
                
                //Processo para criptografar msg 
                byte[] enc = AES.encode(AES.nullPadString(msg_formulada).getBytes(), AESkeyGerada);
                String msgParaEnviar = AES.fromHex(enc);
                
                saida.writeUTF(msgParaEnviar);
                      
            }
            
        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(0);
    }
}
