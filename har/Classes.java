package har;

public enum Classes {
    BOXING("boxing", "boxing/", 100), HANDCLAPPING("handclapping",
            "handclapping/", 99), HANDWAVING("handwaving", "handwaving/", 100), RUNNING(
            "running", "running/", 100), JOGGING("jogging", "jogging/", 100), WALKING(
            "walking", "walking/", 100);

    private String name;
    private String address;
    private int numberOfVideos;

    Classes(String name, String address, int numberOfVideos) {
        this.name = name;
        this.address = address;
        this.numberOfVideos = numberOfVideos;
    }

    public String getName() {
        return this.name;
    }

    public String getAddress() {
        return this.address;
    }

    public int getNumberOfVideos() {
        return this.numberOfVideos;
    }
}
