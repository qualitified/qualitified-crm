package org.qualitified.crm.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Command {

    public static void main(String[] arg) throws IOException, InterruptedException {
        String key = "~/.ssh/key.pem";
        String host = "xxx";
        String cmd = "mp-list";
        Process p = Runtime.getRuntime().exec("ssh -i "+key+" xxx@"+host+" './qualitified.sh' '"+cmd+"'  -o StrictHostKeyChecking=no");
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        InputStream errorStream = p.getErrorStream();
        int c = 0;
        while ((c = errorStream.read()) != -1) {
            System.out.print((char)c);
        }
        while(in.ready()) {
            String s = in.readLine();
            System.out.println(s);
        }
        System.out.println("exit");
        p.waitFor();
    }
}
