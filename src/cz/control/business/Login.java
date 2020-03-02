/*
 * Login.java
 *
 * Created on 13. z��� 2005, 23:11
 */

package cz.control.business;

import cz.control.errors.InvalidLoginException;
import cz.control.data.ClientType;
import cz.control.business.Account;
import cz.control.data.Client;
import java.sql.*;
import java.util.*;

import static cz.control.database.DatabaseAccess.*;
/**
 * Program Control - Skladov� syst�m
 *
 * T��da realizuj�c� p�ihl�en� u�ivatele k syst�mu. Provede vyhled�n� a 
 * zkontrolov�n� u�ivatele podle datab�ze.
 * vytvo�� instanci podt��dy t��dy User, ��m� nastav� p��slu�n� p��stupov� pr�va.
 *
 * @author Kamil Je�ek
 *
 * (C) 2005, ver. 1.0
 */
public final class Login {
    private User logedUser; // Uchov�v� p�ihl�en�ho u�ivatele
    private boolean defaultLogin = false; // Uchov�v�, �e se jedn� o v�choz� p�ihl�en�
    
    /**
     * Provede p�ihl�n� u�ivatele 
     * @param loginName u�ivatelsk� jm�no u�ivatele
     * @param password heslo u�ivatele 
     */
    public Login(String loginName, String password) throws InvalidLoginException, SQLException {
        Account account = new Account(); // Vytvo� instanci pro pr�ci s ��ty
        int managerCount = 0; // Po�et vedouc�h v syst�mu

        ArrayList<Client> clients = account.getAllUser();
        
        /* Projdi seznam klient� */
        for (Client i: clients) {
            if (ClientType.MANAGER.equals(i.getType())) {
                managerCount++; // Zvy� ��ta� po�tu vedouc�h v syst�mu
            }
        }
        
        Client client = account.getUserByLoginName(loginName);
        /* Zjisti zda se rovn� heslo a p�ihla�ovac� jm�no */
        if (password.compareTo(client.getPassword()) == 0 && loginName.compareTo(client.getLoginName()) == 0) {
            /* Vyber o jak� p�ihl�en� se jedn� */
            if (ClientType.MANAGER.equals(client.getType())) {
                logedUser = new Manager(client);
                return;
            }
            if (ClientType.STORE_MAN.equals(client.getType())) {
                logedUser = new StoreMan(client);
                return;
            }
            if (ClientType.CASH.equals(client.getType())) {
                logedUser = new Cash(client);
                return;
            }
        }
        
        /* Jestli�e je tabulka u�ivatel� pr�zdn�, nebo nen� ��dn� vedouc� ulo�en v syst�mu, 
           p�ihla� vedouc�ho */
        if (clients.isEmpty() || managerCount == 0) {
            logedUser = new Manager( new Client(-1, ClientType.MANAGER.getOrdinal(), loginName, "", ""));
            defaultLogin = true;
            return;
        }
        
        throw new InvalidLoginException(); // jestli�e nena�etl u�ivatele ;
    }
    
    /**
     * Provede p�ihl�en� pro v�choz�ho u�ivatele
     */
    public Login() {
            logedUser = new Manager( new Client(-1, ClientType.MANAGER.getOrdinal(), "", "", ""));
            defaultLogin = true;
    }
    
    /**
     * Vrac� instanci p�ihl�en�ho u�ivatele 
     * @return instance p�ihl�en�ho u�ivatele
     */
    public User getUser() {
        return logedUser;
    }
    
    /**
     * Vrac� �et�zec obsahuj�c� jm�bo pr�v� p�ihl�en�ho u�ivatele
     * @return Jm�no pr�v� p�ihl�en�ho u�ivatele
     */
    public String getUserName() {
        return logedUser.getUserName();
    }
    
    /**
     * Vrac�, jestli do�lo k v�choz�mu p�ihl�en�. K takov� situaci dojde,
     * jestli�e v syst�mu nen� zad�n ��dn� u�ivatel, nebo nen� ��dn� u�ivatel 
     * s pravomocemi vedouc�ho.
     * @return true jestli�e se jedn� o v�choz� p�ihl�en�
     */
    public boolean isDefaultLogin() {
        return defaultLogin;
    }
    
    /**
     * Zru�� p�ihl�en�ho u�ivatele
     */
    public void logoutUser() {
        logedUser = null;
    }
    
    
    
}
