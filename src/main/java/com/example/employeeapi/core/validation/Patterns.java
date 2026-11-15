package com.example.employeeapi.core.validation;

/** Formats attendus pour l'email et le mot de passe. */
public final class Patterns {

    private Patterns() {
    }

    /** Minuscule, majuscule, chiffre, caractère spécial, 8 caractères minimum. */
    public static final String PASSWORD =
            "^(?=.*[`~!@\\\\#$%^&*()+=\\[{\\]}'<,.>?\";:|/\\-])(?=.{8,})(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$";

    /** Adresse email valide (source : http://emailregex.com/). */
    public static final String EMAIL =
            "^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))"
                    + "@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
}
