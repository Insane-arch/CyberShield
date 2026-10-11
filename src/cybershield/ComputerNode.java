package cybershield;

// Project members:
// Muhammad Hamza Naseer - 247121
// Sultan Majeed - 247187

public class ComputerNode {
    private final int id;
    private boolean compromised;
    private boolean isolated;
    private boolean protectedNode;

    public ComputerNode(int id) {
        this.id = id;
        this.compromised = false;
        this.isolated = false;
        this.protectedNode = false;
    }

    public int getId() {
        return id;
    }

    public boolean isCompromised() {
        return compromised;
    }

    public boolean isIsolated() {
        return isolated;
    }

    public boolean isProtected() {
        return protectedNode;
    }

    public void compromise() {
        compromised = true;
    }

    public void isolate() {
        isolated = true;
    }

    public void protect() {
        protectedNode = true;
    }

    @Override
    public String toString() {
        String status = compromised ? "COMPROMISED" : "HEALTHY";

        return "Computer " + id
                + " | Status: " + status
                + " | Isolated: " + isolated
                + " | Protected: " + protectedNode;
    }
}