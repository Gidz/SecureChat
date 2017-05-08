package securechat;

import securechat.ttp.TTP;

import java.util.Scanner;

/**
 * Created by gideon on 07/05/17.
 */
public class Runner {
    public static void main(String args[])
    {
        System.out.print("Enter the port you want to start the server on : ");
        Scanner in = new Scanner(System.in);
        TTP ttpServer = new TTP(Integer.parseInt(in.nextLine()));
    }
}
