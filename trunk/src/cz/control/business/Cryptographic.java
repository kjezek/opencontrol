/*
 * Cryptographic.java
 *
 * Vytvoøeno 19. únor 2006, 15:09
 *
 * Autor: Kamil Ježek
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
 * Tøída slouží k zakódování a dekódování informací.
 *  Používá kódování DES
 *
 * @author Kamil Ježek
 */
public class Cryptographic {
    
    /** Nastaví jako algortimus DES */
    public static final String DES_CBC = "DES/CBC";
    /**
     * Nastaví jako algortimus DES
     */
    public static final String DES = "DES";
    
    /** Instance objektu pro kódování */
    private Cipher cipher;
    
    /**
     * Vytvoøí novou instanci Cryptographic
     */
    private Cryptographic(Cipher cipher) {
        this.cipher = cipher;
    }
    
    /**
     * Vrací instanci pro objekt kódování
     * @param algoritmus Øìtìzec pøedstavující kódování ze tøídy Cipher
     * @return Vrací objekt kódování
     * @throws java.security.NoSuchAlgorithmException Chybný algortimus
     * @throws javax.crypto.NoSuchPaddingException Chybné zarovnání
     */
    public static Cryptographic getInstance(String algoritmus) throws NoSuchAlgorithmException, NoSuchPaddingException  {
        
        return new Cryptographic( Cipher.getInstance(algoritmus) );
    }
    

    /**
     * Zakóduje pole byte
     * @param input Vstupní pole byte
     * @throws javax.crypto.IllegalBlockSizeException Špatná velikost bloku
     * @throws javax.crypto.BadPaddingException Špatné zarovnání
     * @return Zakódované pole byte
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
     * Odkóduje zakódované pole byte
     * @return Vrací odkódované pole byte
     * @param input Vstupní pole byte
     * @throws javax.crypto.IllegalBlockSizeException Špatná velikost bloku
     * @throws javax.crypto.BadPaddingException Špatné zarovnání
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
     * Zakóduje øetìzec
     * @param input øetìzec, který má být zakódován
     * @return zakódovaný øetìzec
     * @throws javax.crypto.IllegalBlockSizeException Špatná velikost bloku
     * @throws javax.crypto.BadPaddingException Špatné zarovnání
     */
    public String encryption(String input) throws IllegalBlockSizeException, BadPaddingException {
        
        byte[] tmp = input.getBytes();
        byte[] b = encryption(tmp) ;
        
        // Øetìzec vytvoøí speciání fukncí pro jednoduché získání zpìtného pole
        // Pøièemž na podobì výstupního øetìzce pøíliš nezáleží
        return byteToString(b);
    }
    
    /**
     * Odkóduje zakódovaný øetìzec
     * @param input Zakódovaný øetìzec
     * @return Odkódovaný øetìzec
     * @throws javax.crypto.IllegalBlockSizeException Špatná velikost bloku
     * @throws javax.crypto.BadPaddingException Špatné zarovnání
     */
    public String decryption(String input) throws IllegalBlockSizeException, BadPaddingException {

        // Opaènou funkcí získáme opìt z øetìzce pole
        byte[] tmp = stringToByte(input);
        byte[] b = decryption(tmp);
        
        // Poslouží ke snadnému pøevodu pole byte na øetìzec
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            bos.write(b);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return bos.toString();    
    }
    
    /**
     *  Pøevede øetìzec na pole byte
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

        /* Øetìzec èísel oddìlený èárkou pøeveï na pole */
        String[] tmp = input.split(",");
        result = new byte[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            result[i] = Byte.valueOf( tmp[i].trim() );
        }

            
        return result;
    }
    
    /**
     *  Pøevede pole byte na øetìzec
     */
    private String byteToString(byte[] input) {
        StringBuffer result = new StringBuffer();

        /*char c;
        for (int i = 0; i < input.length; i++) {
            c = (char) input[i];
            result.append(c);
        }*/
        
        /* Pøeveï na øetìzec èísel oddìlený èárkou */
        for (int i = 0; i < input.length; i++) {
            result.append( input[i] + ", ");
        }

        return result.toString();
    }
    
    /**
     *  Tøída implementující klíè pro šifru
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
