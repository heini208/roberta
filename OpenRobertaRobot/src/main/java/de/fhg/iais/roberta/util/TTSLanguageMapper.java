package de.fhg.iais.roberta.util;

import de.fhg.iais.roberta.inter.mode.action.ILanguage;
import de.fhg.iais.roberta.mode.action.Language;

public class TTSLanguageMapper {

    public static String getLanguageString(ILanguage language) {
        switch ( (Language) language ) {
            case GERMAN:
                return "de";
            case ENGLISH:
                return "en";
            case FRENCH:
                return "fr";
            case SPANISH:
                return "es";
            case ITALIAN:
                return "it";
            case DUTCH:
                return "nl";
            case FINNISH:
                return "fi";
            case POLISH:
                return "pl";
            case RUSSIAN:
                return "ru";
            case TURKISH:
                return "tu";
            case CZECH:
                return "cs";
            case PORTUGUESE:
                return "pt-pt";
            case DANISH:
                return "da";
            default:
                return "en";
        }
    }

    public static String getLanguageCountryString(ILanguage language) {
        switch ( (Language) language ) {
            case GERMAN:
                return "de-DE";
            case ENGLISH:
                return "en-GB";
            case FRENCH:
                return "fr-FR";
            case SPANISH:
                return "es-ES";
            case ITALIAN:
                return "it-IT";
            default:
                return "en-GB";
        }
    }

}
