package net.doodcraft.Dooder07.Stargates.Wormhole.utils;

public class SGStringUtils {

    public static boolean isIntNumber(String num) {
        try{
            Integer.parseInt(num);
        } catch(NumberFormatException e) {
            return false;
        }
        return true;
    }
}
