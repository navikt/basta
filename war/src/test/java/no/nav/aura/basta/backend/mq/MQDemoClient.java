package no.nav.aura.basta.backend.mq;

import java.util.Scanner;

public class MQDemoClient {

    public static void main(String[] a) throws Exception
    {
        String hostname = "e26apvl100.test.local";
        int port = 1411;
        String connectChannel = "MOD.MDLCLIENT03";
        String userid = "modadmin";

        System.out.println("Input:");
        Scanner in = new Scanner(System.in);
        System.out.print("Environment: ");
        String environmentName = in.nextLine();
        System.out.print("Application: ");
        String applicationName = in.nextLine();
        System.out.print("Queue name: ");
        String queueName = in.nextLine();
        in.close();

        MQChannel channel = new MQChannel(environmentName, applicationName);
        MQQueue queue = new MQQueue(queueName, environmentName, applicationName);

        try (MqAdmin mq = new MqAdmin(hostname, port, connectChannel, userid, "MDLCLIENT03")) {
            mq.createOrUpdate(channel);
            mq.createOrUpdate(queue);
            System.out.println();
            System.out.println("Data");
            mq.print(queue);
            mq.print(channel);
        }
    }
}
