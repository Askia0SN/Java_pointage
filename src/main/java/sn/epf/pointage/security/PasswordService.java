package sn.epf.pointage.security;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordService {

    public String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas etre vide.");
        }

        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public boolean matches(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null || hashedPassword.isBlank()) {
            return false;
        }

        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
