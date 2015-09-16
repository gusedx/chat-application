package comp90015.project1.gustavo.client;

public class Debugger {
	
    public static boolean isEnabled(){
        return true;
    }

    public static void log(Object o){
        System.out.println(o.toString());
    }
}
