package LMP;

class Requete {
    private int horloge;
    private int id;

    public Requete(int horloge, int id) {
        this.horloge = horloge;
        this.id = id;
    }

    public int getHorloge() {
        return horloge;
    }

    public int getId() {
        return id;
    }
}