package vn.iuh.gui.base;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DemoLocalIp {
    public static void main(String[] args) {
        try {
            URL url = new URL("https://api.ipify.org");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String ip = in.readLine();
            in.close();

            System.out.println("Public IP: " + ip);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
