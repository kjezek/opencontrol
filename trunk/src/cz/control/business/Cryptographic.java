/*
 * Cryptographic.java
 *
 * Vytvo�eno 19. �nor 2006, 15:09
 *
 * Autor: Kamil Je�ek
 * email: kjezek@students.zcu.cz
 */

package cz.control.business;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**                    
 * T��da slou�� k zak�dov�n� a dek�dov�n� informac�.
 *  Pou��v� k�dov�n� DES
 *
 * @author Kamil Je�ek
 */
public class Cryptographic {
    
    /** Nastav� jako algortimus DES */
    public static final String DES_CBC = "DES/CBC";
    /**
     * Nastav� jako algortimus DES
     */
    public static final String DES = "DES";
    
    /** Instance objektu pro k�dov�n� */
    private Cipher cipher;
    
    /**
     * Vytvo�� novou instanci Cryptographic
     */
    private Cryptographic(Cipher cipher) {
        this.cipher = cipher;
    }
    
    /**
     * Vrac� instanci pro objekt k�dov�n�
     * @param algoritmus ��t�zec p�edstavuj�c� k�dov�n� ze t��dy Cipher
     * @return Vrac� objekt k�dov�n�
     * @throws java.security.NoSuchAlgorithmException Chybn� algortimus
     * @throws javax.crypto.NoSuchPaddingException Chybn� zarovn�n�
     */
    public static Cryptographic getInstance(String algoritmus) throws NoSuchAlgorithmException, NoSuchPaddingException  {
        
        return new Cryptographic( Cipher.getInstance(algoritmus) );
    }
    

    /**
     * Zak�duje pole byte
     * @param input Vstupn� pole byte
     * @throws javax.crypto.IllegalBlockSizeException �patn� velikost bloku
     * @throws javax.crypto.BadPaddingException �patn� zarovn�n�
     * @return Zak�dovan� pole byte
     */
    public byte[] encryption(byte[] input) throws IllegalBlockSizeException, BadPaddingException {
        try {
            
            cipher.init(Cipher.ENCRYPT_MODE, new MyKey() );
        } catch (InvalidKeyException ex) {
            ex.printStackTrace();
            return null;
        }
        
        return cipher.doFinal(input);
        
    }
    
    /**
     * Odk�duje zak�dovan� pole byte
     * @return Vrac� odk�dovan� pole byte
     * @param input Vstupn� pole byte
     * @throws javax.crypto.IllegalBlockSizeException �patn� velikost bloku
     * @throws javax.crypto.BadPaddingException �patn� zarovn�n�
     */
    public byte[] decryption(byte[] input) throws IllegalBlockSizeException, BadPaddingException {
        try {
            
            cipher.init(Cipher.DECRYPT_MODE, new MyKey() );
        } catch (InvalidKeyException ex) {
            ex.printStackTrace();
            return null;
        }
        
        return cipher.doFinal(input);
    }    
    
    /**
     * Zak�duje �et�zec
     * @param input �et�zec, kter� m� b�t zak�dov�n
     * @return zak�dovan� �et�zec
     * @throws javax.crypto.IllegalBlockSizeException �patn� velikost bloku
     * @throws javax.crypto.BadPaddingException �patn� zarovn�n�
     */
    public String encryption(String input) throws IllegalBlockSizeException, BadPaddingException {
        
        byte[] tmp = input.getBytes();
        byte[] b = encryption(tmp) ;
        
        // �et�zec vytvo�� speci�n� fuknc� pro jednoduch� z�sk�n� zp�tn�ho pole
        // P�i�em� na podob� v�stupn�ho �et�zce p��li� nez�le��
        return byteToString(b);
    }
    
    /**
     * Odk�duje zak�dovan� �et�zec
     * @param input Zak�dovan� �et�zec
     * @return Odk�dovan� �et�zec
     * @throws javax.crypto.IllegalBlockSizeException �patn� velikost bloku
     * @throws javax.crypto.BadPaddingException �patn� zarovn�n�
     */
    public String decryption(String input) throws IllegalBlockSizeException, BadPaddingException {

        // Opa�nou funkc� z�sk�me op�t z �et�zce pole
        byte[] tmp = stringToByte(input);
        byte[] b = decryption(tmp);
        
        // Poslou�� ke snadn�mu p�evodu pole byte na �et�zec
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            bos.write(b);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return bos.toString();    
    }
    
    /**
     *  P�evede �et�zec na pole byte
     */
    private byte[] stringToByte(String input) {
        byte[] result = new byte[input.length()];

        /*char c;
        byte b;
        for (int i = 0; i < input.length(); i++) {
            c = input.charAt(i);
            b = ((byte) c);
            
            result[i] = b;
        }*/

        /* �et�zec ��sel odd�len� ��rkou p�eve� na pole */
        String[] tmp = input.split(",");
        result = new byte[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            result[i] = Byte.valueOf( tmp[i].trim() );
        }

            
        return result;
    }
    
    /**
     *  P�evede pole byte na �et�zec
     */
    private String byteToString(byte[] input) {
        StringBuffer result = new StringBuffer();

        /*char c;
        for (int i = 0; i < input.length; i++) {
            c = (char) input[i];
            result.append(c);
        }*/
        
        /* P�eve� na �et�zec ��sel odd�len� ��rkou */
        for (int i = 0; i < input.length; i++) {
            result.append( input[i] + ", ");
        }

        return result.toString();
    }
    
    /**
     *  T��da implementuj�c� kl�� pro �ifru
     */
    private class MyKey implements Key {
        public String getAlgorithm() {
            return "DES";
        }

        public String getFormat() {
            return "RAW";
        }

        public byte[] getEncoded() {
            byte[] key = { (byte) 1, (byte) 2, (byte) 3, (byte) 8, (byte) 109, 
                            (byte) 48, (byte) 23, (byte) 197};
            return key;
        }
        
    }
    
    
    
}
