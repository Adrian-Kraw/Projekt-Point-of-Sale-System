package de.fhswf.kassensystem.exception;

public class LeerWarenkorbException extends KassensystemException {
    public LeerWarenkorbException() {
        super("Der Warenkorb ist leer.");
    }
}
