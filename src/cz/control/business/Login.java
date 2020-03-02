/*
 * Login.java
 *
 * Created on 13. záøí 2005, 23:11
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
 * Program Control - Skladový systém
 *
 * Tøída realizující pøihlášení uživatele k systému. Provede vyhledání a 
 * zkontrolování uživatele podle databáze.
 * vytvoøí instanci podtøídy tøídy User, èímž nastaví pøíslušná pøístupová práva.
 *
 * @author Kamil Ježek
 *
 * (C) 2005, ver. 1.0
 */
public final class Login {
    private User logedUser; // Uchovává pøihlášeného uživatele
    private boolean defaultLogin = false; // Uchovává, že se jedná o výchozí pøihlášení
    
    /**
     * Provede pøihlášní uživatele 
     * @param loginName uživatelské jméno uživatele
     * @param password heslo uživatele 
     */
    public Login(String loginName, String password) throws InvalidLoginException, SQLException {
        Account account = new Account(); // Vytvoø instanci pro práci s úèty
        int managerCount = 0; // Poèet vedoucíh v systému

        ArrayList<Client> clients = account.getAllUser();
        
        /* Projdi seznam klientù */
        for (Client i: clients) {
            if (ClientType.MANAGER.equals(i.getType())) {
                managerCount++; // Zvyš èítaè poètu vedoucíh v systému
            }
        }
        
        Client client = account.getUserByLoginName(loginName);
        /* Zjisti zda se rovná heslo a pøihlašovací jméno */
        if (password.compareTo(client.getPassword()) == 0 && loginName.compareTo(client.getLoginName()) == 0) {
            /* Vyber o jaké pøihlášení se jedná */
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
        
        /* Jestliže je tabulka uživatelù prázdná, nebo není žádný vedoucí uložen v systému, 
           pøihlaš vedoucího */
        if (clients.isEmpty() || managerCount == 0) {
            logedUser = new Manager( new Client(-1, ClientType.MANAGER.getOrdinal(), loginName, "", ""));
            defaultLogin = true;
            return;
        }
        
        throw new InvalidLoginException(); // jestliže nenaèetl uživatele ;
    }
    
    /**
     * Provede pøihlášení pro výchozího uživatele
     */
    public Login() {
            logedUser = new Manager( new Client(-1, ClientType.MANAGER.getOrdinal(), "", "", ""));
            defaultLogin = true;
    }
    
    /**
     * Vrací instanci pøihlášeného uživatele 
     * @return instance pøihlášeného uživatele
     */
    public User getUser() {
        return logedUser;
    }
    
    /**
     * Vrací øetìzec obsahující jmébo právì pøihlášeného uživatele
     * @return Jméno právì pøihlášeného uživatele
     */
    public String getUserName() {
        return logedUser.getUserName();
    }
    
    /**
     * Vrací, jestli došlo k výchozímu pøihlášení. K takové situaci dojde,
     * jestliže v systému není zadán žádný uživatel, nebo není žádný uživatel 
     * s pravomocemi vedoucího.
     * @return true jestliže se jedná o výchozí pøihlášení
     */
    public boolean isDefaultLogin() {
        return defaultLogin;
    }
    
    /**
     * Zruší pøihlášeného uživatele
     */
    public void logoutUser() {
        logedUser = null;
    }
    
    
    
}
