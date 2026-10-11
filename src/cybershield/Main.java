package cybershield;

public class Main {
    public static void main(String[] args) {
        System.out.println("======================================");
        System.out.println("             CyberShield");
        System.out.println(" Network Attack Containment Simulator");
        System.out.println("======================================");
        System.out.println("Muhammad Hamza Naseer - 247121");
        System.out.println("Sultan Majeed - 247187");

        ComputerNode firstComputer = new ComputerNode(1);
        ComputerNode secondComputer = new ComputerNode(2);

        System.out.println("\nInitial computer states:");
        System.out.println(firstComputer);
        System.out.println(secondComputer);

        firstComputer.protect();
        secondComputer.compromise();
        secondComputer.isolate();

        System.out.println("\nAfter state changes:");
        System.out.println(firstComputer);
        System.out.println(secondComputer);
    }
}