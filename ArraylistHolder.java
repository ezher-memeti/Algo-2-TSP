import java.util.ArrayList;

public class ArraylistHolder {
    private ArrayList<City> tour1;
    private ArrayList<City> tour2;

    public ArraylistHolder(ArrayList<City> tour1, ArrayList<City> tour2) {
        this.tour1 = tour1;
        this.tour2 = tour2;
    }

    public ArrayList<City> getTour1() {
        return tour1;
    }

    public ArrayList<City> getTour2() {
        return tour2;
    }

}
