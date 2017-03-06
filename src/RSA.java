/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JOptionPane;


public class RSA {
    
    static BigInteger p,q,msgCodificada;
    static BigInteger n,d,e;
    
    public RSA() {
        
    }
    
    public String menu() {
        String menu = ("========== MENU ============\n");
        menu += ("1 - Gerar chaves\n");
        menu += ("2 - Codificar Mensangem\n");
        menu += ("3 - Decodificar Mensagem\n");
        menu += ("\n0 - SAIR");
        return menu;
    }
  
    
    public static BigInteger codificar(String entrada, BigInteger e, BigInteger n){  
        //conversao de String => BigInteger
        BigInteger c = new BigInteger(entrada.getBytes());
        //m = c^e mod n
        msgCodificada = c.modPow(e, n);
        
        return msgCodificada;
    }
    
    public static String decodificar(BigInteger c, BigInteger d, BigInteger n){
        //c = c^d mod n
        c = c.modPow(d,n);
        String saida = new String(c.toByteArray());
        
        return saida;
    }

    public static List<BigInteger> geraChaves () {
        
        BigInteger phi; //funcao phi de Euler
        final int SIZE = 512; //qtde de bits
        p = BigInteger.probablePrime(SIZE, new Random());
        q = BigInteger.probablePrime(SIZE, new Random());
        //n = p * q; 
        n = p.multiply(q);
        //phi = (p-1)*(q-1);
        phi = p.subtract(BigInteger.valueOf(1)).multiply(q.subtract(BigInteger.valueOf(1)));        
        //Obtem-se um e > 1, coprimo e menor do que phi
        do
        {
            e = new BigInteger(2*SIZE, new Random());
        }
        while ((e.compareTo(phi) != -1) || (e.gcd(phi).compareTo(BigInteger.valueOf(1)) != 0));
        //a funcao compareTo compara se um BigInteger eh menor, igual ou maior 
        //da seguinte forma:
        //se a.compareTo(b) == -1  entao (a < b)
        //se a.compareTo(b) ==  0  entao (a = b)
        //se a.compareTo(b) ==  1  entao (a > b)
        
        //Logo, enquanto pelo menos uma das condicoes acimas continuar sendo 
        //diferente do resultado esperado, o while continua obtendo novo "e".
        
        // Calcula o inverso modular d da variável e
        d = e.modInverse(phi);
        
        List<BigInteger> lista = new ArrayList<>();
        
        lista.add(n);
        lista.add(e);
        lista.add(d);
        
        return lista;
    }

}
