package gui.model;

import gui.MainWindow;
import gui.view.PackageView;
import gui.view.dialog.PlacePackageDialog;
import serial.SerialCommunication;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.concurrent.Executors;

public class RobotQueue {
    public static ArrayList<PackageModel> queue=new ArrayList<>();

    public RobotQueue(){

    }


    public static void printQueue() {
        if(queue.size()>0){
            System.out.println("wachtrij:");
            for(int i = 0; i < queue.size(); i++) {
                System.out.println(i+": "+queue.get(i));
            }
        }else{
            System.out.println("er is geen wachtrij");
        }
    }

    public static void addQueue(PackageModel loadmodel, boolean inladen){
        if(inladen){
            queue.add(loadmodel);
            printQueue();
            PackageView.model.addElement("Inladen Product Id: "+loadmodel.getItemnummer()+" X:"+loadmodel.getLocationX()+" Y:"+loadmodel.getLocationY());
        } else if (!inladen) {
            queue.add(loadmodel);
            printQueue();
            PackageView.model.addElement("Uitladen Product Id: "+loadmodel.getItemnummer()+" X:"+loadmodel.getLocationX()+" Y:"+loadmodel.getLocationY());
        }
        System.out.println(queue.size());
        if(queue.size()==1){
            executeQueue();
        }



    }
    public static void executeQueue(){
        //als er iets in de rij staat gaat die door
        try {
        if(queue.size()>=1) {
                PackageModel item=queue.get(0);
                //kijkt of item al in het magazijn staat of niet
                if (!item.isInMagazijn()) {
                    //item moet ingeladen worden

                    // robot gaat eerst naar beginpunt

                    SerialCommunication.writeToSerial(6,1);
                    //Dialoog wordt aan gemaakt voor het plaatsen van pakket op palletvork
                    PlacePackageDialog placePackageDialog = new PlacePackageDialog(item);


                } else {
                    //item moet worden uitgeladen

                }

            } else {
                System.out.println("wachtrij is leeg");
            }

        } catch (ConcurrentModificationException e) {
            System.err.println("opdracht gecancelled");
        }
    }


    public static void removeQueue() {
        PackageView.model.removeAllElements();
        MainWindow.model.removeAllElements();

        queue.clear();
    }

    public static void removeFirstItem(PackageModel packageModel) {
        queue.remove(packageModel);
        PackageView.model.removeAllElements();
        for (PackageModel item : queue) {
            PackageView.model.addElement(item.toString());
            MainWindow.model.addElement(item.toString());
        }
    }

    public static void inladen(PackageModel packageModel) {
        int x = packageModel.getLocationX();
        int y = packageModel.getLocationY();
        SerialCommunication.writeToSerial(x,y);
        SerialCommunication.setMeldingRobot("A");

        // Start de loop in een nieuwe thread
        Executors.newSingleThreadExecutor().execute(() -> {
            // Wacht op "2" van de seriële poort
            while (true) {
                try {
                String b = SerialCommunication.getMeldingRobot();
                if (b.equals("B")) {
                    SerialCommunication.setMeldingRobot("");
                    System.out.println("Lading is in rack");
                    RackModel.addToRack(packageModel);
                    RobotQueue.removeFirstItem(packageModel);
                    RackModel.printRack();
                    RobotQueue.printQueue();
                    executeQueue();
                    break;

                }else {
                    System.out.println(SerialCommunication.getMeldingRobot());
                }
                }catch (NullPointerException ex){
                    System.err.println("b=null");
                }
            }


        });


    }

    public static boolean CheckIfLoadInRack() {
        return true;
    }



}
