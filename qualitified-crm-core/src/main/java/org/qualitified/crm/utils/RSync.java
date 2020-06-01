package org.qualitified.crm.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RSync {

    public static void main(String[] arg) throws IOException, InterruptedException {
        String key = "~/.ssh/key.pem";
        String host = "xxx";
        String packagePath = "xxx";

        Process p = Runtime.getRuntime().exec(new String[]{"rsync","-avL", "--progress", "-e", "ssh -i  "+key+" -o StrictHostKeyChecking=no", packagePath, "xxx@"+host+":/opt/import/"});

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
